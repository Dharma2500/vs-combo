package com.vs.vscombo.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vs.vscombo.VSBaseMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = VSBaseMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockHighlightHandler {
    
    private static boolean effectEnabled = false;
    private static int effectColor = 0xFF800080;
    private static long lastParticleTime = 0;
    
    public static void setEffectEnabled(boolean enabled) {
        effectEnabled = enabled;
        VSBaseMod.LOGGER.info("Block highlight {}", enabled ? "enabled" : "disabled");
    }
    
    public static void setEffectColor(int color) {
        effectColor = color;
        VSBaseMod.LOGGER.info("Block highlight color set to {}", color);
    }
    
    public static boolean isEffectEnabled() {
        return effectEnabled;
    }
    
    public static int getEffectColor() {
        return effectColor;
    }
    
    public static void clearEffect() {
        effectEnabled = false;
    }
    
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!effectEnabled) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.world == null) return;
        
        MatrixStack matrixStack = event.getMatrixStack();
        
        // FIX: В MCP snapshot mappings поле называется objectMouseTarget
        if (mc.objectMouseTarget != null && mc.objectMouseTarget.getType() == BlockRayTraceResult.Type.BLOCK) {
            BlockPos pos = ((BlockRayTraceResult) mc.objectMouseTarget).getPos();
            
            drawBlockOutline(matrixStack, pos, effectColor, event.getPartialTicks());
            
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastParticleTime > 100) {
                spawnBlockParticles(mc, pos);
                lastParticleTime = currentTime;
            }
        }
    }
    
    private static void drawBlockOutline(MatrixStack matrixStack, BlockPos pos, int color, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(2.0F);
        
        // FIX: В MCP snapshot mappings используем getRenderViewEntity() и prevPosX/posX
        Entity renderViewEntity = mc.getRenderViewEntity();
        if (renderViewEntity == null) return;
        
        double viewerX = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * partialTicks;
        double viewerY = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * partialTicks;
        double viewerZ = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * partialTicks;
        
        AxisAlignedBB bb = new AxisAlignedBB(
            pos.getX() - viewerX, pos.getY() - viewerY, pos.getZ() - viewerZ,
            pos.getX() + 1 - viewerX, pos.getY() + 1 - viewerY, pos.getZ() + 1 - viewerZ
        );
        
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        
        // FIX: Используем Tessellator + BufferBuilder с правильным импортом
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        // FIX: Правильный путь к DefaultVertexFormats для MCP mappings
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        
        // Рисуем 12 рёбер куба
        // Нижняя грань
        buffer.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, 255).endVertex();
        
        // Верхняя грань
        buffer.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, 255).endVertex();
        
        // Вертикальные рёбра
        buffer.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, 255).endVertex();
        
        buffer.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, 255).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, 255).endVertex();
        
        tessellator.draw();
        
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
    
    private static void spawnBlockParticles(Minecraft mc, BlockPos pos) {
        if (mc.world == null) return;
        
        // FIX: В MCP mappings поле называется rand, а не random
        for (int i = 0; i < 4; i++) {
            double offsetX = (mc.world.rand.nextDouble() - 0.5) * 1.2;
            double offsetY = (mc.world.rand.nextDouble() - 0.5) * 1.2;
            double offsetZ = (mc.world.rand.nextDouble() - 0.5) * 1.2;
            
            mc.world.addParticle(
                net.minecraft.particles.ParticleTypes.PORTAL,
                pos.getX() + 0.5 + offsetX,
                pos.getY() + 0.5 + offsetY,
                pos.getZ() + 0.5 + offsetZ,
                0, 0, 0
            );
        }
    }
}
