package com.vs.vscombo.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;

public interface IVSTab {
    void init(Screen parent, int x, int y, int width, int height);
    void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, int x, int y, int w, int h);
    boolean keyPressed(int keyCode, int scanCode, int modifiers);
    boolean charTyped(char codePoint, int modifiers);
    void onShow();
    void onHide();
}
