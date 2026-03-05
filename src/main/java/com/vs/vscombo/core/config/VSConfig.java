package com.vs.vscombo.core.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class VSConfig {
    
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    public static final ForgeConfigSpec.ConfigValue<String> MAIN_WINDOW_KEY;
    
    static {
        BUILDER.push("interface");
        MAIN_WINDOW_KEY = BUILDER
                .comment("Default key to open VS Universe")
                .define("mainWindowKey", "X");
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
    
    public static void load() {}
}
