package com.vs.vscombo.feature.tabs;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.vs.vscombo.VSBaseMod;
import com.vs.vscombo.core.gui.IVSTab;
import com.vs.vscombo.util.VSFileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MacrosTab implements IVSTab {
    
    protected final String tabId;
    protected final File saveFile;
    protected final File settingsFile;
    protected Screen parent;
    protected int x, y, width, height;
    
    protected final List<String> lines = new ArrayList<>(Collections.singletonList(""));
    protected int cursorLine = 0, cursorCol = 0;
    protected boolean cursorVisible = true;
    protected long lastCursorToggle = 0;
    protected int scrollX = 0, scrollY = 0;
    
    protected static String clipboard = "";
    protected final List<Button> tabButtons = new ArrayList<>();
    
    // FIX: Per-tab settings
    protected int delay = 50;
    protected int loopCount = 1;
    protected boolean isRunning = false;
    protected AtomicBoolean shouldStop = new AtomicBoolean(false);
    protected Thread executionThread = null;
    
    // UI Widgets
    protected TextFieldWidget delayField;
    protected TextFieldWidget loopField;
    protected Button stopButton;
    protected Button executeButton;

    public MacrosTab(String tabId) {
        this.tabId = tabId;
        File configDir = new File(Minecraft.getInstance().gameDir, "config/vscombo");
        if (!configDir.exists()) configDir.mkdirs();
        this.saveFile = new File(configDir, "macros" + tabId + ".dat");
        this.settingsFile = new File(configDir, "macros" + tabId + "_settings.dat");
        loadSettings();
    }

    @Override
    public void init(Screen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x; 
        this.y = y; 
        this.width = width; 
        this.height = height;
        loadContent();
        
        int bottomY = y + height - 30;
        
        // Delay field
        delayField = new TextFieldWidget(Minecraft.getInstance().fontRenderer, 
            x + 5, bottomY + 5, 60, 18, new StringTextComponent("Delay"));
        delayField.setText(String.valueOf(delay));
        delayField.setTextColor(0xFFFFFF);
        delayField.setFilter(s -> s.matches("\\d*"));
        parent.addChild(delayField);
        
        // Loop field
        loopField = new TextFieldWidget(Minecraft.getInstance().fontRenderer,
            x + 75, bottomY + 5, 60, 18, new StringTextComponent("Loop"));
        loopField.setText(String.valueOf(loopCount));
        loopField.setTextColor(0xFFFFFF);
        loopField.setFilter(s -> s.matches("\\d*"));
        parent.addChild(loopField);
        
        // Stop button
        stopButton = new Button(x + 145, bottomY, 70, 20,
            new StringTextComponent("Stop"), btn -> stopExecution());
        stopButton.active = false;
        parent.addButton(stopButton);
        
        // Execute button
        executeButton = new Button(x + width - 85, bottomY, 80, 20,
            new StringTextComponent("Execute"), btn -> startExecution());
        parent.addButton(executeButton);
    }
    
    private void startExecution() {
        if (isRunning) return;
        
        try {
            delay = Integer.parseInt(delayField.getText());
            loopCount = Integer.parseInt(loopField.getText());
        } catch (NumberFormatException e) {
            delay = 50;
            loopCount = 1;
        }
        
        saveSettings();
        isRunning = true;
        shouldStop.set(false);
        stopButton.active = true;
        executeButton.active = false;
        
        executionThread = new Thread(() -> {
            try {
                executeWithLoop();
            } finally {
                isRunning = false;
                stopButton.active = false;
                executeButton.active = true;
            }
        }, "MacroExecutor-" + tabId);
        executionThread.start();
    }
    
    protected void stopExecution() {
        shouldStop.set(true);
        if (executionThread != null) {
            executionThread.interrupt();
        }
    }
    
    protected void executeWithLoop() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            VSBaseMod.LOGGER.warn("Cannot execute: player or connection is null");
            return;
        }
        
        for (int loop = 0; loop < loopCount && !shouldStop.get(); loop++) {
            if (shouldStop.get()) break;
            
            for (String line : lines) {
                if (shouldStop.get()) break;
                
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                
                String cmd = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
                final String finalCmd = cmd;
                
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.sendChatMessage(finalCmd);
                    }
                });
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        VSBaseMod.LOGGER.info("Macro {} completed {} loop(s)", tabId, loopCount);
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
            
            // FIX: Корректное отображение курсора
            if (lineIdx == cursorLine && cursorVisible) {
                int cursorPixelX = x + 2 + ((cursorCol - scrollX) * 6);
                if (cursorPixelX >= x + 2 && cursorPixelX < x + w) {
                    AbstractGui.fill(ms, cursorPixelX, drawY, cursorPixelX + 1, drawY + lineHeight, 0xFFFFFFFF);
                }
            }
        }
        
        // Render widgets
        if (delayField != null) delayField.render(ms, mouseX, mouseY, pt);
        if (loopField != null) loopField.render(ms, mouseX, mouseY, pt);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int contentX, int contentY, int contentW, int contentH) {
        if (delayField != null && delayField.mouseClicked(mouseX, mouseY, button)) return true;
        if (loopField != null && loopField.mouseClicked(mouseX, mouseY, button)) return true;
        
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
        if (delayField != null && delayField.keyPressed(keyCode, scanCode, modifiers)) {
            updateSettingsFromFields();
            return true;
        }
        if (loopField != null && loopField.keyPressed(keyCode, scanCode, modifiers)) {
            updateSettingsFromFields();
            return true;
        }
        
        boolean ctrl = (modifiers & 2) != 0;
        
        if (ctrl && keyCode == GLFW.GLFW_KEY_V) {
            pasteFromClipboard();
            saveContent();
            return true;
        }
        
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
            if (cursorCol < line.length()) { 
                cursorCol++; scrollCursor(); saveContent(); return true; 
            } else if (cursorLine < lines.size() - 1) { 
                cursorLine++; cursorCol = 0; adjustCol(); saveContent(); return true; 
            }
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
    
    private void updateSettingsFromFields() {
        try {
            delay = Integer.parseInt(delayField.getText());
            if (delay < 0) delay = 0;
            if (delay > 10000) delay = 10000;
        } catch (NumberFormatException e) { delay = 50; }
        
        try {
            loopCount = Integer.parseInt(loopField.getText());
            if (loopCount < 1) loopCount = 1;
            if (loopCount > 1000) loopCount = 1000;
        } catch (NumberFormatException e) { loopCount = 1; }
        
        saveSettings();
    }
    
    protected void saveSettings() {
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter(settingsFile);
            writer.println(delay);
            writer.println(loopCount);
            writer.close();
        } catch (Exception e) {
            VSBaseMod.LOGGER.error("Failed to save settings for tab {}", tabId, e);
        }
    }
    
    protected void loadSettings() {
        try {
            if (settingsFile.exists()) {
                Scanner scanner = new Scanner(settingsFile);
                if (scanner.hasNextInt()) delay = scanner.nextInt();
                if (scanner.hasNextInt()) loopCount = scanner.nextInt();
                scanner.close();
            }
        } catch (Exception e) {
            VSBaseMod.LOGGER.error("Failed to load settings for tab {}", tabId, e);
            delay = 50;
            loopCount = 1;
        }
    }
    
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
    public void onHide() {
        // Cleanup widgets
        if (parent != null) {
            if (delayField != null) parent.removeChild(delayField);
            if (loopField != null) parent.removeChild(loopField);
        }
    }
}
