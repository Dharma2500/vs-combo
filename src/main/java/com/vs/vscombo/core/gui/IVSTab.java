package com.vs.vscombo.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import java.util.List;

public interface IVSTab {
    void init(Screen parent, int x, int y, int width, int height);
    void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, int x, int y, int w, int h);
    boolean keyPressed(int keyCode, int scanCode, int modifiers);
    boolean charTyped(char codePoint, int modifiers);
    void onShow();
    void onHide();
    // VS Core: Modular button registration pattern
    default List<Button> getButtons(int x, int y, int width, int height) {
        return java.util.Collections.emptyList();
    }
}
