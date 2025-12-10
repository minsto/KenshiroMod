package com.atomicstrykers.kenshiro.network;

import com.atomicstrykers.kenshiro.KenshiroMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record KenshiroTechniquePacket() implements CustomPacketPayload {

    public static final Type<KenshiroTechniquePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(KenshiroMod.MOD_ID, "kenshiro_technique"));

    // Pas de données dans ce packet, donc codec vide
    public static final StreamCodec<FriendlyByteBuf, KenshiroTechniquePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {},         // encode
                    buf -> new KenshiroTechniquePacket() // decode
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
