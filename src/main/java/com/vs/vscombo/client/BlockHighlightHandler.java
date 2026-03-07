package com.vs.vscombo.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vs.vscombo.VSBaseMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
        VSBaseMod.LOGGER.info("Block highlight color set to 0x{}", Integer.toHexString(color));
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
        
        // FIX: Для MCP mappings используем objectMouseOver
        if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == net.minecraft.util.math.BlockRayTraceResult.Type.BLOCK) {
            BlockPos pos = ((BlockRayTraceResult) mc.objectMouseOver).getPos();
            
            MatrixStack matrixStack = event.getMatrixStack();
            drawBlockOutline(matrixStack, pos, effectColor);
            
            // Спавним частицы каждые 100мс
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastParticleTime > 100) {
                spawnBlockParticles(mc, pos);
                lastParticleTime = currentTime;
            }
        }
    }
    
    private static void drawBlockOutline(MatrixStack matrixStack, BlockPos pos, int color) {
        // Извлекаем компоненты цвета
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = 200; // Полупрозрачность
        
        // Создаем AABB для блока
        AxisAlignedBB bb = new AxisAlignedBB(
            pos.getX(), pos.getY(), pos.getZ(),
            pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
        );
        
        // Push матрицу
        matrixStack.push();
        
        // Включаем blending
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(3.0F);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        // Начинаем рисовать линии
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        
        // Рисуем все 12 ребер куба
        // Нижняя грань
        buffer.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
        
        buffer.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
        
        buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
        
        buffer.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
        
        // Верхняя грань
        buffer.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        
        buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        
        buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        
        buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        
        // Вертикальные ребра
        buffer.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        
        buffer.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        
        buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        
        buffer.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        
        tessellator.draw();
        
        // Восстанавливаем состояние
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        
        // Pop матрицу
        matrixStack.pop();
    }
    
    private static void spawnBlockParticles(Minecraft mc, BlockPos pos) {
        if (mc.world == null) return;
        
        // Центр блока
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        
        for (int i = 0; i < 8; i++) {
            // Случайное направление от центра
            double offsetX = (mc.world.rand.nextDouble() - 0.5) * 2.0;
            double offsetY = (mc.world.rand.nextDouble() - 0.5) * 2.0;
            double offsetZ = (mc.world.rand.nextDouble() - 0.5) * 2.0;
            
            // Нормализуем
            double length = Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
            if (length > 0) {
                offsetX /= length;
                offsetY /= length;
                offsetZ /= length;
            }
            
            // Спавним частицу
            mc.world.addParticle(
                net.minecraft.particles.ParticleTypes.PORTAL,
                centerX, centerY, centerZ,
                offsetX * 0.5, offsetY * 0.5, offsetZ * 0.5
            );
        }
    }
}
