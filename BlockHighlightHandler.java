package com.vs.vscombo.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vs.vscombo.VSBaseMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = VSBaseMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockHighlightHandler {
    
    // Effect state (сохраняется между сессиями)
    private static boolean effectEnabled = false;
    private static int effectColor = 0xFF800080; // Default purple
    private static BlockPos lastTargetedPos = null;
    private static long lastParticleTime = 0;
    
    // Public API для управления из GUI
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
        lastTargetedPos = null;
    }
    
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!effectEnabled) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.world == null) return;
        
        MatrixStack matrixStack = event.getMatrixStack();
        
        // Get targeted block using player's look
        Vector3d start = mc.player.getEyePosition(1.0f);
        Vector3d look = mc.player.getLook(1.0f);
        Vector3d end = start.add(look.scale(6.0));
        
        BlockRayTraceResult result = mc.world.rayTraceBlocks(
            new net.minecraft.util.math.BlockRayTraceResult(start, net.minecraft.util.Direction.UP, 
                BlockPos.ZERO, false),
            new net.minecraft.util.math.BlockRayTraceResult(end, net.minecraft.util.Direction.DOWN, 
                BlockPos.ZERO, false),
            false, BlockPos.ZERO, false
        );
        
        if (result != null && result.getType() == BlockRayTraceResult.Type.BLOCK) {
            BlockPos pos = result.getPos();
            
            // Draw outline
            drawBlockOutline(matrixStack, pos, effectColor);
            
            // Spawn particles (every 100ms)
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastParticleTime > 100) {
                spawnBlockParticles(pos);
                lastParticleTime = currentTime;
            }
            
            lastTargetedPos = pos;
        } else {
            lastTargetedPos = null;
        }
    }
    
    private static void drawBlockOutline(MatrixStack matrixStack, BlockPos pos, int color) {
        Minecraft mc = Minecraft.getInstance();
        
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 
            GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        
        double d0 = mc.getRenderManager().viewerX;
        double d1 = mc.getRenderManager().viewerY;
        double d2 = mc.getRenderManager().viewerZ;
        
        AxisAlignedBB bb = new AxisAlignedBB(
            pos.getX() - d0, pos.getY() - d1, pos.getZ() - d2,
            pos.getX() + 1 - d0, pos.getY() + 1 - d1, pos.getZ() + 1 - d2
        );
        
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        
        // Draw outline using WorldRenderer
        WorldRenderer.drawBoundingBox(matrixStack, bb, 
            (float)r / 255f, (float)g / 255f, (float)b / 255f, 0.8f);
        
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
    
    private static void spawnBlockParticles(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
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
