package com.vs.vscombo.client;

import com.vs.vscombo.VSBaseMod;
import net.minecraft.client.Minecraft;
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
    
    // ========== ВТОРОЙ ЭФФЕКТ - МАЛЕНЬКИЕ ЦВЕТНЫЕ ЧАСТИЦЫ ==========
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
        
        if (mc.objectMouseOver == null || mc.objectMouseOver.getType() != net.minecraft.util.math.BlockRayTraceResult.Type.BLOCK) {
            return;
        }
        
        BlockPos pos = ((BlockRayTraceResult) mc.objectMouseOver).getPos();
        long currentTime = System.currentTimeMillis();
        
        // Первый эффект - цветные частицы (каждые 100мс)
        if (particleEffectEnabled && currentTime - lastParticleTime > 100) {
            spawnParticleEffect(mc, pos);
            lastParticleTime = currentTime;
        }
        
        // Второй эффект - маленькие цветные частицы (каждые 150мс)
        if (blockEffectEnabled && currentTime - lastBlockTime > 150) {
            spawnSmallColoredParticles(mc, pos);
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
    
// ========== ВТОРОЙ ЭФФЕКТ - МАЛЕНЬКИЕ ЦВЕТНЫЕ ЧАСТИЦЫ ==========
private static void spawnSmallColoredParticles(Minecraft mc, BlockPos pos) {
    if (mc.world == null) return;
    
    // 8 углов блока
    double[][] corners = {
        {0, 0, 0},  // 0
        {1, 0, 0},  // 1
        {0, 1, 0},  // 2
        {1, 1, 0},  // 3
        {0, 0, 1},  // 4
        {1, 0, 1},  // 5
        {0, 1, 1},  // 6
        {1, 1, 1}   // 7
    };
    
    // Соседние углы (только по осям)
    int[][] neighbors = {
        {1, 2, 4},   // 0 → 1(X), 2(Y), 4(Z)
        {0, 3, 5},   // 1 → 0(X), 3(Y), 5(Z)
        {0, 3, 6},   // 2 → 0(Y), 3(X), 6(Z)
        {1, 2, 7},   // 3 → 1(Y), 2(X), 7(Z)
        {0, 5, 6},   // 4 → 0(Z), 5(X), 6(Y)
        {1, 4, 7},   // 5 → 1(Z), 4(X), 7(Y)
        {2, 4, 7},   // 6 → 2(Z), 4(Y), 7(X)
        {3, 5, 6}    // 7 → 3(Z), 5(Y), 6(X)
    };
    
    // Получаем цвет для текущего эффекта
    float red = getRed(blockEffectColor);
    float green = getGreen(blockEffectColor);
    float blue = getBlue(blockEffectColor);
    
    // Для каждого угла создаем несколько маленьких частиц
    for (int i = 0; i < corners.length; i++) {
        // Выбираем случайного соседа
        int[] neighborIndices = neighbors[i];
        int targetIndex = neighborIndices[mc.world.rand.nextInt(neighborIndices.length)];
        
        // Позиция старта
        double startX = pos.getX() + corners[i][0];
        double startY = pos.getY() + corners[i][1];
        double startZ = pos.getZ() + corners[i][2];
        
        // Позиция цели
        double targetX = pos.getX() + corners[targetIndex][0];
        double targetY = pos.getY() + corners[targetIndex][1];
        double targetZ = pos.getZ() + corners[targetIndex][2];
        
        // Направление ОТ старта К цели
        double dirX = targetX - startX;
        double dirY = targetY - startY;
        double dirZ = targetZ - startZ;
        
        // Нормализуем и задаем скорость
        double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (length > 0) {
            dirX = (dirX / length) * 0.12;
            dirY = (dirY / length) * 0.12;
            dirZ = (dirZ / length) * 0.12;
        }
        
        // Создаем несколько маленьких цветных частиц С ПРАВИЛЬНЫМ ЦВЕТОМ
        for (int j = 0; j < 3; j++) {
            mc.world.addParticle(
                ParticleTypes.ENTITY_EFFECT,
                startX, startY, startZ,
                red, green, blue  // ← FIX: Передаем цвет!
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
