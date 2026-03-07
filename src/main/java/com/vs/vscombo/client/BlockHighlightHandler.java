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
    
    // Цвета частиц для разных типов
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
        
        // Получаем блок, на который смотрит игрок
        if (mc.objectMouseOver == null || mc.objectMouseOver.getType() != net.minecraft.util.math.BlockRayTraceResult.Type.BLOCK) {
            return;
        }
        
        BlockPos pos = ((BlockRayTraceResult) mc.objectMouseOver).getPos();
        
        // Спавним частицы каждые 50мс (20 раз в секунду)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastParticleTime > 50) {
            spawnBlockParticles(mc, pos);
            lastParticleTime = currentTime;
        }
    }
    
    private static void spawnBlockParticles(Minecraft mc, BlockPos pos) {
        if (mc.world == null) return;
        
        // Центр блока
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        
        // Выбираем тип частиц в зависимости от цвета
        BasicParticleType particleType = getParticleTypeForColor(effectColor);
        
        // Создаем поток частиц от 8 углов блока наружу
        spawnParticleFromCorner(mc, particleType, centerX, centerY, centerZ, 1, 1, 1);   // +X +Y +Z
        spawnParticleFromCorner(mc, particleType, centerX, centerY, centerZ, 1, 1, -1);  // +X +Y -Z
        spawnParticleFromCorner(mc, particleType, centerX, centerY, centerZ, 1, -1, 1);  // +X -Y +Z
        spawnParticleFromCorner(mc, particleType, centerX, centerY, centerZ, 1, -1, -1); // +X -Y -Z
        spawnParticleFromCorner(mc, particleType, centerX, centerY, centerZ, -1, 1, 1);  // -X +Y +Z
        spawnParticleFromCorner(mc, particleType, centerX, centerY, centerZ, -1, 1, -1); // -X +Y -Z
        spawnParticleFromCorner(mc, particleType, centerX, centerY, centerZ, -1, -1, 1); // -X -Y +Z
        spawnParticleFromCorner(mc, particleType, centerX, centerY, centerZ, -1, -1, -1);// -X -Y -Z
        
        // Дополнительные частицы от граней блока
        spawnParticleFromFace(mc, particleType, centerX, centerY, centerZ);
    }
    
    private static void spawnParticleFromCorner(Minecraft mc, BasicParticleType particleType, 
                                                 double centerX, double centerY, double centerZ,
                                                 int dirX, int dirY, int dirZ) {
        // Позиция частицы на углу блока (0.5 блока от центра)
        double posX = centerX + (dirX * 0.5);
        double posY = centerY + (dirY * 0.5);
        double posZ = centerZ + (dirZ * 0.5);
        
        // Направление движения от центра наружу (нормализованное)
        double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        double velX = (dirX / length) * 0.15;
        double velY = (dirY / length) * 0.15;
        double velZ = (dirZ / length) * 0.15;
        
        // FIX: Приводим BasicParticleType к IParticleData
        mc.world.addParticle((IParticleData) particleType, posX, posY, posZ, velX, velY, velZ);
    }
    
    private static void spawnParticleFromFace(Minecraft mc, BasicParticleType particleType,
                                               double centerX, double centerY, double centerZ) {
        // Частицы от 6 граней блока
        int[][] faces = {
            {1, 0, 0}, {-1, 0, 0},  // +X, -X
            {0, 1, 0}, {0, -1, 0},  // +Y, -Y
            {0, 0, 1}, {0, 0, -1}   // +Z, -Z
        };
        
        for (int[] face : faces) {
            double posX = centerX + (face[0] * 0.5);
            double posY = centerY + (face[1] * 0.5);
            double posZ = centerZ + (face[2] * 0.5);
            
            double velX = face[0] * 0.1;
            double velY = face[1] * 0.1;
            double velZ = face[2] * 0.1;
            
            // FIX: Приводим BasicParticleType к IParticleData
            mc.world.addParticle((IParticleData) particleType, posX, posY, posZ, velX, velY, velZ);
        }
    }
    
    private static BasicParticleType getParticleTypeForColor(int color) {
        // Выбираем тип частиц в зависимости от цвета
        if (color == COLOR_PURPLE) {
            return ParticleTypes.PORTAL;      // Пурпурные частицы
        } else if (color == COLOR_LIME) {
            return ParticleTypes.ENCHANT;     // Зелёные частицы
        } else if (color == COLOR_RED) {
            return ParticleTypes.FLAME;       // Оранжево-красные частицы
        }
        return ParticleTypes.PORTAL; // По умолчанию
    }
}
