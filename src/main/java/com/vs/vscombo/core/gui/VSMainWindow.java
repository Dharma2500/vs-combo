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
    private TextFieldWidget timerField;
    
    public static int lineDelay = 50;
    public static int executionTimer = 0;

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
        
        int buttonY = panelY + 25;
        int buttonSpacing = 25;
        
        this.addButton(new Button(panelX + 5, buttonY, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#1"), btn -> tabManager.switchTab("macros1")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#2"), btn -> tabManager.switchTab("macros2")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing * 2, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#3"), btn -> tabManager.switchTab("macros3")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing * 3, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#4"), btn -> tabManager.switchTab("macros4")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing * 4, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#5"), btn -> tabManager.switchTab("macros5")));
        
        initBottomSection();
        initTabButtons();
    }
    
    private void initBottomSection() {
        int bottomY = panelY + panelH - BOTTOM_SECTION_HEIGHT + 10;
        
        // Delay label
        this.font.drawString(new MatrixStack(), "Delay (ms):", 
            (float)(contentX + 5), (float)(bottomY + 5), 0xFFAAAAAA);
        
        // Delay input field
        delayField = new TextFieldWidget(this.font, contentX + 70, bottomY, 60, 18,
            new StringTextComponent("Delay"));
        delayField.setText(String.valueOf(lineDelay));
        delayField.setTextColor(0xFFFFFF);
        delayField.setResponder(this::onDelayChanged);
        this.addChild(delayField);
        
        // Timer label
        this.font.drawString(new MatrixStack(), "Timer (sec):", 
            (float)(contentX + 145), (float)(bottomY + 5), 0xFFAAAAAA);
        
        // Timer input field
        timerField = new TextFieldWidget(this.font, contentX + 225, bottomY, 60, 18,
            new StringTextComponent("Timer"));
        timerField.setText(String.valueOf(executionTimer));
        timerField.setTextColor(0xFFFFFF);
        timerField.setResponder(this::onTimerChanged);
        this.addChild(timerField);
        
        // Execute button in bottom section
        this.addButton(new Button(
            panelX + panelW - 95, bottomY,
            80, 20,
            new StringTextComponent("Execute"),
            btn -> executeActiveTab()
        ));
    }
    
    private void onDelayChanged(String value) {
        try {
            lineDelay = Integer.parseInt(value);
            if (lineDelay < 0) lineDelay = 0;
            if (lineDelay > 5000) lineDelay = 5000;
        } catch (NumberFormatException e) {
            lineDelay = 50;
        }
    }
    
    private void onTimerChanged(String value) {
        try {
            executionTimer = Integer.parseInt(value);
            if (executionTimer < 0) executionTimer = 0;
            if (executionTimer > 3600) executionTimer = 3600;
        } catch (NumberFormatException e) {
            executionTimer = 0;
        }
    }
    
    private void executeActiveTab() {
        if (this.tabManager.getActiveTab() instanceof MacrosTab) {
            ((MacrosTab) this.tabManager.getActiveTab()).executeWithSettings();
        }
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
        
        // Bottom section separator
        int bottomSectionY = panelY + panelH - BOTTOM_SECTION_HEIGHT;
        AbstractGui.fill(matrixStack, panelX, bottomSectionY, panelX + panelW, bottomSectionY + 1, 0xFF555555);
        
        this.font.drawString(matrixStack, "Created by Vitaly_Sokolov", 
                (float)(panelX + 10), (float)(panelY + 8), TEXT_COLOR);
        
        if (this.tabManager.getActiveTab() != null) {
            this.tabManager.getActiveTab().render(matrixStack, mouseX, mouseY, partialTicks, 
                    contentX, contentY, contentW, contentH);
        }
        
        // Render input fields
        if (delayField != null) delayField.render(matrixStack, mouseX, mouseY, partialTicks);
        if (timerField != null) timerField.render(matrixStack, mouseX, mouseY, partialTicks);
        
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
        if (timerField != null && timerField.keyPressed(keyCode, scanCode, modifiers)) {
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
        
        if (delayField != null && delayField.charTyped(codePoint, modifiers)) {
            return true;
        }
        if (timerField != null && timerField.charTyped(codePoint, modifiers)) {
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
        if (timerField != null && timerField.mouseClicked(mouseX, mouseY, button)) {
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
