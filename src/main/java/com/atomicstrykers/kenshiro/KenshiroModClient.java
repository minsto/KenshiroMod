package com.atomicstrykers.kenshiro;



import com.atomicstrykers.kenshiro.Config.KenshiroConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = KenshiroMod.MOD_ID, dist = Dist.CLIENT)
public class KenshiroModClient {

    public KenshiroModClient(ModContainer container, IEventBus modBus) {

        KenshiroConfig.registerClientScreen(container);

        modBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        KenshiroMod.LOGGER.info("Client setup KenshiroMod.");
        // bind HUD, keybinds, renderers d’items spéciaux etc.
    }
}
