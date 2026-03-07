package com.vs.vscombo.client;

import com.vs.vscombo.VSBaseMod;
import net.minecraft.client.Minecraft;
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
        
        // Спавним частицы каждые 100мс
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastParticleTime > 100) {
            spawnBlockParticles(mc);
            lastParticleTime = currentTime;
        }
    }
    
    private static void spawnBlockParticles(Minecraft mc) {
        if (mc.objectMouseOver == null || mc.objectMouseOver.getType() != net.minecraft.util.math.BlockRayTraceResult.Type.BLOCK) {
            return;
        }
        
        BlockPos pos = ((BlockRayTraceResult) mc.objectMouseOver).getPos();
        
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
