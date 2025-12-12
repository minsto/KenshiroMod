package com.atomicstrykers.kenshiro.event;



import com.atomicstrykers.kenshiro.Config.KenshiroClientEvents;
import com.atomicstrykers.kenshiro.Config.KenshiroConfig;
import com.atomicstrykers.kenshiro.registry.KenshiroSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.atomicstrykers.kenshiro.KenshiroMod;
import com.atomicstrykers.kenshiro.registry.KenshiroSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

@EventBusSubscriber(modid = KenshiroMod.MOD_ID)
public class KenshiroPunchHandler {

    /**
     * Remplace l’attaque vanilla quand le joueur frappe une entité.
     */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();

        // Serveur uniquement
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Cible doit être une LivingEntity (mob, joueur…)
        if (!(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        // Doit être à mains nues (rien en main principale)
        if (!serverPlayer.getMainHandItem().isEmpty()) {
            return;
        }

        // On remplace l'attaque vanilla
        event.setCanceled(true);

        doPunch(serverPlayer, target);
    }
    public static void startPunch(Minecraft mc) {

        if (mc.player != null) {
            mc.player.playSound(KenshiroSounds.KENSHIRO_PUNCH.value(), 1.0F, 1.0F);
        }
    }
    /**
     * Applique le Kenshiro punch :
     * - 3 dégâts
     * - petit knockback
     * - son kenshiropunch seulement si dégâts > 0
     */
    public static void doPunch(ServerPlayer player, LivingEntity target) {
        // Source de dégâts "attaque joueur"
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        DamageSource src = player.damageSources().playerAttack(player);
        if (!KenshiroConfig.ENABLE_KENSHIRO_PUNCH.get()) return;

        int dmg = KenshiroConfig.KENSHIRO_BASE_DAMAGE.get();


            dmg += 3;
          if (KenshiroConfig.ENABLE_AURA.get()) {
            dmg += 2;
        }
        // On regarde la vie AVANT
        float before = target.getHealth();

        // 3 de dégâts
        target.hurt(src, dmg);

        // Si la vie n'a pas bougé → pas de hit
        boolean damaged = target.getHealth() < before;
        if (!damaged) {
            return;
        }

        // Recul dans la direction du regard
        Vec3 look = player.getLookAngle().normalize();
        Vec3 kb = look.scale(0.5D);
        target.push(kb.x, 0.2D, kb.z);
        target.hasImpulse = true; // force la prise en compte du knockback

        // Son local pour le joueur qui frappe
        startPunch(mc);

        KenshiroMod.LOGGER.debug(
                "Kenshiro punch hit {} for 3 dmg",
                target.getName().getString()
        );
    }
}