package com.vs.vscombo;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("vscombo")
public class VSBaseMod {
    public static final String MOD_ID = "vscombo";
    public static final Logger LOGGER = LogManager.getLogger();
    
    public VSBaseMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        com.vs.vscombo.common.registry.DeferredRegistry.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("VS Base initialized. Forge 1.16.5 compatible.");
    }
}
