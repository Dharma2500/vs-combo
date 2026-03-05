package com.vs.vscombo.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
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
    private static final int EXECUTE_BUTTON_HEIGHT = 30;
    
    private TabManager tabManager;
    private int panelX, panelY, panelW, panelH;
    private int contentX, contentY, contentW, contentH;
    private static boolean suppressNextChar = false;
    
    private final List<Button> tabButtons = new ArrayList<>();

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
        contentH = panelH - 60 - EXECUTE_BUTTON_HEIGHT;
        
        this.tabManager.init(panelX, panelY, panelW, panelH);
        
        // FIX: All 5 sidebar buttons
        int buttonY = panelY + 25;
        int buttonSpacing = 25;
        
        this.addButton(new Button(
            panelX + 5, buttonY, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#1"),
            btn -> tabManager.switchTab("macros1")
        ));
        
        this.addButton(new Button(
            panelX + 5, buttonY + buttonSpacing, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#2"),
            btn -> tabManager.switchTab("macros2")
        ));
        
        this.addButton(new Button(
            panelX + 5, buttonY + buttonSpacing * 2, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#3"),
            btn -> tabManager.switchTab("macros3")
        ));
        
        this.addButton(new Button(
            panelX + 5, buttonY + buttonSpacing * 3, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#4"),
            btn -> tabManager.switchTab("macros4")
        ));
        
        this.addButton(new Button(
            panelX + 5, buttonY + buttonSpacing * 4, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#5"),
            btn -> tabManager.switchTab("macros5")
        ));
        
        initTabButtons();
    }
    
    private void initTabButtons() {
        for (Button btn : tabButtons) {
            this.children.remove(btn);
            this.buttons.remove(btn);
        }
        tabButtons.clear();
        
        if (this.tabManager.getActiveTab() != null) {
            List<Button> newButtons = this.tabManager.getActiveTab().getButtons(
                    contentX, contentY + contentH, contentW, EXECUTE_BUTTON_HEIGHT);
            for (Button btn : newButtons) {
                this.addButton(btn);
                tabButtons.add(btn);
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        AbstractGui.fill(matrixStack, 0, 0, this.width, this.height, (BG_ALPHA << 24) | 0x000000);
        
        AbstractGui.fill(matrixStack, panelX, panelY, panelX + panelW, panelY + panelH, PANEL_COLOR);
        
        AbstractGui.fill(matrixStack, panelX, panelY, panelX + panelW, panelY + 1, 0xFF555555);
        AbstractGui.fill(matrixStack, panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, 0xFF555555);
        
        int separatorY = panelY + panelH - EXECUTE_BUTTON_HEIGHT - 5;
        AbstractGui.fill(matrixStack, panelX, separatorY, panelX + panelW, separatorY + 1, 0xFF555555);
        
        this.font.drawString(matrixStack, "Created by Vitaly_Sokolov", 
                (float)(panelX + 10), (float)(panelY + 8), TEXT_COLOR);
        
        if (this.tabManager.getActiveTab() != null) {
            this.tabManager.getActiveTab().render(matrixStack, mouseX, mouseY, partialTicks, 
                    contentX, contentY, contentW, contentH);
        }
        
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
