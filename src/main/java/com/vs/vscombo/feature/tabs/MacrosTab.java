package com.vs.vscombo.feature.tabs;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.vs.vscombo.VSBaseMod;
import com.vs.vscombo.core.gui.IVSTab;
import com.vs.vscombo.core.gui.VSMainWindow;
import com.vs.vscombo.util.VSFileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.Platform;

import java.io.File;
import java.util.*;

public class MacrosTab implements IVSTab {
    
    protected final String tabId;
    protected final File saveFile;
    protected Screen parent;
    protected int x, y, width, height;
    
    protected final List<String> lines = new ArrayList<>(Collections.singletonList(""));
    protected int cursorLine = 0, cursorCol = 0;
    protected boolean cursorVisible = true;
    protected long lastCursorToggle = 0;
    protected int scrollX = 0, scrollY = 0;
    
    protected static String clipboard = "";
    protected final List<Button> tabButtons = new ArrayList<>();

    public MacrosTab(String tabId) {
        this.tabId = tabId;
        File configDir = new File(Minecraft.getInstance().gameDir, "config/vscombo");
        if (!configDir.exists()) configDir.mkdirs();
        this.saveFile = new File(configDir, "macros" + tabId + ".dat");
    }

    @Override
    public void init(Screen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x; 
        this.y = y; 
        this.width = width; 
        this.height = height;
        loadContent();
    }

    @Override
    public List<Button> getButtons(int x, int y, int width, int height) {
        tabButtons.clear();
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int contentX, int contentY, int contentW, int contentH) {
        if (mouseX < contentX || mouseX >= contentX + contentW || 
            mouseY < contentY || mouseY >= contentY + contentH) {
            return false;
        }
        
        Minecraft mc = Minecraft.getInstance();
        int lineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int charWidth = 6;
        
        int clickedLine = scrollY + (int)((mouseY - contentY) / lineHeight);
        if (clickedLine < 0) clickedLine = 0;
        if (clickedLine >= lines.size()) clickedLine = lines.size() - 1;
        
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

    protected void drawBorder(MatrixStack ms, int x, int y, int w, int h, int color) {
        AbstractGui.fill(ms, x, y, x + w, y + 1, color);
        AbstractGui.fill(ms, x, y + h - 1, x + w, y + h, color);
        AbstractGui.fill(ms, x, y, x + 1, y + h, color);
        AbstractGui.fill(ms, x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean ctrl = (modifiers & 2) != 0;
        
        // FIX: Ctrl+V для вставки из буфера обмена Windows
        if (ctrl && keyCode == GLFW.GLFW_KEY_V) {
            pasteFromClipboard();
            saveContent();
            return true;
        }
        
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
        if (ctrl) {
            if (keyCode == GLFW.GLFW_KEY_C) { copySelection(); return true; }
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
    
    protected void scrollCursor() {
        Minecraft mc = Minecraft.getInstance();
        int lineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int visibleLines = height / lineHeight;
        int visibleCols = width / 6;
        while (cursorLine - scrollY >= visibleLines) scrollY++;
        while (cursorLine < scrollY) scrollY = Math.max(0, scrollY - 1);
        while (cursorCol - scrollX >= visibleCols) scrollX++;
        while (cursorCol < scrollX) scrollX = Math.max(0, scrollX - 1);
    }
    
    protected void adjustCol() {
        String line = lines.get(cursorLine);
        if (cursorCol > line.length()) cursorCol = line.length();
    }

    public void executeWithSettings() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            VSBaseMod.LOGGER.warn("Cannot execute: player is null");
            return;
        }
        if (mc.getConnection() == null) {
            VSBaseMod.LOGGER.warn("Cannot execute: connection is null");
            return;
        }
        
        int delay = VSMainWindow.lineDelay;
        int timer = VSMainWindow.executionTimer;
        
        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();
                int sentCount = 0;
                
                for (String line : lines) {
                    // Check timer
                    if (timer > 0) {
                        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                        if (elapsed >= timer) {
                            VSBaseMod.LOGGER.info("Timer reached {} seconds, stopping execution", timer);
                            break;
                        }
                    }
                    
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) continue;
                    
                    String cmd = trimmed;
                    if (!cmd.startsWith("/")) {
                        cmd = "/" + cmd;
                    }
                    
                    mc.execute(() -> mc.player.sendChatMessage(cmd));
                    sentCount++;
                    
                    Thread.sleep(delay);
                }
                
                VSBaseMod.LOGGER.info("Executed {} commands from Macros#{}", sentCount, tabId);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                VSBaseMod.LOGGER.error("Macro execution interrupted");
            }
        }, "MacroExecutor-" + tabId).start();
    }
    
    // FIX: Вставка из буфера обмена Windows
    private void pasteFromClipboard() {
        try {
            String systemClipboard = GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle());
            if (systemClipboard != null && !systemClipboard.isEmpty()) {
                String[] linesToPaste = systemClipboard.split("\n", -1);
                
                String currentLine = lines.get(cursorLine);
                String beforeCursor = currentLine.substring(0, cursorCol);
                String afterCursor = currentLine.substring(cursorCol);
                
                lines.set(cursorLine, beforeCursor + linesToPaste[0]);
                cursorCol += linesToPaste[0].length();
                
                for (int i = 1; i < linesToPaste.length; i++) {
                    lines.add(cursorLine + i, linesToPaste[i]);
                }
                
                if (linesToPaste.length > 0) {
                    String lastLine = lines.get(cursorLine + linesToPaste.length - 1);
                    lines.set(cursorLine + linesToPaste.length - 1, lastLine + afterCursor);
                    cursorLine += linesToPaste.length - 1;
                    cursorCol = lines.get(cursorLine).length() - afterCursor.length();
                }
                
                scrollCursor();
                VSBaseMod.LOGGER.info("Pasted {} lines from clipboard", linesToPaste.length);
            }
        } catch (Exception e) {
            VSBaseMod.LOGGER.error("Failed to paste from clipboard", e);
        }
    }
    
    protected void saveContent() { 
        VSFileUtil.saveString(saveFile, String.join("\n", lines)); 
    }
    
    protected void loadContent() {
        String data = VSFileUtil.loadString(saveFile);
        if (data != null) {
            lines.clear();
            Collections.addAll(lines, data.split("\n", -1));
            if (lines.isEmpty()) lines.add("");
        }
    }
    
    protected void copySelection() { 
        if (cursorLine < lines.size()) {
            clipboard = lines.get(cursorLine);
            GLFW.glfwSetClipboardString(Minecraft.getInstance().getMainWindow().getHandle(), clipboard);
        }
    }
    
    protected void cutSelection() { 
        copySelection();
        lines.set(cursorLine, "");
        cursorCol = 0;
    }

    @Override 
    public void onShow() {}
    
    @Override 
    public void onHide() {}
}
