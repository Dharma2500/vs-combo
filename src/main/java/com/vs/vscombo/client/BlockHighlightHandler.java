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
    
    // ========== ПЕРВЫЙ ЭФФЕКТ - ЦВЕТНЫЕ ЧАСТИЦЫ ==========
    private static boolean particleEffectEnabled = false;
    private static int particleEffectColor = 0xFF800080;
    private static long lastParticleTime = 0;
    
    // ========== ВТОРОЙ ЭФФЕКТ - КАПАЮЩИЕ ЧАСТИЦЫ ==========
    private static boolean blockEffectEnabled = false;
    private static int blockEffectColor = 0xFF800080;
    private static long lastBlockCycleTime = 0;
    private static int ticksInCurrentCycle = 0;
    
    // ========== КОНСТАНТЫ ==========
    private static final int COLOR_PURPLE = 0xFF800080;
    private static final int COLOR_LIME = 0xFF00FF00;
    private static final int COLOR_RED = 0xFFFF0000;
    
    // ========== ПЕРВЫЙ ЭФФЕКТ - МЕТОДЫ ==========
    public static void setParticleEffectEnabled(boolean enabled) {
        particleEffectEnabled = enabled;
        VSBaseMod.LOGGER.info("Particle effect {}", enabled ? "enabled" : "disabled");
    }
    
    public static void setParticleEffectColor(int color) {
        particleEffectColor = color;
        VSBaseMod.LOGGER.info("Particle effect color set to 0x{}", Integer.toHexString(color));
    }
    
    public static boolean isParticleEffectEnabled() { 
        return particleEffectEnabled; 
    }
    
    public static int getParticleEffectColor() { 
        return particleEffectColor; 
    }
    
    // ========== ВТОРОЙ ЭФФЕКТ - МЕТОДЫ ==========
    public static void setBlockEffectEnabled(boolean enabled) {
        blockEffectEnabled = enabled;
        ticksInCurrentCycle = 0;
        lastBlockCycleTime = 0;
        VSBaseMod.LOGGER.info("Block effect {}", enabled ? "enabled" : "disabled");
    }
    
    public static void setBlockEffectColor(int color) {
        blockEffectColor = color;
        ticksInCurrentCycle = 0;
        VSBaseMod.LOGGER.info("Block effect color set to 0x{}", Integer.toHexString(color));
    }
    
    public static boolean isBlockEffectEnabled() { 
        return blockEffectEnabled; 
    }
    
    public static int getBlockEffectColor() { 
        return blockEffectColor; 
    }
    
    // ========== ОБЩАЯ ФУНКЦИЯ CLEAR ==========
    public static void clearAllEffects() {
        particleEffectEnabled = false;
        blockEffectEnabled = false;
        ticksInCurrentCycle = 0;
        VSBaseMod.LOGGER.info("All effects cleared");
    }
    
    // ========== ОБРАБОТКА СОБЫТИЙ ==========
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.world == null) return;
        
        // FIX: MCP mappings - objectMouseOver
        if (mc.objectMouseOver == null || mc.objectMouseOver.getType() != BlockRayTraceResult.Type.BLOCK) {
            return;
        }
        
        BlockPos pos = ((BlockRayTraceResult) mc.objectMouseOver).getPos();
        long currentTime = System.currentTimeMillis();
        
        // Первый эффект - цветные частицы (каждые 100мс)
        if (particleEffectEnabled && currentTime - lastParticleTime > 100) {
            spawnParticleEffect(mc, pos);
            lastParticleTime = currentTime;
        }
        
        // Второй эффект - быстрый цикл (0.05с активно, 0.15с пауза)
        if (blockEffectEnabled) {
            spawnFastCyclicParticles(mc, pos);
        }
    }
    
    // ========== ПЕРВЫЙ ЭФФЕКТ - ЦВЕТНЫЕ ЧАСТИЦЫ ==========
    private static void spawnParticleEffect(Minecraft mc, BlockPos pos) {
        if (mc.world == null) return;
        
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        
        double[][] corners = {
            {pos.getX(), pos.getY(), pos.getZ()},
            {pos.getX() + 1, pos.getY(), pos.getZ()},
            {pos.getX(), pos.getY() + 1, pos.getZ()},
            {pos.getX() + 1, pos.getY() + 1, pos.getZ()},
            {pos.getX(), pos.getY(), pos.getZ() + 1},
            {pos.getX() + 1, pos.getY(), pos.getZ() + 1},
            {pos.getX(), pos.getY() + 1, pos.getZ() + 1},
            {pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1}
        };
        
        for (double[] corner : corners) {
            double dirX = corner[0] - centerX;
            double dirY = corner[1] - centerY;
            double dirZ = corner[2] - centerZ;
            
            double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
            if (length > 0) {
                dirX = (dirX / length) * 0.3;
                dirY = (dirY / length) * 0.3;
                dirZ = (dirZ / length) * 0.3;
            }
            
            mc.world.addParticle(
                ParticleTypes.ENTITY_EFFECT,
                corner[0], corner[1], corner[2],
                getRed(particleEffectColor), getGreen(particleEffectColor), getBlue(particleEffectColor)
            );
        }
    }
    
    // ========== ВТОРОЙ ЭФФЕКТ - БЫСТРЫЕ ЦИКЛИЧЕСКИЕ ЧАСТИЦЫ ==========
    private static void spawnFastCyclicParticles(Minecraft mc, BlockPos pos) {
        if (mc.world == null) return;
        
        long currentTime = System.currentTimeMillis();
        long elapsedMs = currentTime - lastBlockCycleTime;
        
        // Новый цикл каждые 0.2 секунды (200мс)
        if (elapsedMs >= 200) {
            lastBlockCycleTime = currentTime;
            ticksInCurrentCycle = 0;
        }
        
        // Спавним частицы только в первый тик цикла (первые 0.05с = 50мс)
        if (elapsedMs < 50 && ticksInCurrentCycle == 0) {
            spawnBlockParticles(mc, pos);
            ticksInCurrentCycle = 1;
        }
    }
    
    // ========== СПАВН ЧАСТИЦ ВТОРОГО ЭФФЕКТА ==========
    private static void spawnBlockParticles(Minecraft mc, BlockPos pos) {
        // 8 углов блока
        double[][] corners = {
            {0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {1, 1, 0},
            {0, 0, 1}, {1, 0, 1}, {0, 1, 1}, {1, 1, 1}
        };
        
        // Соседние углы (только по осям)
        int[][] neighbors = {
            {1, 2, 4}, {0, 3, 5}, {0, 3, 6}, {1, 2, 7},
            {0, 5, 6}, {1, 4, 7}, {2, 4, 7}, {3, 5, 6}
        };
        
        // Выбираем тип частиц в зависимости от цвета
        BasicParticleType particleType;
        
        // FIX: PORTAL для пурпурного, DRIPPING_LAVA для красного, SMOKE для лайма
        if (blockEffectColor == COLOR_RED) {
            particleType = ParticleTypes.DRIPPING_LAVA;    // Красный → лава
        } else if (blockEffectColor == COLOR_PURPLE) {
            particleType = ParticleTypes.PORTAL;           // Пурпур → портал
        } else {
            particleType = ParticleTypes.SMOKE;            // Лайм → дым
        }
        
        // Спавним частицы из каждого угла
        for (int i = 0; i < corners.length; i++) {
            int[] neighborIndices = neighbors[i];
            int targetIndex = neighborIndices[mc.world.rand.nextInt(neighborIndices.length)];
            
            double startX = pos.getX() + corners[i][0];
            double startY = pos.getY() + corners[i][1];
            double startZ = pos.getZ() + corners[i][2];
            
            double targetX = pos.getX() + corners[targetIndex][0];
            double targetY = pos.getY() + corners[targetIndex][1];
            double targetZ = pos.getZ() + corners[targetIndex][2];
            
            double dirX = targetX - startX;
            double dirY = targetY - startY;
            double dirZ = targetZ - startZ;
            
            double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
            if (length > 0) {
                dirX = (dirX / length) * 0.12;
                dirY = (dirY / length) * 0.12;
                dirZ = (dirZ / length) * 0.12;
            }
            
            // Создаем 3 частицы на угол
            for (int j = 0; j < 3; j++) {
                // FIX: Правильное приведение типа для всех частиц
                mc.world.addParticle(
                    (IParticleData) particleType,
                    startX, startY, startZ,
                    dirX, dirY, dirZ
                );
            }
        }
    }
    
    // ========== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ==========
    private static float getRed(int color) {
        return ((color >> 16) & 0xFF) / 255.0f;
    }
    
    private static float getGreen(int color) {
        return ((color >> 8) & 0xFF) / 255.0f;
    }
    
    private static float getBlue(int color) {
        return (color & 0xFF) / 255.0f;
    }
}
