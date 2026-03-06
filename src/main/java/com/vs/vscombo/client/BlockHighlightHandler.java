package com.vs.vscombo.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.vs.vscombo.VSBaseMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
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
        
        // FIX: Получаем целевой блок через rayTrace
        BlockRayTraceResult result = mc.objectMouseTarget;
        
        if (result != null && result.getType() == BlockRayTraceResult.Type.BLOCK) {
            BlockPos pos = result.getPos();
            
            // FIX: Рисуем обводку с правильной сигнатурой для 1.16.5
            drawBlockOutline(matrixStack, pos, effectColor);
            
            // Спавним частицы
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastParticleTime > 100) {
                spawnBlockParticles(mc, pos);
                lastParticleTime = currentTime;
            }
        }
    }
    
    private static void drawBlockOutline(MatrixStack matrixStack, BlockPos pos, int color) {
        Minecraft mc = Minecraft.getInstance();
        
        // FIX: В 1.16.5 используем RenderSystem и IVertexBuilder
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(2.0F);
        
        // FIX: Получаем позицию камеры правильно для 1.16.5
        Vector3d cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        
        AxisAlignedBB bb = new AxisAlignedBB(
            pos.getX() - cameraPos.x,
            pos.getY() - cameraPos.y,
            pos.getZ() - cameraPos.z,
            pos.getX() + 1 - cameraPos.x,
            pos.getY() + 1 - cameraPos.y,
            pos.getZ() + 1 - cameraPos.z
        );
        
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        
        // FIX: Используем правильный метод drawBoundingBox для 1.16.5
        IVertexBuilder builder = Minecraft.getInstance().renderBuffers().bufferSource()
            .getBuffer(RenderType.lines());
        
        WorldRenderer.renderLineBox(matrixStack, builder, bb, 
            (float)r / 255f, (float)g / 255f, (float)b / 255f, 0.8f);
        
        Minecraft.getInstance().renderBuffers().bufferSource().finish(RenderType.lines());
        
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
