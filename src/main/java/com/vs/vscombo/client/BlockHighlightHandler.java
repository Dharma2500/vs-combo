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
    
    private static boolean particleEffectEnabled = false;
    private static int particleEffectColor = 0xFF800080;
    private static long lastParticleTime = 0;
    
    private static boolean blockEffectEnabled = false;
    private static int blockEffectColor = 0xFF800080;
    private static long lastBlockTime = 0;
    
    // ========== ДЛЯ ЭФФЕКТА СХЛОПЫВАНИЯ ==========
    private static BlockPos lastTargetBlock = null;
    private static long collapseStartTime = 0;
    private static boolean isCollapsing = false;
    private static int collapseTicks = 0;
    
    private static final int COLOR_PORTAL = 0xFF800080;
    private static final int COLOR_LIME = 0xFF00FF00;
    private static final int COLOR_RED = 0xFFFF0000;
    
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
    
    public static void setBlockEffectEnabled(boolean enabled) {
        blockEffectEnabled = enabled;
        if (!enabled) {
            lastTargetBlock = null;
            isCollapsing = false;
        }
        VSBaseMod.LOGGER.info("Block effect {}", enabled ? "enabled" : "disabled");
    }
    
    public static void setBlockEffectColor(int color) {
        blockEffectColor = color;
        VSBaseMod.LOGGER.info("Block effect color set to 0x{}", Integer.toHexString(color));
    }
    
    public static boolean isBlockEffectEnabled() { return blockEffectEnabled; }
    public static int getBlockEffectColor() { return blockEffectColor; }
    
    public static void clearAllEffects() {
        particleEffectEnabled = false;
        blockEffectEnabled = false;
        lastTargetBlock = null;
        isCollapsing = false;
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
        
        BlockPos currentPos = ((BlockRayTraceResult) mc.objectMouseOver).getPos();
        long currentTime = System.currentTimeMillis();
        
        // ========== ПРОВЕРКА НА СХЛОПЫВАНИЕ ==========
        if (blockEffectEnabled && blockEffectColor == COLOR_PORTAL) {
            if (lastTargetBlock != null && !lastTargetBlock.equals(currentPos)) {
                // Игрок отвел прицел от блока - запускаем схлопывание
                startCollapse(lastTargetBlock);
            }
            lastTargetBlock = currentPos;
        } else {
            lastTargetBlock = null;
        }
        
        // ========== ЭФФЕКТ СХЛОПЫВАНИЯ ==========
        if (isCollapsing && lastTargetBlock != null) {
            spawnPortalCollapse(mc, lastTargetBlock);
            collapseTicks++;
            
            // Схлопывание длится 1 секунду (20 тиков)
            if (collapseTicks > 20) {
                isCollapsing = false;
                collapseTicks = 0;
                lastTargetBlock = null;
            }
        }
        
        // ========== ОБЫЧНЫЙ ЭФФЕКТ ==========
        if (particleEffectEnabled && currentTime - lastParticleTime > 100) {
            spawnParticleEffect(mc, currentPos);
            lastParticleTime = currentTime;
        }
        
        if (blockEffectEnabled && currentTime - lastBlockTime > 100) {
            spawnBlockParticles(mc, currentPos);
            lastBlockTime = currentTime;
        }
    }
    
    private static void startCollapse(BlockPos pos) {
        isCollapsing = true;
        collapseStartTime = System.currentTimeMillis();
        collapseTicks = 0;
        VSBaseMod.LOGGER.info("Portal collapsing at {}", pos);
    }
    
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
    
    private static void spawnBlockParticles(Minecraft mc, BlockPos pos) {
        double[][] corners = {
            {0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {1, 1, 0},
            {0, 0, 1}, {1, 0, 1}, {0, 1, 1}, {1, 1, 1}
        };
        
        int[][] neighbors = {
            {1, 2, 4}, {0, 3, 5}, {0, 3, 6}, {1, 2, 7},
            {0, 5, 6}, {1, 4, 7}, {2, 4, 7}, {3, 5, 6}
        };
        
        BasicParticleType particleType;
        
        if (blockEffectColor == COLOR_RED) {
            particleType = ParticleTypes.FLAME;
        } else if (blockEffectColor == COLOR_PORTAL) {
            particleType = ParticleTypes.PORTAL;
        } else {
            particleType = ParticleTypes.HAPPY_VILLAGER;
        }
        
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
                dirX = (dirX / length) * 0.08;
                dirY = (dirY / length) * 0.08;
                dirZ = (dirZ / length) * 0.08;
            }
            
            for (int j = 0; j < 2; j++) {
                mc.world.addParticle(
                    (IParticleData) particleType,
                    startX, startY, startZ,
                    dirX * 0.5, dirY * 0.5, dirZ * 0.5
                );
            }
        }
    }
    
    // ========== НОВЫЙ ЭФФЕКТ: СХЛОПЫВАНИЕ ПОРТАЛА К ЦЕНТРУ ==========
    private static void spawnPortalCollapse(Minecraft mc, BlockPos pos) {
        if (mc.world == null) return;
        
        // Центр блока
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        
        // 8 углов блока
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
        
        // Спавним частицы из каждого угла, летящие К центру
        for (double[] corner : corners) {
            // Направление ОТ угла К центру
            double dirX = centerX - corner[0];
            double dirY = centerY - corner[1];
            double dirZ = centerZ - corner[2];
            
            double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
            if (length > 0) {
                dirX = (dirX / length) * 0.15;
                dirY = (dirY / length) * 0.15;
                dirZ = (dirZ / length) * 0.15;
            }
            
            // Создаем 4 частицы на угол для эффекта схлопывания
            for (int i = 0; i < 4; i++) {
                mc.world.addParticle(
                    ParticleTypes.PORTAL,
                    corner[0], corner[1], corner[2],
                    dirX, dirY, dirZ
                );
            }
        }
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
