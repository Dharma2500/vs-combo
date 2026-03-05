package com.vs.vscombo.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

public class VSMainWindow extends Screen {
    
    private static VSMainWindow instance;
    private static boolean isOpen = false;
    
    // Layout Constants
    private static final int BG_ALPHA = 180;
    private static final float SCREEN_RATIO = 0.25f;
    private static final int PANEL_COLOR = 0xFF1A1A1A; // Dark strict background
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    
    // Components
    private TabManager tabManager;

    private VSMainWindow() {
        super(new StringTextComponent("Vitaly_Sokolov Universe"));
        this.tabManager = new TabManager(this);
    }

    public static void toggle() {
        Minecraft mc = Minecraft.getInstance();
        if (isOpen && mc.screen instanceof VSMainWindow) {
            mc.displayGuiScreen(null);
            isOpen = false;
        } else if (!isOpen) {
            instance = new VSMainWindow();
            mc.displayGuiScreen(instance);
            isOpen = true;
        }
    }

    @Override
    protected void init() {
        // Calculate 25% screen dimensions, centered
        int winW = this.width;
        int winH = this.height;
        int panelW = (int)(winW * SCREEN_RATIO);
        int panelH = (int)(winH * SCREEN_RATIO);
        int posX = (winW - panelW) / 2;
        int posY = (winH - panelH) / 2;
        
        // Initialize Tab System (Left sidebar)
        this.tabManager.init(posX, posY, panelW, panelH);
        
        // Add Tab Buttons
        this.addButton(new Button(
            posX + 5, posY + 25, 100, 20, 
            new StringTextComponent("Macros#1"), 
            btn -> tabManager.switchTab("macros1")
        ));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // 1. Render Dark Overlay (Dim background)
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        fill(matrixStack, 0, 0, this.width, this.height, (BG_ALPHA << 24) | 0x000000);
        
        // 2. Render Main Panel
        int winW = this.width;
        int winH = this.height;
        int panelW = (int)(winW * SCREEN_RATIO);
        int panelH = (int)(winH * SCREEN_RATIO);
        int posX = (winW - panelW) / 2;
        int posY = (winH - panelH) / 2;
        
        // Panel Background
        fill(matrixStack, posX, posY, posX + panelW, posY + panelH, PANEL_COLOR);
        // Border
        drawHorizontalLine(matrixStack, posX, posX + panelW, posY, 0xFF555555);
        drawVerticalLine(matrixStack, posX, posY, posY + panelH, 0xFF555555);
        
        // 3. Render Header
        drawString(matrixStack, this.font, "Created by Vitaly_Sokolov", 
                posX + 10, posY + 8, TEXT_COLOR);
        
        // 4. Render Active Tab Content
        if (this.tabManager.getActiveTab() != null) {
            this.tabManager.getActiveTab().render(matrixStack, mouseX, mouseY, partialTicks, 
                    posX + 120, posY + 25, panelW - 130, panelH - 35);
        }
        
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Intercept input for active tab (e.g. Text Editor)
        if (this.tabManager.getActiveTab() != null) {
            if (this.tabManager.getActiveTab().keyPressed(keyCode, scanCode, modifiers)) {
                return true; // Consumed by tab
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.tabManager.getActiveTab() != null) {
            if (this.tabManager.getActiveTab().charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }
    
    @Override
    public void onClose() {
        isOpen = false;
        super.onClose();
    }
}
