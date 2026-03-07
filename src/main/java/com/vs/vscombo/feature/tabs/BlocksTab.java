package com.vs.vscombo.feature.tabs;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.vs.vscombo.VSBaseMod;
import com.vs.vscombo.client.BlockHighlightHandler;
import com.vs.vscombo.core.gui.IVSTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class BlocksTab implements IVSTab {
    
    protected Screen parent;
    protected int x, y, width, height;
    
    protected final List<Button> tabButtons = new ArrayList<>();
    protected int selectedButton = -1;

    public BlocksTab() {}

    @Override
    public void init(Screen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        if (BlockHighlightHandler.isEffectEnabled()) {
            selectedButton = getColorButtonIndex(BlockHighlightHandler.getEffectColor());
        } else {
            selectedButton = -1;
        }
    }

    @Override
    public List<Button> getButtons(int x, int y, int width, int height) {
        tabButtons.clear();
        
        int buttonWidth = 80;
        int buttonHeight = 20;
        int spacing = 10;
        int startX = x + 10;
        int startY = y + 10;
        
        tabButtons.add(new Button(
            startX, startY, buttonWidth, buttonHeight,
            new StringTextComponent("Пурпур"),
            btn -> setEffectColor(0xFF800080, 0)
        ));
        
        tabButtons.add(new Button(
            startX + buttonWidth + spacing, startY, buttonWidth, buttonHeight,
            new StringTextComponent("Лайм"),
            btn -> setEffectColor(0xFF00FF00, 1)
        ));
        
        tabButtons.add(new Button(
            startX + (buttonWidth + spacing) * 2, startY, buttonWidth, buttonHeight,
            new StringTextComponent("Красный"),
            btn -> setEffectColor(0xFFFF0000, 2)
        ));
        
        tabButtons.add(new Button(
            startX + (buttonWidth + spacing) * 3, startY, buttonWidth, buttonHeight,
            new StringTextComponent("Clear"),
            btn -> clearEffect()
        ));
        
        return tabButtons;
    }

    private void setEffectColor(int color, int buttonIndex) {
        BlockHighlightHandler.setEffectEnabled(true);
        BlockHighlightHandler.setEffectColor(color);
        selectedButton = buttonIndex;
        VSBaseMod.LOGGER.info("Set effect color to 0x{}", Integer.toHexString(color));
    }

    private void clearEffect() {
        BlockHighlightHandler.clearEffect();
        selectedButton = -1;
        VSBaseMod.LOGGER.info("Cleared effect");
    }
    
    private int getColorButtonIndex(int color) {
        if (color == 0xFF800080) return 0;
        if (color == 0xFF00FF00) return 1;
        if (color == 0xFFFF0000) return 2;
        return -1;
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float pt, int x, int y, int w, int h) {
        AbstractGui.fill(ms, x, y, x + w, y + h, 0xFF252525);
        drawBorder(ms, x, y, w, h, 0xFF444444);
        
        Minecraft mc = Minecraft.getInstance();
        
        if (BlockHighlightHandler.isEffectEnabled()) {
            mc.fontRenderer.drawString(ms, "Effect: ACTIVE", 
                (float)(x + 10), (float)(y + 50), BlockHighlightHandler.getEffectColor());
            mc.fontRenderer.drawString(ms, "Look at a block for particles", 
                (float)(x + 10), (float)(y + 65), 0xFFAAAAAA);
        } else {
            mc.fontRenderer.drawString(ms, "Effect: INACTIVE", 
                (float)(x + 10), (float)(y + 50), 0xFFAAAAAA);
            mc.fontRenderer.drawString(ms, "Click a color button", 
                (float)(x + 10), (float)(y + 65), 0xFFAAAAAA);
        }
        
        renderButtonStates(ms, x, y);
    }

    private void renderButtonStates(MatrixStack ms, int x, int y) {
        int buttonWidth = 80;
        int spacing = 10;
        int startX = x + 10;
        int startY = y + 10;
        
        if (selectedButton >= 0 && selectedButton <= 2) {
            int btnX = startX + selectedButton * (buttonWidth + spacing);
            AbstractGui.fill(ms, btnX - 2, startY - 2, 
                btnX + buttonWidth + 2, startY + 22, 0xFFFFFFFF);
        }
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
}
