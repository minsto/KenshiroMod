package com.atomicstrykers.kenshiro.Config;

import com.atomicstrykers.kenshiro.KenshiroMod;
import com.atomicstrykers.kenshiro.event.KenshiroPunchHandler;
import com.atomicstrykers.kenshiro.network.KenshiroMinePacket;
import com.atomicstrykers.kenshiro.network.KenshiroSmashPacket;
import com.atomicstrykers.kenshiro.network.KenshiroTechniquePacket;
import com.atomicstrykers.kenshiro.registry.KenshiroSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import static net.minecraft.world.level.block.SculkSensorBlock.COOLDOWN_TICKS;

@EventBusSubscriber(modid = KenshiroMod.MOD_ID, value = Dist.CLIENT)
public class KenshiroClientEvents {
    public static boolean styleActive = false;
    private static final int COOLDOWN_TICKS = 5 * 20; // 5 secondes
    private static int lastTechniqueEndTick = -COOLDOWN_TICKS;
    private static void mineTargetedBlock(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (player == null || mc.level == null || mc.getConnection() == null) return;

        double reach = 4.5;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.x * reach, look.y * reach, look.z * reach);

        ClipContext context = new ClipContext(
                eye,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        );

        BlockHitResult hit = mc.level.clip(context);
        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = hit.getBlockPos();

