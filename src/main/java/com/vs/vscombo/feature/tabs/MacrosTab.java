package com.vs.vscombo.feature.tabs;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.vs.vscombo.VSBaseMod;
import com.vs.vscombo.core.gui.IVSTab;
import com.vs.vscombo.util.VSFileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.*;

public class MacrosTab implements IVSTab {
    
    private Screen parent;
    private int x, y, width, height;
    
    private final List<String> lines = new ArrayList<>(Collections.singletonList(""));
    private int cursorLine = 0, cursorCol = 0;
    private boolean cursorVisible = true;
    private long lastCursorToggle = 0;
    private int scrollX = 0, scrollY = 0;
    
    private final File saveFile;
    private static String clipboard = "";
    private final List<Button> tabButtons = new ArrayList<>();

    public MacrosTab() {
        File configDir = new File(Minecraft.getInstance().gameDir, "config/vscombo");
        if (!configDir.exists()) configDir.mkdirs();
        this.saveFile = new File(configDir, "macros1.dat");
    }

    @Override
    public void init(Screen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x; this.y = y; this.width = width; this.height = height;
        loadContent();
    }

    @Override
    public List<Button> getButtons(int x, int y, int width, int height) {
        tabButtons.clear();
        tabButtons.add(new Button(
            x + width - 85, y + height - 25, 80, 20,
            new StringTextComponent("Execute"),
            btn -> executeCommands()
        ));
        return tabButtons;
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float pt, int x, int y, int w, int h) {
        AbstractGui.fill(ms, x, y, x + w, y + h, 0xFF252525);
        drawBorder(ms, x, y, w, h, 0xFF444444);
        
        long now = System.currentTimeMillis();
        if (now - lastCursorToggle > 500) {
            cursorVisible = !cursorVisible;
            lastCursorToggle = now;
        }
        
        Minecraft mc = Minecraft.getInstance();
        int lineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int visibleLines = h / lineHeight;
        int visibleCols = w / 6;
        
        for (int i = 0; i < visibleLines; i++) {
            int lineIdx = scrollY + i;
            if (lineIdx >= lines.size()) break;
            
            String line = lines.get(lineIdx);
            String display = line.substring(Math.min(scrollX, line.length()));
            if (display.length() > visibleCols) display = display.substring(0, visibleCols);
            
            int drawY = y + i * lineHeight;
            mc.fontRenderer.drawString(ms, display, (float)(x + 2), (float)drawY, 0xFFCCCCCC);
            
            if (lineIdx == cursorLine && cursorVisible) {
                int cx = x + 2 + (cursorCol - scrollX) * 6;
                if (cx >= x + 2 && cx < x + w) {
                    AbstractGui.fill(ms, cx, drawY, cx + 1, drawY + lineHeight, 0xFFFFFFFF);
                }
            }
        }
    }

