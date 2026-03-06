package com.vs.vscombo.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vs.vscombo.VSBaseMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
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
        
        // FIX: Получаем целевой блок через objectMouseTarget (1.16.5 compatible)
        if (mc.objectMouseTarget != null && mc.objectMouseTarget.getType() == BlockRayTraceResult.Type.BLOCK) {
            BlockPos pos = ((BlockRayTraceResult) mc.objectMouseTarget).getPos();
            
            drawBlockOutline(matrixStack, pos, effectColor, event.getPartialTicks());
            
            // Спавним частицы каждые 100мс
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
        
        // FIX: Получаем позицию камеры через renderViewEntity (1.16.5 official mappings compatible)
        Entity renderViewEntity = mc.getRenderViewEntity();
        if (renderViewEntity == null) return;
        
        double viewerX = renderViewEntity.lastTickPosX + (renderViewEntity.getPosX() - renderViewEntity.lastTickPosX) * partialTicks;
        double viewerY = renderViewEntity.lastTickPosY + (renderViewEntity.getPosY() - renderViewEntity.lastTickPosY) * partialTicks;
        double viewerZ = renderViewEntity.lastTickPosZ + (renderViewEntity.getPosZ() - renderViewEntity.lastTickPosZ) * partialTicks;
        
        // Создаём AABB относительно камеры
        AxisAlignedBB bb = new AxisAlignedBB(
            pos.getX() - viewerX, pos.getY() - viewerY, pos.getZ() - viewerZ,
            pos.getX() + 1 - viewerX, pos.getY() + 1 - viewerY, pos.getZ() + 1 - viewerZ
        );
        
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        
        // FIX: Используем WorldRenderer.drawBoundingBox с правильной сигнатурой для 1.16.5
        WorldRenderer.drawBoundingBox(matrixStack, bb, 
            (float)r / 255f, (float)g / 255f, (float)b / 255f, 0.8f);
        
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
    
    private static void spawnBlockParticles(Minecraft mc, BlockPos pos) {
        if (mc.world == null) return;
        
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
