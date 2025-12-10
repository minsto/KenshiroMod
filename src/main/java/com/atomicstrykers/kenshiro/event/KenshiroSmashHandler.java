package com.atomicstrykers.kenshiro.event;

import com.atomicstrykers.kenshiro.KenshiroMod;
import com.atomicstrykers.kenshiro.registry.KenshiroSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class KenshiroSmashHandler {

    private static final String NBT_SMASH_COUNT = "kenshiro_smash_count";
    private static final int HEARTBEAT_EVERY = 3;
    private static void startHeartbeat(Minecraft mc) {


        mc.player.playSound(KenshiroSounds.KENSHIRO_HEARTBEAT.value(), 1.0F, 1.0F);

    }
    private static void startCharge(Minecraft mc) {


        mc.player.playSound(KenshiroSounds.KENSHIRO_SMASH.value(), 1.0F, 1.0F);

    }


    public static void doSmash(ServerPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        // On est côté serveur
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        // Mains nues obligatoires
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) {
            return;
        }

        // === 1) Récupérer le BLOC VISÉ par le joueur (comme la main qui mine) ===
        // 4.5 blocs de portée, pas de fluide
        HitResult hit = player.pick(4.5D, 0.0F, false);

        if (hit.getType() != HitResult.Type.BLOCK) {
            // rien de ciblé -> on annule
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos targetPos = blockHit.getBlockPos();

        // On choisit comme centre le bloc visé ou celui en dessous
        BlockPos center = targetPos;
        // si tu veux vraiment la couche du sol sous le bloc visé :
        // center = targetPos.below();

        // === 2) Casser les blocs en 5x5 autour du centre (2 couches de hauteur) ===
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);

                    if (state.isAir() || state.is(Blocks.BEDROCK)) {
                        continue;
                    }

                    // Particules du bloc
                    level.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, state),
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            40,
                            0.3, 0.3, 0.3,
                            0.15
                    );

                    // Casse le bloc avec loot
                    level.destroyBlock(pos, true, player);
                }
            }
        }

        // === 3) Onde de choc : repousse les mobs autour du joueur ===
        double radius = 6.0D;
        AABB zone = new AABB(
                player.getX() - radius, player.getY() - 1, player.getZ() - radius,
                player.getX() + radius, player.getY() + 2, player.getZ() + radius
        );

        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                zone,
                e -> e != player && e.isAlive()
        )) {
            Vec3 dir = entity.position().subtract(player.position());
            double dist = dir.length();
            if (dist < 0.001) continue;

            double strength = 1.2 + (radius - dist) * 0.18;
            Vec3 push = dir.normalize().scale(strength);

            entity.push(push.x, 0.45, push.z);
        }

        // === 4) Anneau de particules (onde visible) ===
        double ringRadius = 4.0D;
        for (int i = 0; i < 40; i++) {
            double angle = (Math.PI * 2.0 * i) / 40.0;
            double px = player.getX() + Math.cos(angle) * ringRadius;
            double pz = player.getZ() + Math.sin(angle) * ringRadius;
            double py = player.getY() + 0.2;

            level.sendParticles(
                    ParticleTypes.CLOUD,
                    px, py, pz,
                    3,
                    0.1, 0.05, 0.1,
                    0.0
            );
        }

        // === 5) Son du smash ===
      startCharge(mc);

        // === 6) Animation de coup ===
        player.swing(InteractionHand.MAIN_HAND, true);

        // === 7) Compteur pour heartbeat toutes les 3 utilisations ===
        CompoundTag tag = player.getPersistentData();
        int count = tag.getInt(NBT_SMASH_COUNT).orElse(0);
        count++;
        tag.putInt(NBT_SMASH_COUNT, count);

        if (count >= HEARTBEAT_EVERY) {
            tag.putInt(NBT_SMASH_COUNT, 0);
            startHeartbeat(mc);
        }

        KenshiroMod.LOGGER.debug("Kenshiro smash exécuté, count={}", tag.getInt(NBT_SMASH_COUNT));
    }
}