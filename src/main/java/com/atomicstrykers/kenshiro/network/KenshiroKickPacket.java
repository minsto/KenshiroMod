package com.atomicstrykers.kenshiro.network;

import com.atomicstrykers.kenshiro.KenshiroMod;
import com.atomicstrykers.kenshiro.event.KenshiroKickHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KenshiroKickPacket() implements CustomPacketPayload {

    public static final Type<KenshiroKickPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(KenshiroMod.MOD_ID, "kenshiro_kick"));

    // codec NeoForge 1.21.x
    public static final StreamCodec<RegistryFriendlyByteBuf, KenshiroKickPacket> STREAM_CODEC =
            CustomPacketPayload.codec(KenshiroKickPacket::write, KenshiroKickPacket::new);

    // ctor lecture
    public KenshiroKickPacket(FriendlyByteBuf buf) {
        this();
    }

    public void write(FriendlyByteBuf buf) {
        // pas de données
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(KenshiroKickPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) {
                KenshiroKickHandler.doKick(sp);
            }
        });
    }
}
