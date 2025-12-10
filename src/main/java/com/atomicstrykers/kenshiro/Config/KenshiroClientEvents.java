package com.atomicstrykers.kenshiro.Config;

import com.atomicstrykers.kenshiro.KenshiroMod;
import com.atomicstrykers.kenshiro.event.KenshiroPunchHandler;
import com.atomicstrykers.kenshiro.network.KenshiroSmashPacket;
import com.atomicstrykers.kenshiro.network.KenshiroTechniquePacket;
import com.atomicstrykers.kenshiro.registry.KenshiroSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = KenshiroMod.MOD_ID, value = Dist.CLIENT)
public class KenshiroClientEvents {
    public static boolean styleActive = false;

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
                    reset();
                }
                // ici tu pourras ajouter le shake écran plus tard
            }
        }

        // 3) Animation des bras PENDANT la phase STYLE (en dehors du switch)
        if (phase == TechniquePhase.STYLE && mc.player != null) {

            LocalPlayer player = mc.player;

            // Poing droit (main hand)
            if (player.tickCount % 4 == 0) {
                player.swing(InteractionHand.MAIN_HAND, true);
                tryHitTarget(mc);  // <-- frappe l'entité si il y en a une
            }

            if (player.tickCount % 3 == 0) {
                player.swing(InteractionHand.OFF_HAND, true);
                tryHitTarget(mc);  // <-- idem
            }
        }

    }
    private static void tryHitTarget(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;

        // Raycast actuel du joueur
        var hit = mc.hitResult;
        if (hit instanceof net.minecraft.world.phys.EntityHitResult ehr) {
            var entity = ehr.getEntity();

            // Appelle l'attaque vanilla coté client -> envoie au serveur
            mc.gameMode.attack(mc.player, entity);
        }
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