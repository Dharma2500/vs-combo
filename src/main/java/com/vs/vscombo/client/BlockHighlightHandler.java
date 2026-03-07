package com.vs.vscombo.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.vs.vscombo.VSBaseMod;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VSBaseMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockHighlightHandler {
    
    // Временная заглушка - код отключён из-за проблем с маппингами
    private static boolean effectEnabled = false;
    private static int effectColor = 0xFF800080;
    
    public static void setEffectEnabled(boolean enabled) {
        effectEnabled = enabled;
        VSBaseMod.LOGGER.info("Block highlight {}", enabled ? "enabled" : "disabled");
    }
    
    public static void setEffectColor(int color) {
        effectColor = color;
        VSBaseMod.LOGGER.info("Block highlight color set to {}", color);
    }
    
    public static boolean isEffectEnabled() { return effectEnabled; }
    public static int getEffectColor() { return effectColor; }
    public static void clearEffect() { effectEnabled = false; }
    
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        // FIX: Временно отключено из-за проблем с маппингами
        // Раскомментируйте, когда маппинги будут работать корректно
        /*
        if (!effectEnabled) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.world == null) return;
        
        MatrixStack matrixStack = event.getMatrixStack();
        
        if (mc.objectMouseTarget != null && mc.objectMouseTarget.getType() == BlockRayTraceResult.Type.BLOCK) {
            BlockPos pos = ((BlockRayTraceResult) mc.objectMouseTarget).getPos();
            drawBlockOutline(matrixStack, pos, effectColor, event.getPartialTicks());
        }
        */
    }
    
    // Методы отрисовки временно отключены
    private static void drawBlockOutline(MatrixStack matrixStack, BlockPos pos, int color, float partialTicks) {
        // Заглушка
    }
}
