package com.atomicstrykers.kenshiro.Config;

import java.util.List;

import com.atomicstrykers.kenshiro.KenshiroMod;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;

import com.atomicstrykers.kenshiro.KenshiroMod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class KenshiroConfig {

    private KenshiroConfig() {}

    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_KENSHIRO_PUNCH;
    public static final ModConfigSpec.IntValue KENSHIRO_BASE_DAMAGE;
    public static final ModConfigSpec.BooleanValue ENABLE_AURA;

    static {
        COMMON_BUILDER.push("combat");
        ENABLE_KENSHIRO_PUNCH = COMMON_BUILDER
                .comment("Activate Kenshiro's special punches (true/false).")
                .define("enableKenshiroPunch", true);
        KENSHIRO_BASE_DAMAGE = COMMON_BUILDER
                .comment("Kenshiro's fist base damage.")
                .defineInRange("kenshiroBaseDamage", 2, 1, 100);

        ENABLE_AURA = COMMON_BUILDER
                .comment("Activate Kenshiro's aura.")
                .define("enableAura", true);

        COMMON_BUILDER.pop();
        COMMON_SPEC = COMMON_BUILDER.build();
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    public static void registerClientScreen(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}