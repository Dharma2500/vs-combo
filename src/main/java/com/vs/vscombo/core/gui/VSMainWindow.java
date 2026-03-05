package com.vs.vscombo.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import com.vs.vscombo.feature.tabs.MacrosTab;

import java.util.ArrayList;
import java.util.List;

public class VSMainWindow extends Screen {
    
    private static VSMainWindow instance;
    private static boolean isOpen = false;
    
    private static final float WIDTH_RATIO = 0.55f;
    private static final float HEIGHT_RATIO = 0.60f;
    private static final int BG_ALPHA = 200;
    private static final int PANEL_COLOR = 0xFF1A1A1A;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int SIDEBAR_WIDTH = 120;
    private static final int BOTTOM_SECTION_HEIGHT = 60;
    
    private TabManager tabManager;
    private int panelX, panelY, panelW, panelH;
    private int contentX, contentY, contentW, contentH;
    private static boolean suppressNextChar = false;
    
    private final List<Button> tabButtons = new ArrayList<>();
    private TextFieldWidget delayField;
    private TextFieldWidget loopField;
    private Button stopButton;
    private Button executeButton;
    
    private int currentDelay = 50;
    private int currentLoop = 1;

    private VSMainWindow() {
        super(new StringTextComponent("Vitaly_Sokolov Universe"));
        this.tabManager = new TabManager(this);
    }

    public static void toggle() {
        Minecraft mc = Minecraft.getInstance();
        if (isOpen && mc.currentScreen instanceof VSMainWindow) {
            mc.displayGuiScreen(null);
            isOpen = false;
        } else if (!isOpen) {
            instance = new VSMainWindow();
            mc.displayGuiScreen(instance);
            isOpen = true;
            suppressNextChar = true;
        }
    }

