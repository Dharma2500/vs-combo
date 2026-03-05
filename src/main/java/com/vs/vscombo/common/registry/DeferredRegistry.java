package com.vs.vscombo.common.registry;

import com.vs.vscombo.VSBaseMod;
import com.vs.vscombo.common.network.PacketHandler;
import com.vs.vscombo.core.config.VSConfig;
import com.vs.vscombo.core.keybind.ModKeybindings;
import net.minecraftforge.eventbus.api.IEventBus;

public class DeferredRegistry {
    
    public void register(IEventBus bus) {
        // Layer 1: Config initialization
        VSConfig.load();
        
        // Layer 2: Network setup (Client-bound only)
        PacketHandler.register();
        
        // Layer 3: Input handlers
        ModKeybindings.register(bus);
        
        VSBaseMod.LOGGER.debug("VS Registry: All systems operational.");
    }
}
