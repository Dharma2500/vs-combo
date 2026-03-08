package com.vs.vscombo.client;

import com.vs.vscombo.VSBaseMod;
import net.minecraft.client.Minecraft;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VSBaseMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockHighlightHandler {
    
    private static boolean particleEffectEnabled = false;
    private static int particleEffectColor = 0xFF800080;
    private static long lastParticleTime = 0;
    
    private static boolean blockEffectEnabled = false;
    private static int blockEffectColor = 0xFF800080;
    private static long lastBlockTime = 0;
    
    private static final int COLOR_PURPLE = 0xFF800080;
    private static final int COLOR_LIME = 0xFF00FF00;
    private static final int COLOR_RED = 0xFFFF0000;
    
    // ========== ПЕРВЫЙ ЭФФЕКТ - ЦВЕТНЫЕ ЧАСТИЦЫ ==========
    public static void setParticleEffectEnabled(boolean enabled) {
        particleEffectEnabled = enabled;
        VSBaseMod.LOGGER.info("Particle effect {}", enabled ? "enabled" : "disabled");
    }
    
    public static void setParticleEffectColor(int color) {
        particleEffectColor = color;
        VSBaseMod.LOGGER.info("Particle effect color set to 0x{}", Integer.toHexString(color));
    }
    
    public static boolean isParticleEffectEnabled() { return particleEffectEnabled; }
    public static int getParticleEffectColor() { return particleEffectColor; }
    
    // ========== ВТОРОЙ ЭФФЕКТ - МИНИ БЛОКИ ==========
    public static void setBlockEffectEnabled(boolean enabled) {
        blockEffectEnabled = enabled;
        VSBaseMod.LOGGER.info("Block effect {}", enabled ? "enabled" : "disabled");
    }
    
    public static void setBlockEffectColor(int color) {
        blockEffectColor = color;
        VSBaseMod.LOGGER.info("Block effect color set to 0x{}", Integer.toHexString(color));
    }
    
    public static boolean isBlockEffectEnabled() { return blockEffectEnabled; }
    public static int getBlockEffectColor() { return blockEffectColor; }
    
    // ========== ОБЩАЯ ФУНКЦИЯ CLEAR ==========
    public static void clearAllEffects() {
        particleEffectEnabled = false;
        blockEffectEnabled = false;
        VSBaseMod.LOGGER.info("All effects cleared");
    }
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.world == null) return;
        
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
        
        // Второй эффект - мини блоки (каждые 150мс)
        if (blockEffectEnabled && currentTime - lastBlockTime > 150) {
            spawnBlockEffect(mc, pos);
            lastBlockTime = currentTime;
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
    
    // ========== ВТОРОЙ ЭФФЕКТ - МИНИ БЛОКИ ==========
    private static void spawnBlockEffect(Minecraft mc, BlockPos pos) {
        if (mc.world == null) return;
        
        // Получаем тип блока по цвету
        BlockState blockState = getBlockStateForColor(blockEffectColor);
        
        // 8 углов блока (координаты относительно блока: 0 или 1)
        double[][] corners = {
            {0, 0, 0},  // 0: нижний левый передний
            {1, 0, 0},  // 1: нижний правый передний
            {0, 1, 0},  // 2: верхний левый передний
            {1, 1, 0},  // 3: верхний правый передний
            {0, 0, 1},  // 4: нижний левый задний
            {1, 0, 1},  // 5: нижний правый задний
            {0, 1, 1},  // 6: верхний левый задний
            {1, 1, 1}   // 7: верхний правый задний
        };
        
        // Соседние углы для каждого угла (только по осям X, Y, Z)
        int[][] neighbors = {
            {1, 2, 4},        // 0 → 1 (по X), 0 → 2 (по Y), 0 → 4 (по Z)
            {0, 3, 5},        // 1 → 0 (по X), 1 → 3 (по Y), 1 → 5 (по Z)
            {0, 3, 6},        // 2 → 0 (по Y), 2 → 3 (по X), 2 → 6 (по Z)
            {1, 2, 7},        // 3 → 1 (по Y), 3 → 2 (по X), 3 → 7 (по Z)
            {0, 5, 6},        // 4 → 0 (по Z), 4 → 5 (по X), 4 → 6 (по Y)
            {1, 4, 7},        // 5 → 1 (по Z), 5 → 4 (по X), 5 → 7 (по Y)
            {2, 4, 7},        // 6 → 2 (по Z), 6 → 4 (по Y), 6 → 7 (по X)
            {3, 5, 6}         // 7 → 3 (по Z), 7 → 5 (по Y), 7 → 6 (по X)
        };
        
        // Для каждого угла создаем частицу, летящую к соседнему углу
        for (int i = 0; i < corners.length; i++) {
            // Выбираем случайного СОСЕДА (не любой угол, а только соединенный ребром)
            int[] neighborIndices = neighbors[i];
            int randomNeighbor = neighborIndices[mc.world.rand.nextInt(neighborIndices.length)];
            
            // Позиция старта (абсолютные координаты мира)
            double startX = pos.getX() + corners[i][0];
            double startY = pos.getY() + corners[i][1];
            double startZ = pos.getZ() + corners[i][2];
            
            // Позиция цели (абсолютные координаты мира)
            double targetX = pos.getX() + corners[randomNeighbor][0];
            double targetY = pos.getY() + corners[randomNeighbor][1];
            double targetZ = pos.getZ() + corners[randomNeighbor][2];
            
            // Направление ОТ текущего угла К соседнему углу
            double dirX = targetX - startX;
            double dirY = targetY - startY;
            double dirZ = targetZ - startZ;
            
            // Нормализуем вектор направления и задаем скорость
            double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
            if (length > 0) {
                dirX = (dirX / length) * 0.15;
                dirY = (dirY / length) * 0.15;
                dirZ = (dirZ / length) * 0.15;
            }
            
            // Создаем частицу блока (FIX: убран setScale - не поддерживается в 1.16.5)
            BlockParticleData particleData = new BlockParticleData(ParticleTypes.BLOCK, blockState);
            mc.world.addParticle(particleData, startX, startY, startZ, dirX, dirY, dirZ);
        }
    }
    
    // ========== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ==========
    private static BlockState getBlockStateForColor(int color) {
        if (color == COLOR_PURPLE) {
            return Blocks.PURPLE_CONCRETE.getDefaultState();
        } else if (color == COLOR_LIME) {
            return Blocks.LIME_CONCRETE.getDefaultState();
        } else if (color == COLOR_RED) {
            return Blocks.RED_CONCRETE.getDefaultState();
        }
        return Blocks.PURPLE_CONCRETE.getDefaultState();
    }
    
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
