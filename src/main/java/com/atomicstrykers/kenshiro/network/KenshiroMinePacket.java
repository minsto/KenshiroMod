package com.atomicstrykers.kenshiro.network;

import com.atomicstrykers.kenshiro.KenshiroMod;
import com.atomicstrykers.kenshiro.event.KenshiroMineHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.atomicstrykers.kenshiro.KenshiroMod;
import com.atomicstrykers.kenshiro.event.KenshiroMineHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KenshiroMinePacket(BlockPos pos) implements CustomPacketPayload {

    public static final Type<KenshiroMinePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    KenshiroMod.MOD_ID,
                    "kenshiro_mine"
            ));

    // ✅ Codec utilisé dans registrar.playToServer(...)
    public static final StreamCodec<FriendlyByteBuf, KenshiroMinePacket> STREAM_CODEC =
            StreamCodec.of(
                    // encode
                    (buf, pkt) -> pkt.write(buf),
                    // decode
                    KenshiroMinePacket::new
            );

    // ===== Constructeur de lecture =====
    public KenshiroMinePacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    // ===== Ecriture =====
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // ===== Handler coté serveur =====
    public static void handle(KenshiroMinePacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) {
                KenshiroMineHandler.doMine(sp, pkt.pos());
            }
        });
    }
}