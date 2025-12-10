package com.atomicstrykers.kenshiro.network;

import com.atomicstrykers.kenshiro.KenshiroMod;
import com.atomicstrykers.kenshiro.event.KenshiroCommonEvents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = KenshiroMod.MOD_ID)
public class KenshiroNetwork {

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(KenshiroMod.MOD_ID).versioned("2");

        registrar.playToServer(
                KenshiroTechniquePacket.TYPE,
                KenshiroTechniquePacket.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> handleTechnique(ctx))
        );
        registrar.playToServer(
                KenshiroSmashPacket.TYPE,
                KenshiroSmashPacket.STREAM_CODEC,
                KenshiroSmashPacket::handle
        );


    }

    private static void handleTechnique(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            KenshiroCommonEvents.startTechnique(player);
        }
    }
}