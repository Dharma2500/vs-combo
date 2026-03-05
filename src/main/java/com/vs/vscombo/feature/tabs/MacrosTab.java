package com.vs.vscombo.feature.tabs;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.vs.vscombo.VSBaseMod;
import com.vs.vscombo.core.gui.IVSTab;
import com.vs.vscombo.util.VSFileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.*;

public class MacrosTab implements IVSTab {
    
    private Screen parent;
    private int x, y, width, height;
    
    // Editor State
    private final List<String> lines = new ArrayList<>(Collections.singletonList(""));
    private int cursorLine = 0, cursorCol = 0;
    private boolean cursorVisible = true;
    private long lastCursorToggle = 0;
    
    // Scroll State
    private int scrollX = 0, scrollY = 0;
    
    // Selection (Simple)
    private int selStart = -1, selEnd = -1;
    
    // Clipboard
    private static String clipboard = "";
    
    // Persistence
    private final File saveFile = new File(Minecraft.getInstance().gameDir, "config/vscombo/macros1.dat");

    @Override
    public void init(Screen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x; this.y = y; this.width = width; this.height = height;
        loadContent();
        
        // Execute Button (Bottom Right)
        parent.addButton(new Button(
            x + width - 85, y + height - 25, 80, 20,
            new StringTextComponent("Execute"),
            btn -> executeCommands()
        ));
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float pt, int x, int y, int w, int h) {
        // Background
        fill(ms, x, y, x + w, y + h, 0xFF252525);
        drawBorder(ms, x, y, x + w, y + h, 0xFF444444);
        
        // Cursor Blink Logic
        long now = System.currentTimeMillis();
        if (now - lastCursorToggle > 500) {
            cursorVisible = !cursorVisible;
            lastCursorToggle = now;
        }
        
        // Render Text
        Minecraft mc = Minecraft.getInstance();
        int lineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int visibleLines = h / lineHeight;
        int visibleCols = w / 6; // Approx char width
        
        for (int i = 0; i < visibleLines; i++) {
            int lineIdx = scrollY + i;
            if (lineIdx >= lines.size()) break;
            
            String line = lines.get(lineIdx);
            String display = line.substring(Math.min(scrollX, line.length()));
            if (display.length() > visibleCols) display = display.substring(0, visibleCols);
            
            int drawY = y + i * lineHeight;
            mc.fontRenderer.drawString(ms, display, x + 2, drawY, 0xFFCCCCCC);
            
            // Render Cursor
            if (lineIdx == cursorLine && cursorVisible) {
                int cx = x + 2 + (cursorCol - scrollX) * 6;
                if (cx >= x + 2 && cx < x + w) {
                    drawVerticalLine(ms, cx, drawY, drawY + lineHeight, 0xFFFFFFFF);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Navigation & Editing
        if (keyCode == GLFW.GLFW_KEY_UP && cursorLine > 0) {
            cursorLine--; adjustCol(); saveContent(); return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN && cursorLine < lines.size() - 1) {
            cursorLine++; adjustCol(); saveContent(); return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT && cursorCol > 0) {
            cursorCol--; scrollCursor(); saveContent(); return true;
        }
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
        // Clipboard Ctrl+C/V/X
        boolean ctrl = (modifiers & 2) != 0; // GLFW_MOD_CONTROL
        if (ctrl) {
            if (keyCode == GLFW.GLFW_KEY_C) { copySelection(); return true; }
            if (keyCode == GLFW.GLFW_KEY_V) { pasteClipboard(); saveContent(); return true; }
            if (keyCode == GLFW.GLFW_KEY_X) { cutSelection(); saveContent(); return true; }
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // Filter control chars
        if (codePoint < 32) return false;
        
        String line = lines.get(cursorLine);
        lines.set(cursorLine, line.substring(0, cursorCol) + codePoint + line.substring(cursorCol));
        cursorCol++;
        scrollCursor();
        saveContent(); // Auto-save per char
        return true;
    }
    
    private void scrollCursor() {
        // Auto-scroll logic to keep cursor visible
        // (Simplified for brevity)
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
    
    // --- Persistence ---
    private void saveContent() {
        VSFileUtil.saveString(saveFile, String.join("\n", lines));
    }
    
    private void loadContent() {
        String data = VSFileUtil.loadString(saveFile);
        if (data != null) {
            lines.clear();
            Collections.addAll(lines, data.split("\n"));
            if (lines.isEmpty()) lines.add("");
        }
    }
    
    // --- Clipboard Helpers ---
    private void copySelection() { /* Implement selection logic */ }
    private void pasteClipboard() {
        if (!clipboard.isEmpty()) {
            String line = lines.get(cursorLine);
            lines.set(cursorLine, line.substring(0, cursorCol) + clipboard + line.substring(cursorCol));
            cursorCol += clipboard.length();
        }
    }
    private void cutSelection() { copySelection(); /* remove selected */ }

    @Override public void onShow() {}
    @Override public void onHide() {}
}