    @Override
    protected void init() {
        panelW = (int)(this.width * WIDTH_RATIO);
        panelH = (int)(this.height * HEIGHT_RATIO);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
        
        contentX = panelX + SIDEBAR_WIDTH;
        contentY = panelY + 25;
        contentW = panelW - SIDEBAR_WIDTH - 10;
        contentH = panelH - 60 - BOTTOM_SECTION_HEIGHT;
        
        this.tabManager.init(panelX, panelY, panelW, panelH);
        
        // Sidebar buttons
        int buttonY = panelY + 25;
        int buttonSpacing = 25;
        this.addButton(new Button(panelX + 5, buttonY, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#1"), btn -> switchTab("macros1")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#2"), btn -> switchTab("macros2")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing * 2, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#3"), btn -> switchTab("macros3")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing * 3, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#4"), btn -> switchTab("macros4")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing * 4, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#5"), btn -> switchTab("macros5")));
        
        initBottomSection();
        initTabButtons();
        syncFieldsWithTab();
    }
    
    private void switchTab(String id) {
        tabManager.switchTab(id);
        syncFieldsWithTab();
    }
    
    private void syncFieldsWithTab() {
        if (tabManager.getActiveTab() instanceof MacrosTab) {
            MacrosTab tab = (MacrosTab) tabManager.getActiveTab();
            currentDelay = tab.getDelay();
            currentLoop = tab.getLoopCount();
            if (delayField != null) delayField.setText(String.valueOf(currentDelay));
            if (loopField != null) loopField.setText(String.valueOf(currentLoop));
        }
    }
    
    private void initBottomSection() {
        int bottomY = panelY + panelH - BOTTOM_SECTION_HEIGHT + 10;
        
        // Delay label + field
        this.font.drawString(new MatrixStack(), "Delay:", 
            (float)(contentX + 5), (float)(bottomY + 5), 0xFFAAAAAA);
        
        delayField = new TextFieldWidget(this.font, contentX + 50, bottomY, 50, 18,
            new StringTextComponent("Delay"));
        delayField.setText(String.valueOf(currentDelay));
        delayField.setTextColor(0xFFFFFF);
        // FIX: В 1.16.5 нет setFilter, используем setMaxStringLength + ручную валидацию
        delayField.setMaxStringLength(5);
        this.children.add(delayField);
        
        // Loop label + field
        this.font.drawString(new MatrixStack(), "Loop:", 
            (float)(contentX + 110), (float)(bottomY + 5), 0xFFAAAAAA);
        
        loopField = new TextFieldWidget(this.font, contentX + 155, bottomY, 50, 18,
            new StringTextComponent("Loop"));
        loopField.setText(String.valueOf(currentLoop));
        loopField.setTextColor(0xFFFFFF);
        loopField.setMaxStringLength(4);
        this.children.add(loopField);
        
        // Stop button
        stopButton = new Button(contentX + 215, bottomY, 60, 20,
            new StringTextComponent("Stop"), btn -> stopMacro());
        stopButton.active = false;
        this.addButton(stopButton);
        
        // Execute button
        executeButton = new Button(panelX + panelW - 85, bottomY, 75, 20,
            new StringTextComponent("Execute"), btn -> executeMacro());
        this.addButton(executeButton);
    }
    
    private void stopMacro() {
        if (tabManager.getActiveTab() instanceof MacrosTab) {
            ((MacrosTab) tabManager.getActiveTab()).stopExecution();
            stopButton.active = false;
            executeButton.active = true;
        }
    }
    
    private void executeMacro() {
        if (!(tabManager.getActiveTab() instanceof MacrosTab)) return;
        MacrosTab tab = (MacrosTab) tabManager.getActiveTab();
        
        // Parse and validate input
        try {
            currentDelay = Integer.parseInt(delayField.getText());
            if (currentDelay < 0) currentDelay = 0;
            if (currentDelay > 10000) currentDelay = 10000;
        } catch (NumberFormatException e) { currentDelay = 50; }
        
        try {
            currentLoop = Integer.parseInt(loopField.getText());
            if (currentLoop < 1) currentLoop = 1;
            if (currentLoop > 1000) currentLoop = 1000;
        } catch (NumberFormatException e) { currentLoop = 1; }
        
        // Update tab settings and execute
        tab.updateSettings(currentDelay, currentLoop);
        tab.executeWithSettings(currentDelay, currentLoop);
        
        stopButton.active = true;
        executeButton.active = false;
    }
    
    private void initTabButtons() {
        for (Button btn : tabButtons) {
            this.children.remove(btn);
            this.buttons.remove(btn);
        }
        tabButtons.clear();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        AbstractGui.fill(matrixStack, 0, 0, this.width, this.height, (BG_ALPHA << 24) | 0x000000);
        
        AbstractGui.fill(matrixStack, panelX, panelY, panelX + panelW, panelY + panelH, PANEL_COLOR);
        
        AbstractGui.fill(matrixStack, panelX, panelY, panelX + panelW, panelY + 1, 0xFF555555);
        AbstractGui.fill(matrixStack, panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, 0xFF555555);
        
        int bottomSectionY = panelY + panelH - BOTTOM_SECTION_HEIGHT;
        AbstractGui.fill(matrixStack, panelX, bottomSectionY, panelX + panelW, bottomSectionY + 1, 0xFF555555);
        
        this.font.drawString(matrixStack, "Created by Vitaly_Sokolov", 
                (float)(panelX + 10), (float)(panelY + 8), TEXT_COLOR);
        
        if (this.tabManager.getActiveTab() != null) {
            this.tabManager.getActiveTab().render(matrixStack, mouseX, mouseY, partialTicks, 
                    contentX, contentY, contentW, contentH);
        }
        
        if (delayField != null) delayField.render(matrixStack, mouseX, mouseY, partialTicks);
        if (loopField != null) loopField.render(matrixStack, mouseX, mouseY, partialTicks);
        
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (suppressNextChar && keyCode == GLFW.GLFW_KEY_X) {
            suppressNextChar = false;
            return true;
        }
        
        if (delayField != null && delayField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (loopField != null && loopField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        
        if (this.tabManager.getActiveTab() != null) {
            if (this.tabManager.getActiveTab().keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (suppressNextChar) {
            suppressNextChar = false;
            return true;
        }
        
        // Validate numeric input for fields
        if (delayField != null && delayField.isFocused()) {
            if (codePoint >= '0' && codePoint <= '9') {
                return delayField.charTyped(codePoint, modifiers);
            }
            return true; // Block non-numeric
        }
        if (loopField != null && loopField.isFocused()) {
            if (codePoint >= '0' && codePoint <= '9') {
                return loopField.charTyped(codePoint, modifiers);
            }
            return true;
        }
        
        if (this.tabManager.getActiveTab() != null) {
            if (this.tabManager.getActiveTab().charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (delayField != null && delayField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (loopField != null && loopField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        
        if (this.tabManager.getActiveTab() != null) {
            if (this.tabManager.getActiveTab().mouseClicked(mouseX, mouseY, button, 
                    contentX, contentY, contentW, contentH)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void onClose() {
        isOpen = false;
        // Cleanup widgets
        if (delayField != null) this.children.remove(delayField);
        if (loopField != null) this.children.remove(loopField);
        super.onClose();
    }
    
    public void reinitTabButtons() {
        initTabButtons();
    }
    
    public int getContentX() { return contentX; }
    public int getContentY() { return contentY; }
    public int getContentW() { return contentW; }
    public int getContentH() { return contentH; }
}
