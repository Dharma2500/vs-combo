package com.vs.vscombo;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod("vscombo")
public class VSBaseMod {
    public static final String MOD_ID = "vscombo";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    // Centralized Registry Holder
    public static final DeferredRegistry REGISTRY = new DeferredRegistry();

    public VSBaseMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Initialize VS Core components
        REGISTRY.register(modEventBus);
        
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("VS Base initialized. Architecture: Secure. Side: Client.");
    }
}
