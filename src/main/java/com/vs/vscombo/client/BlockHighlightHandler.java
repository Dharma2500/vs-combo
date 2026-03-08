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
                dirX = (dirX / length) * 0.15;
                dirY = (dirY / length) * 0.15 + 0.1;
                dirZ = (dirZ / length) * 0.15;
            }
            
            // FIX: Используем BlockParticleData для Forge 1.16.5
            BlockParticleData particleData = new BlockParticleData(ParticleTypes.BLOCK, blockState);
            mc.world.addParticle(particleData, corner[0], corner[1], corner[2], dirX, dirY, dirZ);
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
