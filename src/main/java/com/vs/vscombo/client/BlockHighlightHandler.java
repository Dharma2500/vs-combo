package com.vs.vscombo.client;

import com.vs.vscombo.VSBaseMod;
import net.minecraft.client.Minecraft;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VSBaseMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockHighlightHandler {
    
    private static boolean effectEnabled = false;
    private static int effectColor = 0xFF800080;
    private static long lastParticleTime = 0;
    
    private static final int COLOR_PURPLE = 0xFF800080;
    private static final int COLOR_LIME = 0xFF00FF00;
    private static final int COLOR_RED = 0xFFFF0000;
    
    public static void setEffectEnabled(boolean enabled) {
        effectEnabled = enabled;
        VSBaseMod.LOGGER.info("Block effect {}", enabled ? "enabled" : "disabled");
    }
    
    public static void setEffectColor(int color) {
        effectColor = color;
        VSBaseMod.LOGGER.info("Block effect color set to 0x{}", Integer.toHexString(color));
    }
    
    public static boolean isEffectEnabled() { return effectEnabled; }
    public static int getEffectColor() { return effectColor; }
    public static void clearEffect() { effectEnabled = false; }
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!effectEnabled || event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.world == null) return;
        
        if (mc.objectMouseOver == null || mc.objectMouseOver.getType() != net.minecraft.util.math.BlockRayTraceResult.Type.BLOCK) {
            return;
        }
        
        BlockPos pos = ((BlockRayTraceResult) mc.objectMouseOver).getPos();
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastParticleTime > 100) {
            spawnCornerParticles(mc, pos);
            lastParticleTime = currentTime;
        }
    }
    
    private static void spawnCornerParticles(Minecraft mc, BlockPos pos) {
        if (mc.world == null) return;
        
        BasicParticleType particleType = getParticleTypeForColor(effectColor);
        
        // 8 углов блока
        double[][] corners = {
            {pos.getX(), pos.getY(), pos.getZ()},                    // 0,0,0
            {pos.getX() + 1, pos.getY(), pos.getZ()},                // 1,0,0
            {pos.getX(), pos.getY() + 1, pos.getZ()},                // 0,1,0
            {pos.getX() + 1, pos.getY() + 1, pos.getZ()},            // 1,1,0
            {pos.getX(), pos.getY(), pos.getZ() + 1},                // 0,0,1
            {pos.getX() + 1, pos.getY(), pos.getZ() + 1},            // 1,0,1
            {pos.getX(), pos.getY() + 1, pos.getZ() + 1},            // 0,1,1
            {pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1}         // 1,1,1
        };
        
        // Спавним частицы от каждого угла
        for (double[] corner : corners) {
            // Направление ОТ центра блока к углу
            double centerX = pos.getX() + 0.5;
            double centerY = pos.getY() + 0.5;
            double centerZ = pos.getZ() + 0.5;
            
            double dirX = corner[0] - centerX;
            double dirY = corner[1] - centerY;
            double dirZ = corner[2] - centerZ;
            
            // Нормализуем и усиливаем скорость
            double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
            if (length > 0) {
                dirX = (dirX / length) * 0.3;
                dirY = (dirY / length) * 0.3;
                dirZ = (dirZ / length) * 0.3;
            }
            
            // Спавним частицу на угле с направлением ОТ центра
            mc.world.addParticle(
                (IParticleData) particleType,
                corner[0], corner[1], corner[2],
                dirX, dirY, dirZ
            );
        }
    }
    
    private static BasicParticleType getParticleTypeForColor(int color) {
        if (color == COLOR_PURPLE) {
            return ParticleTypes.PORTAL;
        } else if (color == COLOR_LIME) {
            return ParticleTypes.ENCHANT;
        } else if (color == COLOR_RED) {
            return ParticleTypes.FLAME;
        }
        return ParticleTypes.PORTAL;
    }
}
