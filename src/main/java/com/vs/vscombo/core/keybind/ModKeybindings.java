package com.vs.vscombo.core.keybind;

import com.vs.vscombo.VSBaseMod;
import com.vs.vscombo.core.gui.VSMainWindow;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

// FIX: bus = Mod.EventBusSubscriber.Bus.FORGE для runtime-событий
@Mod.EventBusSubscriber(modid = VSBaseMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModKeybindings {

    public static final KeyBinding OPEN_VS_MENU = new KeyBinding(
            "key.vscombo.open_menu",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "Vitaly_Sokolov Universe"
    );

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        ClientRegistry.registerKeyBinding(OPEN_VS_MENU);
        // Не регистрируем этот класс на mod bus - он уже зарегистрирован на FORGE bus через аннотацию
        VSBaseMod.LOGGER.debug("VS Keybindings: Registered on FORGE bus.");
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        while (OPEN_VS_MENU.isPressed()) {
            VSMainWindow.toggle();
        }
    }
}
