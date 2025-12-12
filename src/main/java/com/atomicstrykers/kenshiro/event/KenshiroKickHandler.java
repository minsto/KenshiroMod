package com.atomicstrykers.kenshiro.event;

import com.atomicstrykers.kenshiro.KenshiroMod;
import com.atomicstrykers.kenshiro.registry.KenshiroSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class KenshiroKickHandler {
    private static void startCharge(Minecraft mc) {

        if (mc.player != null) {
            mc.player.playSound(KenshiroSounds.KENSHIRO_SMASH.value(), 0.5F, 1.0F);
        }
    }
    public static void forceJumpInAir(Player player){
        double jumpPower =0.42;
        if(!player.onGround()){
            jumpPower=0.42;
            player.jumpFromGround();
        }
        player.setDeltaMovement(player.getDeltaMovement().x,jumpPower,player.getDeltaMovement().z);
    }
    // dégâts du kick
    private static final float KICK_DAMAGE = 4.0F;
    // force du knockback
    private static final double KNOCKBACK_STRENGTH = 1.5D;
    // portée
    private static final double RANGE = 3.0D;

    public static void doKick(ServerPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!(player.level() instanceof ServerLevel level)) return;




        // saut vanilla (même logique que quand tu appuies sur la barre espace)

        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.getEyePosition();

        AABB box = new AABB(
                origin.x - 0.75, origin.y - 0.75, origin.z - 0.75,
                origin.x + 0.75, origin.y + 0.75, origin.z + 0.75
        ).expandTowards(look.scale(RANGE));

        LivingEntity target = level.getEntitiesOfClass(
                        LivingEntity.class,
                        box,
                        e -> e != player && e.isAlive()
                )
                .stream()
                .findFirst()
                .orElse(null);

        if (target == null) return;

        DamageSource src = player.damageSources().playerAttack(player);

        // 🟢 1. inflige des dégâts (void maintenant)
        target.hurt(src, KICK_DAMAGE);

        // 🟢 2. knockback
        Vec3 kb = look.scale(KNOCKBACK_STRENGTH);
        target.push(kb.x, 0.3D, kb.z);
        //startCharge(mc);
        // 🟢 3. animation du joueur
        startCharge(mc);
        forceJumpInAir(mc.player);
        player.swing(InteractionHand.MAIN_HAND, true);

        KenshiroMod.LOGGER.debug(
                "Kenshiro kick hit {} for {} dmg",
                target.getName().getString(),
                KICK_DAMAGE
        );
    }
}
