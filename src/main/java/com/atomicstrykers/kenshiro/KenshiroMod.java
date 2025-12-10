package com.atomicstrykers.kenshiro;

import com.atomicstrykers.kenshiro.Config.KenshiroConfig;
import com.atomicstrykers.kenshiro.registry.KenshiroSounds;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;




import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(KenshiroMod.MOD_ID)
public class KenshiroMod {

    public static final String MOD_ID = "kenshiromod"; // même valeur que dans neoforge.mods.toml
    public static final Logger LOGGER = LogUtils.getLogger();

    public KenshiroMod(IEventBus modBus, ModContainer container) {

        // Registre d’items


        // Config
        KenshiroConfig.register(container);
        KenshiroSounds.SOUNDS.register(modBus);
        // Lifecycle
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::addItemsToCreativeTabs);

        // Events de jeu (commun / serveur)
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);

        LOGGER.info("KenshiroMod Load.");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Common setup KenshiroMod.");
        // init réseau, capacités, etc.
    }

    private void addItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {

    }

    private void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Serveur for KenshiroMod starting.");
    }
}