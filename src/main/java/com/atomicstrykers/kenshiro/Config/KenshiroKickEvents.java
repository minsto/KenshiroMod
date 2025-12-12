package com.atomicstrykers.kenshiro.Config;

import com.atomicstrykers.kenshiro.KenshiroMod;
import com.atomicstrykers.kenshiro.network.KenshiroKickPacket;
import com.atomicstrykers.kenshiro.network.KenshiroSmashPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = KenshiroMod.MOD_ID, value = Dist.CLIENT)
public class KenshiroKickEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;

        // Smash (touche B)
        if (KenshiroKeyMappings.KICK_KEY!= null) {
            while (KenshiroKeyMappings.KICK_KEY.consumeClick()) {
                mc.getConnection().send(new KenshiroKickPacket());
            }
        }
    }
}
