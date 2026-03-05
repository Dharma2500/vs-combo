package com.vs.vscombo.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

public class VSMainWindow extends Screen {
    
    private static VSMainWindow instance;
    private static boolean isOpen = false;
    
    private static final int BG_ALPHA = 200;
    private static final float SCREEN_RATIO = 1.0f;
    private static final int PANEL_COLOR = 0xFF1A1A1A;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    
    private TabManager tabManager;
    private int panelX, panelY, panelW, panelH;
    private static boolean suppressNextChar = false;

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
        int winW = this.width;
        int winH = this.height;
        panelW = (int)(winW * SCREEN_RATIO);
        panelH = (int)(winH * SCREEN_RATIO);
        panelX = (winW - panelW) / 2;
        panelY = (winH - panelH) / 2;
        
        this.tabManager.init(panelX, panelY, panelW, panelH);
        
        this.addButton(new Button(
            panelX + 5, panelY + 25, 100, 20, 
            new StringTextComponent("Macros#1"), 
            btn -> tabManager.switchTab("macros1")
        ));
        
        if (this.tabManager.getActiveTab() != null) {
            for (Button btn : this.tabManager.getActiveTab().getButtons(
                    panelX + 120, panelY + 25, panelW - 130, panelH - 35)) {
                this.addButton(btn);
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        AbstractGui.fill(matrixStack, 0, 0, this.width, this.height, (BG_ALPHA << 24) | 0x000000);
        
        AbstractGui.fill(matrixStack, panelX, panelY, panelX + panelW, panelY + panelH, PANEL_COLOR);
        drawBorder(matrixStack, panelX, panelY, panelW, panelH, 0xFF555555);
        
        this.font.drawString(matrixStack, "Created by Vitaly_Sokolov", 
                (float)(panelX + 10), (float)(panelY + 8), TEXT_COLOR);
        
        if (this.tabManager.getActiveTab() != null) {
            this.tabManager.getActiveTab().render(matrixStack, mouseX, mouseY, partialTicks, 
                    panelX + 120, panelY + 25, panelW - 130, panelH - 35);
        }
        
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    private void drawBorder(MatrixStack ms, int x, int y, int w, int h, int color) {
        AbstractGui.fill(ms, x, y, x + w, y + 1, color);
        AbstractGui.fill(ms, x, y + h - 1, x + w, y + h, color);
        AbstractGui.fill(ms, x, y, x + 1, y + h, color);
        AbstractGui.fill(ms, x + w - 1, y, x + w, y + h, color);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // FIX: только GLFW_KEY_X, без несуществующего GLFW_KEY_x
        if (suppressNextChar && keyCode == GLFW.GLFW_KEY_X) {
            suppressNextChar = false;
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
        if (this.tabManager.getActiveTab() != null) {
            if (this.tabManager.getActiveTab().charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.tabManager.getActiveTab() != null) {
            if (this.tabManager.getActiveTab().mouseClicked(mouseX, mouseY, button, 
                    panelX + 120, panelY + 25, panelW - 130, panelH - 35)) {
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
    
    public void reinitTabButtons(IVSTab tab, int x, int y, int w, int h) {
        for (Button btn : tab.getButtons(x, y, w, h)) {
            this.addButton(btn);
        }
    }
}
