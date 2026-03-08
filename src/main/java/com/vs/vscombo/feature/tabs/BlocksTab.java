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
    protected int selectedParticleButton = -1;
    protected int selectedBlockButton = -1;

    public BlocksTab() {}

    @Override
    public void init(Screen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        if (BlockHighlightHandler.isParticleEffectEnabled()) {
            selectedParticleButton = getColorButtonIndex(BlockHighlightHandler.getParticleEffectColor());
        } else {
            selectedParticleButton = -1;
        }
        
        if (BlockHighlightHandler.isBlockEffectEnabled()) {
            selectedBlockButton = getColorButtonIndex(BlockHighlightHandler.getBlockEffectColor());
        } else {
            selectedBlockButton = -1;
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
        
        // ===== ПЕРВЫЙ РЯД - ЦВЕТНЫЕ ЧАСТИЦЫ =====
        tabButtons.add(new Button(
            startX, startY, buttonWidth, buttonHeight,
            new StringTextComponent("Пурпур"),
            btn -> setParticleEffectColor(0xFF800080, 0)
        ));
        
        tabButtons.add(new Button(
            startX + buttonWidth + spacing, startY, buttonWidth, buttonHeight,
            new StringTextComponent("Лайм"),
            btn -> setParticleEffectColor(0xFF00FF00, 1)
        ));
        
        tabButtons.add(new Button(
            startX + (buttonWidth + spacing) * 2, startY, buttonWidth, buttonHeight,
            new StringTextComponent("Красный"),
            btn -> setParticleEffectColor(0xFFFF0000, 2)
        ));
        
        tabButtons.add(new Button(
            startX + (buttonWidth + spacing) * 3, startY, buttonWidth, buttonHeight,
            new StringTextComponent("Clear"),
            btn -> clearAllEffects()
        ));
        
        // ===== ВТОРОЙ РЯД - ПОРТАЛ/REDSTONE/ДЫМ =====
        int secondRowY = startY + buttonHeight + 15;
        
        // FIX: Кнопка "Портал" вместо "Пурпур"
        tabButtons.add(new Button(
            startX, secondRowY, buttonWidth, buttonHeight,
            new StringTextComponent("Портал"),
            btn -> setBlockEffectColor(0xFF800080, 0)
        ));
        
        tabButtons.add(new Button(
            startX + buttonWidth + spacing, secondRowY, buttonWidth, buttonHeight,
            new StringTextComponent("Лайм"),
            btn -> setBlockEffectColor(0xFF00FF00, 1)
        ));
        
        tabButtons.add(new Button(
            startX + (buttonWidth + spacing) * 2, secondRowY, buttonWidth, buttonHeight,
            new StringTextComponent("Красный"),
            btn -> setBlockEffectColor(0xFFFF0000, 2)
        ));
        
        tabButtons.add(new Button(
            startX + (buttonWidth + spacing) * 3, secondRowY, buttonWidth, buttonHeight,
            new StringTextComponent("Clear"),
            btn -> clearAllEffects()
        ));
        
        return tabButtons;
    }

    private void setParticleEffectColor(int color, int buttonIndex) {
        BlockHighlightHandler.setParticleEffectEnabled(true);
        BlockHighlightHandler.setParticleEffectColor(color);
        selectedParticleButton = buttonIndex;
        VSBaseMod.LOGGER.info("Set particle effect color to 0x{}", Integer.toHexString(color));
    }

    private void setBlockEffectColor(int color, int buttonIndex) {
        BlockHighlightHandler.setBlockEffectEnabled(true);
        BlockHighlightHandler.setBlockEffectColor(color);
        selectedBlockButton = buttonIndex;
        VSBaseMod.LOGGER.info("Set block effect color to 0x{}", Integer.toHexString(color));
    }

    private void clearAllEffects() {
        BlockHighlightHandler.clearAllEffects();
        selectedParticleButton = -1;
        selectedBlockButton = -1;
        VSBaseMod.LOGGER.info("Cleared all effects");
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
        
        // ===== ТЕКСТОВАЯ ПОДСКАЗКА ВВЕРХУ =====
        int textStartY = y + 10;
        int textLineHeight = 15;
        
        mc.fontRenderer.drawString(ms, "Используйте кнопки для добавления/изменения", 
            (float)(x + 10), (float)(textStartY), 0xFFAAAAAA);
        mc.fontRenderer.drawString(ms, "цвета эффекта на блоках", 
            (float)(x + 10), (float)(textStartY + textLineHeight), 0xFFAAAAAA);
        
        // ===== ТЕКСТ В ДВА СТОЛБЦА =====
        int infoStartY = y + 70;
        int column1X = x + 10;
        int column2X = x + 200;
        
        // Левый столбец - названия эффектов
        mc.fontRenderer.drawString(ms, "Effect 1: Particles", 
            (float)(column1X), (float)(infoStartY), 0xFFE0E0E0);
        mc.fontRenderer.drawString(ms, "Effect 2: Mini Blocks", 
            (float)(column1X), (float)(infoStartY + textLineHeight * 2), 0xFFE0E0E0);
        
        // Правый столбец - статусы
        if (BlockHighlightHandler.isParticleEffectEnabled()) {
            mc.fontRenderer.drawString(ms, "Status: ACTIVE", 
                (float)(column2X), (float)(infoStartY), BlockHighlightHandler.getParticleEffectColor());
        } else {
            mc.fontRenderer.drawString(ms, "Status: INACTIVE", 
                (float)(column2X), (float)(infoStartY), 0xFFAAAAAA);
        }
        
        if (BlockHighlightHandler.isBlockEffectEnabled()) {
            mc.fontRenderer.drawString(ms, "Status: ACTIVE", 
                (float)(column2X), (float)(infoStartY + textLineHeight * 2), BlockHighlightHandler.getBlockEffectColor());
        } else {
            mc.fontRenderer.drawString(ms, "Status: INACTIVE", 
                (float)(column2X), (float)(infoStartY + textLineHeight * 2), 0xFFAAAAAA);
        }
        
        renderButtonStates(ms, x, y);
    }

    private void renderButtonStates(MatrixStack ms, int x, int y) {
        int buttonWidth = 80;
        int spacing = 10;
        int startX = x + 10;
        int startY = y + 10;
        int secondRowY = startY + 35;
        
        if (selectedParticleButton >= 0 && selectedParticleButton <= 2) {
            int btnX = startX + selectedParticleButton * (buttonWidth + spacing);
            AbstractGui.fill(ms, btnX - 2, startY - 2, 
                btnX + buttonWidth + 2, startY + 22, 0xFFFFFFFF);
        }
        
        if (selectedBlockButton >= 0 && selectedBlockButton <= 2) {
            int btnX = startX + selectedBlockButton * (buttonWidth + spacing);
            AbstractGui.fill(ms, btnX - 2, secondRowY - 2, 
                btnX + buttonWidth + 2, secondRowY + 22, 0xFFFFFFFF);
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
