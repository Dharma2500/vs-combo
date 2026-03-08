package com.vs.vscombo.feature.tabs;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.vs.vscombo.VSBaseMod;
import com.vs.vscombo.core.gui.IVSTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class AntiChatTab implements IVSTab {
    
    protected Screen parent;
    protected int x, y, width, height;
    
    protected final List<Button> tabButtons = new ArrayList<>();
    protected boolean antiChatEnabled = false;

    public AntiChatTab() {}

    @Override
    public void init(Screen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public List<Button> getButtons(int x, int y, int width, int height) {
        tabButtons.clear();
        
        int buttonWidth = 100;
        int buttonHeight = 20;
        int spacing = 10;
        int startX = x + 10;
        int startY = y + 10;
        
        // Кнопка включения/выключения AntiChat
        tabButtons.add(new Button(
            startX, startY, buttonWidth, buttonHeight,
            new StringTextComponent(antiChatEnabled ? "ON" : "OFF"),
            btn -> toggleAntiChat()
        ));
        
        return tabButtons;
    }

    private void toggleAntiChat() {
        antiChatEnabled = !antiChatEnabled;
        VSBaseMod.LOGGER.info("AntiChat {}", antiChatEnabled ? "enabled" : "disabled");
        
        // Обновляем текст кнопки
        if (!tabButtons.isEmpty()) {
            tabButtons.get(0).setMessage(new StringTextComponent(antiChatEnabled ? "ON" : "OFF"));
        }
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float pt, int x, int y, int w, int h) {
        AbstractGui.fill(ms, x, y, x + w, y + h, 0xFF252525);
        drawBorder(ms, x, y, w, h, 0xFF444444);
        
        Minecraft mc = Minecraft.getInstance();
        
        int textStartY = y + 50;
        int textLineHeight = 15;
        
        mc.fontRenderer.drawString(ms, "AntiChat System", 
            (float)(x + 10), (float)(textStartY), 0xFFE0E0E0);
        
        if (antiChatEnabled) {
            mc.fontRenderer.drawString(ms, "Status: ACTIVE", 
                (float)(x + 10), (float)(textStartY + textLineHeight), 0xFF00FF00);
        } else {
            mc.fontRenderer.drawString(ms, "Status: INACTIVE", 
                (float)(x + 10), (float)(textStartY + textLineHeight), 0xFFAAAAAA);
        }
        
        mc.fontRenderer.drawString(ms, "Blocks chat messages", 
            (float)(x + 10), (float)(textStartY + textLineHeight * 3), 0xFFAAAAAA);
    }

    protected void drawBorder(MatrixStack ms, int x, int y, int w, int h, int color) {
        AbstractGui.fill(ms, x, y, x + w, y + 1, color);
        AbstractGui.fill(ms, x, y + h - 1, x + w, y + h, color);
        AbstractGui.fill(ms, x, y, x + 1, y + h, color);
        AbstractGui.fill(ms, x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, 
                                 int contentX, int contentY, int contentW, int contentH) {
        return false;
    }

    @Override
    public void onShow() {}

    @Override
    public void onHide() {}
    
    public boolean isAntiChatEnabled() { return antiChatEnabled; }
}
