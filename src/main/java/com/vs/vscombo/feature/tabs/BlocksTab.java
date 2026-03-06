package com.vs.vscombo.feature.tabs;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.vs.vscombo.VSBaseMod;
import com.vs.vscombo.core.gui.IVSTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BlocksTab implements IVSTab {
    
    protected Screen parent;
    protected int x, y, width, height;
    
    protected final List<Button> tabButtons = new ArrayList<>();
    
    // Effect state
    protected boolean effectEnabled = false;
    protected int effectColor = 0xFF800080; // Default purple
    protected int selectedButton = 0;

    public BlocksTab() {}

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
        
        int buttonWidth = 70;
        int buttonHeight = 20;
        int spacing = 5;
        int startX = x + 10;
        int startY = y + 10;
        
        // Purple button
        tabButtons.add(new Button(
            startX, startY, buttonWidth, buttonHeight,
            new StringTextComponent("Пурпур"),
            btn -> setEffectColor(0xFF800080, 0)
        ));
        
        // Lime button
        tabButtons.add(new Button(
            startX + buttonWidth + spacing, startY, buttonWidth, buttonHeight,
            new StringTextComponent("Лайм"),
            btn -> setEffectColor(0xFF00FF00, 1)
        ));
        
        // Red button
        tabButtons.add(new Button(
            startX + (buttonWidth + spacing) * 2, startY, buttonWidth, buttonHeight,
            new StringTextComponent("Красный"),
            btn -> setEffectColor(0xFFFF0000, 2)
        ));
        
        // Clear button
        tabButtons.add(new Button(
            startX + (buttonWidth + spacing) * 3, startY, buttonWidth, buttonHeight,
            new StringTextComponent("Clear"),
            btn -> clearEffect()
        ));
        
        return tabButtons;
    }

    private void setEffectColor(int color, int buttonIndex) {
        this.effectColor = color;
        this.selectedButton = buttonIndex;
        this.effectEnabled = true;
        VSBaseMod.LOGGER.info("Block effect color set to {}", color);
    }

    private void clearEffect() {
        this.effectEnabled = false;
        this.selectedButton = -1;
        VSBaseMod.LOGGER.info("Block effect cleared");
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float pt, int x, int y, int w, int h) {
        AbstractGui.fill(ms, x, y, x + w, y + h, 0xFF252525);
        drawBorder(ms, x, y, w, h, 0xFF444444);
        
        Minecraft mc = Minecraft.getInstance();
        
        // Render effect preview
        if (effectEnabled) {
            mc.fontRenderer.drawString(ms, "Effect: ACTIVE", 
                (float)(x + 10), (float)(y + 40), effectColor);
            
            // Highlight targeted block
            highlightTargetedBlock(mc, ms);
        } else {
            mc.fontRenderer.drawString(ms, "Effect: INACTIVE", 
                (float)(x + 10), (float)(y + 40), 0xFFAAAAAA);
        }
        
        // Render button states
        renderButtonStates(ms, x, y);
    }

    private void renderButtonStates(MatrixStack ms, int x, int y) {
        int buttonWidth = 70;
        int spacing = 5;
        int startX = x + 10;
        int startY = y + 10;
        
        // Highlight selected button
        if (selectedButton >= 0 && selectedButton <= 2) {
            int btnX = startX + selectedButton * (buttonWidth + spacing);
            AbstractGui.fill(ms, btnX - 2, startY - 2, 
                btnX + buttonWidth + 2, startY + 22, 0xFFFFFFFF);
        }
    }

    private void highlightTargetedBlock(Minecraft mc, MatrixStack ms) {
        if (mc.player == null || mc.world == null) return;
        
        // Ray trace to get targeted block
        Vector3d start = mc.player.getEyePosition(1.0f);
        Vector3d look = mc.player.getLook(1.0f);
        Vector3d end = start.add(look.scale(6.0));
        
        BlockRayTraceResult result = mc.world.rayTraceBlocks(
            new net.minecraft.util.math.vector.Vector3d(start.x, start.y, start.z),
            new net.minecraft.util.math.vector.Vector3d(end.x, end.y, end.z),
            false, true, false
        );
        
        if (result != null && result.getType() == BlockRayTraceResult.Type.BLOCK) {
            // Draw outline around targeted block
            drawBlockOutline(mc, ms, result);
            
            // Spawn particles
            if (mc.player.ticksExisted % 5 == 0) {
                spawnBlockParticles(mc, result);
            }
        }
    }

    private void drawBlockOutline(Minecraft mc, MatrixStack ms, BlockRayTraceResult result) {
        net.minecraft.util.math.BlockPos pos = result.getPos();
        
        // Get render manager
        net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
        
        // Draw outline using the selected color
        double d0 = mc.getRenderManager().viewerPosX;
        double d1 = mc.getRenderManager().viewerPosY;
        double d2 = mc.getRenderManager().viewerPosZ;
        
        net.minecraft.util.math.AxisAlignedBB bb = new net.minecraft.util.math.AxisAlignedBB(
            pos.getX() - d0, pos.getY() - d1, pos.getZ() - d2,
            pos.getX() + 1 - d0, pos.getY() + 1 - d1, pos.getZ() + 1 - d2
        );
        
        // Draw outline (simplified for 1.16.5)
        net.minecraft.client.renderer.BufferBuilder buffer = 
            net.minecraft.client.renderer.Tessellator.getInstance().getBuffer();
        
        buffer.begin(net.minecraft.client.renderer.GL11.GL_LINE_STRIP, 
            net.minecraft.client.renderer.DefaultVertexFormats.POSITION_COLOR);
        
        int r = (effectColor >> 16) & 0xFF;
        int g = (effectColor >> 8) & 0xFF;
        int b = effectColor & 0xFF;
        
        // Draw box edges
        buffer.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, 255).endVertex();
        
        net.minecraft.client.renderer.Tessellator.getInstance().draw();
    }

    private void spawnBlockParticles(Minecraft mc, BlockRayTraceResult result) {
        net.minecraft.util.math.BlockPos pos = result.getPos();
        
        // Spawn simple particles around the block
        for (int i = 0; i < 4; i++) {
            double offsetX = (mc.rand.nextDouble() - 0.5) * 1.2;
            double offsetY = (mc.rand.nextDouble() - 0.5) * 1.2;
            double offsetZ = (mc.rand.nextDouble() - 0.5) * 1.2;
            
            mc.world.addParticle(
                net.minecraft.particles.ParticleTypes.PORTAL,
                pos.getX() + 0.5 + offsetX,
                pos.getY() + 0.5 + offsetY,
                pos.getZ() + 0.5 + offsetZ,
                0, 0, 0
            );
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

    public boolean isEffectEnabled() { return effectEnabled; }
    public int getEffectColor() { return effectColor; }

    @Override
    public void onShow() {}

    @Override
    public void onHide() {}
}
