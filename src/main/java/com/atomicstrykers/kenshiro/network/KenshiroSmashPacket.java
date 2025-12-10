package com.atomicstrykers.kenshiro.network;

import com.atomicstrykers.kenshiro.KenshiroMod;
import com.atomicstrykers.kenshiro.event.KenshiroSmashHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KenshiroSmashPacket() implements CustomPacketPayload {

    // Type du payload
    public static final Type<KenshiroSmashPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(KenshiroMod.MOD_ID, "kenshiro_smash"));

    // StreamCodec obligatoire pour NeoForge 21.10
    public static final StreamCodec<RegistryFriendlyByteBuf, KenshiroSmashPacket> STREAM_CODEC =
            StreamCodec.ofMember(KenshiroSmashPacket::write, KenshiroSmashPacket::new);

    // constructeur de lecture
    public KenshiroSmashPacket(RegistryFriendlyByteBuf buf) {
        this(); // pas de data à lire
    }

    // écriture (rien à écrire)
    public void write(RegistryFriendlyByteBuf buf) {
        // vide
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // handler côté serveur
    public static void handle(KenshiroSmashPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) {
                KenshiroSmashHandler.doSmash(sp);
            }
        });
    }
}