        // ⬇️ Envoie au serveur: “casse ce bloc comme une pioche OP”
        mc.getConnection().send(new KenshiroMinePacket(pos));
    }

    // Version pour serveur (si jamais nécessaire)
    private static void simulateLeftClickHold(ServerPlayer player, int durationTicks) {
        BlockPos target = getTargetBlockPos(player);
        if (target == null) return;

        ServerLevel level = player.level();

        // Simuler la progression du minage
        for (int tick = 0; tick < durationTicks; tick++) {
            if (tick % 5 == 0) { // Tous les 5 ticks
                // Envoyer le paquet de progression au client
                player.connection.send(new ClientboundBlockUpdatePacket(target,
                        level.getBlockState(target)));

                // Jouer le son de minage
            }

            // À la fin, miner le bloc
            if (tick == durationTicks - 1) {
                player.gameMode.destroyBlock(target);

                // Faire tomber les items
                BlockState state = level.getBlockState(target);
                Block.dropResources(state, level, target,
                        level.getBlockEntity(target), player, player.getMainHandItem());
            }
        }
    }

    private static BlockPos getTargetBlockPos(ServerPlayer player) {
        double reach = 4.5;
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eyePos.add(look.x * reach, look.y * reach, look.z * reach);

        ClipContext context = new ClipContext(eyePos, end,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        BlockHitResult hit = player.level().clip(context);

        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getBlockPos();
        }
        return null;
    }

    private enum TechniquePhase {
        NONE,
        CHARGE,
        STYLE,
        SHINDEIRU,
        HEARTBEAT
    }

    private static TechniquePhase phase = TechniquePhase.NONE;
    private static int phaseTicks = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 1) Gestion de la touche R
        if (KenshiroKeyMappings.TECHNIQUE_KEY != null) {
            while (KenshiroKeyMappings.TECHNIQUE_KEY.consumeClick()) {
                if (mc.player == null) break;

                // Si une technique est déjà en cours, on ignore
                if (phase != TechniquePhase.NONE) {
                    continue;
                }

                int now = mc.player.tickCount;
                int elapsed = now - lastTechniqueEndTick;

                // Cooldown pas terminé
                if (elapsed < COOLDOWN_TICKS) {
                    // ⚠ Punition : 3 dégâts
                    float newHp = mc.player.getHealth() - 3.0F;
                    if (newHp < 0.0F) newHp = 0.0F;
                    mc.player.setHealth(newHp);

                    // petit son de hurt vanilla (optionnel)
                    mc.player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_HURT, 1.0F, 1.0F);
                    continue;
                }

                // Cooldown ok → on lance la technique
                if (mc.getConnection() != null) {
                    mc.getConnection().send(new KenshiroTechniquePacket());
                }
                startCharge(mc);
            }
        }

        // 2) Avancement de la séquence de sons
        if (phase == TechniquePhase.NONE) return;

        phaseTicks++;

        switch (phase) {
            case CHARGE -> {
                if (phaseTicks > 40) { // ~2 secondes
                    startStyle(mc);
                }
            }
            case STYLE -> {
                if (phaseTicks > 60) { // ~3 secondes
                    startShindeiru(mc);
                }
            }
            case SHINDEIRU -> {
                if (phaseTicks > 40) { // ~2 secondes
                    startHeartbeat(mc);
                }
            }
            case HEARTBEAT -> {
                if (phaseTicks > 100) { // ~5 secondes
                    // On mémorise le tick de fin
                    if (mc.player != null) {
                        startHeartbeat(mc);
                        lastTechniqueEndTick = mc.player.tickCount;
                    }
                    reset();
                }
            }
        }

        // 3) Animation des bras PENDANT la phase STYLE (en dehors du switch)
        if (phase == TechniquePhase.STYLE && mc.player != null) {
            LocalPlayer player = mc.player;

            // Poing droit (main hand)
            if (player.tickCount % 4 == 0) {
                player.swing(InteractionHand.MAIN_HAND, true);
                boolean entityHit = tryHitTarget(mc);  // <-- frappe l'entité si il y en a une

                // Si aucune entité n'a été touchée, miner le bloc regardé
                if (!entityHit) {
                    mineTargetedBlock(player);
                }
            }

            // Poing gauche (off hand)
            if (player.tickCount % 3 == 0) {
                player.swing(InteractionHand.OFF_HAND, true);
                boolean entityHit = tryHitTarget(mc);

                // Si aucune entité n'a été touchée, miner le bloc regardé
                if (!entityHit) {
                    mineTargetedBlock(player);
                }
            }
        }
    }

    private static boolean tryHitTarget(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return false;

        // Raycast actuel du joueur
        var hit = mc.hitResult;
        if (hit instanceof net.minecraft.world.phys.EntityHitResult ehr) {
            var entity = ehr.getEntity();

            // Appelle l'attaque vanilla coté client -> envoie au serveur
            mc.gameMode.attack(mc.player, entity);
            return true; // Une entité a été touchée
        }
        return false; // Aucune entité touchée
    }

    private static void punchHitServer(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;

        // Optionnel : on check s'il y a quelque chose devant pour éviter de spam le serveur
        Vec3 look = player.getLookAngle().normalize();
        Vec3 eye = player.getEyePosition();
        double range = 2.2D;

        AABB box = new AABB(
                eye.x - 0.5, eye.y - 0.5, eye.z - 0.5,
                eye.x + 0.5, eye.y + 0.5, eye.z + 0.5
        ).expandTowards(look.scale(range));

        boolean hasTarget = player.level().getEntities(
                player,
                box,
                e -> e instanceof LivingEntity && e.isAlive()
        ).stream().findFirst().isPresent();

        if (!hasTarget) {
            return;
        }

        // Envoie du packet au serveur → le handler fera les dégâts + son
    }

    private static void startCharge(Minecraft mc) {
        phase = TechniquePhase.CHARGE;
        phaseTicks = 0;
        if (mc.player != null) {
            mc.player.playSound(KenshiroSounds.KENSHIRO_CHARGE.value(), 1.0F, 1.0F);
        }
    }

    private static void startStyle(Minecraft mc) {
        phase = TechniquePhase.STYLE;
        styleActive = true;
        phaseTicks = 0;
        if (mc.player != null) {
            mc.player.playSound(KenshiroSounds.KENSHIRO_STYLE.value(), 0.5F, 1.0F);
        }
    }

    private static void startShindeiru(Minecraft mc) {
        phase = TechniquePhase.SHINDEIRU;
        styleActive = false;
        phaseTicks = 0;
        if (mc.player != null) {
            mc.player.playSound(KenshiroSounds.KENSHIRO_SHINDEIRU.value(), 1.0F, 1.0F);
        }
    }

    private static void startHeartbeat(Minecraft mc) {
        phase = TechniquePhase.HEARTBEAT;
        phaseTicks = 0;
        if (mc.player != null) {
            mc.player.playSound(KenshiroSounds.KENSHIRO_HEARTBEAT.value(), 1.0F, 1.0F);
        }
    }

    private static void reset() {
        phase = TechniquePhase.NONE;
        phaseTicks = 0;
    }
}