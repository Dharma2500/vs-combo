package com.vs.vscombo.common.registry;

import com.vs.vscombo.VSBaseMod;
import com.vs.vscombo.core.config.VSConfig;
import com.vs.vscombo.core.keybind.ModKeybindings;
import net.minecraftforge.eventbus.api.IEventBus;

public class DeferredRegistry {
    
    public static void register(IEventBus bus) {
        VSConfig.load();
        ModKeybindings.register(bus);
        VSBaseMod.LOGGER.debug("VS Registry: All systems operational.");
    }
}