    // FIX: обработка клика мыши для позиционирования курсора
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int contentX, int contentY, int contentW, int contentH) {
        if (mouseX < contentX || mouseX >= contentX + contentW || 
            mouseY < contentY || mouseY >= contentY + contentH) {
            return false;
        }
        
        Minecraft mc = Minecraft.getInstance();
        int lineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int charWidth = 6; // approximate
        
        // Вычисляем линию
        int clickedLine = scrollY + (int)((mouseY - contentY) / lineHeight);
        if (clickedLine < 0) clickedLine = 0;
        if (clickedLine >= lines.size()) clickedLine = lines.size() - 1;
        
        // Вычисляем колонку
        String line = lines.get(clickedLine);
        int clickedCol = scrollX + (int)((mouseX - contentX - 2) / charWidth);
        if (clickedCol < 0) clickedCol = 0;
        if (clickedCol > line.length()) clickedCol = line.length();
        
        cursorLine = clickedLine;
        cursorCol = clickedCol;
        scrollCursor();
        saveContent();
        return true;
    }

    private void drawBorder(MatrixStack ms, int x, int y, int w, int h, int color) {
        AbstractGui.fill(ms, x, y, x + w, y + 1, color);
        AbstractGui.fill(ms, x, y + h - 1, x + w, y + h, color);
        AbstractGui.fill(ms, x, y, x + 1, y + h, color);
        AbstractGui.fill(ms, x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP && cursorLine > 0) { cursorLine--; adjustCol(); saveContent(); return true; }
        if (keyCode == GLFW.GLFW_KEY_DOWN && cursorLine < lines.size() - 1) { cursorLine++; adjustCol(); saveContent(); return true; }
        if (keyCode == GLFW.GLFW_KEY_LEFT && cursorCol > 0) { cursorCol--; scrollCursor(); saveContent(); return true; }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            String line = lines.get(cursorLine);
            if (cursorCol < line.length()) { cursorCol++; scrollCursor(); saveContent(); return true; }
            else if (cursorLine < lines.size() - 1) { cursorLine++; cursorCol = 0; adjustCol(); saveContent(); return true; }
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            String line = lines.get(cursorLine);
            lines.set(cursorLine, line.substring(0, cursorCol));
            lines.add(cursorLine + 1, line.substring(cursorCol));
            cursorLine++; cursorCol = 0; scrollCursor(); saveContent(); return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (cursorCol > 0) {
                String line = lines.get(cursorLine);
                lines.set(cursorLine, line.substring(0, cursorCol - 1) + line.substring(cursorCol));
                cursorCol--; scrollCursor(); saveContent(); return true;
            } else if (cursorLine > 0) {
                String prev = lines.get(cursorLine - 1);
                String curr = lines.get(cursorLine);
                cursorCol = prev.length();
                lines.set(cursorLine - 1, prev + curr);
                lines.remove(cursorLine);
                cursorLine--; scrollCursor(); saveContent(); return true;
            }
        }
        boolean ctrl = (modifiers & 2) != 0;
        if (ctrl) {
            if (keyCode == GLFW.GLFW_KEY_C) { copySelection(); return true; }
            if (keyCode == GLFW.GLFW_KEY_V) { pasteClipboard(); saveContent(); return true; }
            if (keyCode == GLFW.GLFW_KEY_X) { cutSelection(); saveContent(); return true; }
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (codePoint < 32) return false;
        String line = lines.get(cursorLine);
        lines.set(cursorLine, line.substring(0, cursorCol) + codePoint + line.substring(cursorCol));
        cursorCol++;
        scrollCursor();
        saveContent();
        return true;
    }
    
    private void scrollCursor() {
        Minecraft mc = Minecraft.getInstance();
        int lineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int visibleLines = height / lineHeight;
        int visibleCols = width / 6;
        while (cursorLine - scrollY >= visibleLines) scrollY++;
        while (cursorLine < scrollY) scrollY = Math.max(0, scrollY - 1);
        while (cursorCol - scrollX >= visibleCols) scrollX++;
        while (cursorCol < scrollX) scrollX = Math.max(0, scrollX - 1);
    }
    
    private void adjustCol() {
        String line = lines.get(cursorLine);
        if (cursorCol > line.length()) cursorCol = line.length();
    }

    private void executeCommands() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;
        for (String line : lines) {
            String cmd = line.trim();
            if (!cmd.isEmpty() && cmd.startsWith("/")) {
                mc.player.sendChatMessage(cmd);
            }
        }
    }
    
    private void saveContent() { VSFileUtil.saveString(saveFile, String.join("\n", lines)); }
    private void loadContent() {
        String data = VSFileUtil.loadString(saveFile);
        if (data != null) {
            lines.clear();
            Collections.addAll(lines, data.split("\n", -1));
            if (lines.isEmpty()) lines.add("");
        }
    }
    
    private void copySelection() { if (cursorLine < lines.size()) clipboard = lines.get(cursorLine); }
    private void pasteClipboard() {
        if (!clipboard.isEmpty()) {
            String line = lines.get(cursorLine);
            lines.set(cursorLine, line.substring(0, cursorCol) + clipboard + line.substring(cursorCol));
            cursorCol += clipboard.length();
        }
    }
    private void cutSelection() { copySelection(); }

    @Override public void onShow() {}
    @Override public void onHide() {}
}
