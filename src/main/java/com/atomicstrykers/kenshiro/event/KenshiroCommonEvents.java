package com.atomicstrykers.kenshiro.event;

import com.atomicstrykers.kenshiro.Config.KenshiroConfig;
import com.atomicstrykers.kenshiro.KenshiroMod;


import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import com.atomicstrykers.kenshiro.Config.KenshiroConfig;
import com.atomicstrykers.kenshiro.KenshiroMod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.atomicstrykers.kenshiro.event.KenshiroPunchHandler.startPunch;

@EventBusSubscriber(modid = KenshiroMod.MOD_ID)
public class KenshiroCommonEvents {

    // durée de la technique (6 secondes)
    private static final long TECHNIQUE_DURATION_TICKS = 20L * 6L;
    private static final Map<UUID, Long> TECHNIQUE_EXPIRY = new HashMap<>();

    // appelé PAR LE PACKET quand tu presses R
    public static void startTechnique(ServerPlayer player) {
        long now = player.level().getGameTime();
        TECHNIQUE_EXPIRY.put(player.getUUID(), now + TECHNIQUE_DURATION_TICKS);
        KenshiroMod.LOGGER.info("Technique Kenshiro activée pour {}",
                player.getGameProfile().name());
    }

    private static boolean isTechniqueActive(Player player) {
        Long expiry = TECHNIQUE_EXPIRY.get(player.getUUID());
        if (expiry == null) return false;
        long now = player.level().getGameTime();
        if (now >= expiry) {
            TECHNIQUE_EXPIRY.remove(player.getUUID());
            return false;
        }
        return true;
    }
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (!(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        // Doit être à mains nues
        if (!serverPlayer.getMainHandItem().isEmpty()) {
            return;
        }

        // On annule l'attaque vanilla
        event.setCanceled(true);

        KenshiroPunchHandler.doPunch(serverPlayer, target);
    }
    // Coups sur entités (mobs/joueurs)
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;

        // uniquement à mains nues
        if (!player.getMainHandItem().isEmpty()) return;
        if (!KenshiroConfig.ENABLE_KENSHIRO_PUNCH.get()) return;

        int dmg = KenshiroConfig.KENSHIRO_BASE_DAMAGE.get();

        if (isTechniqueActive(player)) {
            startPunch(mc);
            dmg += 6;
        } else if (KenshiroConfig.ENABLE_AURA.get()) {
            dmg += 2;
        }

        DamageSource source = player.damageSources().playerAttack(player);
        target.hurt(source, dmg);

        if (isTechniqueActive(player)) {
            target.igniteForSeconds(4);

            if (player.level() instanceof ServerLevel sl) {
                for (int i = 0; i < 12; i++) {
                    double x = target.getX() + (sl.random.nextDouble() - 0.5D) * 1.2D;
                    double y = target.getY() + sl.random.nextDouble() * target.getBbHeight();
                    double z = target.getZ() + (sl.random.nextDouble() - 0.5D) * 1.2D;
                    sl.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0.05, 0, 0);
                }
            }
        }

        KenshiroMod.LOGGER.debug("Coup Kenshiro sur {} pour {} dégâts (tech active: {}).",
                target.getName().getString(), dmg, isTechniqueActive(player));

        // on annule le hit vanilla pour ne pas faire double dégât
        event.setCanceled(true);
    }

    // Clic gauche sur bloc pendant la technique (facultatif, si le joueur tape lui-même)
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;
        if (!(sp.level() instanceof ServerLevel level)) return;
        if (!isTechniqueActive(sp)) return;

        BlockPos pos = event.getPos();
        if (level.getBlockState(pos).is(Blocks.BEDROCK)) return;

        for (int i = 0; i < 8; i++) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5);
            double y = pos.getY() + 0.5 + (level.random.nextDouble() - 0.5);
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5);
            level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0.02, 0, 0);
        }

       // boolean success = sp.gameMode.destroyBlock(pos);
       // if (success) {
         //   KenshiroMod.LOGGER.debug("KenshiroStyle casse (clic) le bloc {} à {}.",
           //         level.getBlockState(pos), pos);
        //}

        event.setCanceled(true);
    }

    // Tick serveur : casse les blocs automatiquement devant le joueur pendant la technique
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;
        if (!(sp.level() instanceof ServerLevel level)) return;
        if (!isTechniqueActive(sp)) return;

        // on veut les deux mains vides pour l'effet "poings" pur
       // if (!sp.getMainHandItem().isEmpty() || !sp.getOffhandItem().isEmpty()) return;

        double reach = 4.0D;
        HitResult hit = sp.pick(reach, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult bhr = (BlockHitResult) hit;
        BlockPos pos = bhr.getBlockPos();

        if (level.getBlockState(pos).is(Blocks.BEDROCK)) return;

        for (int i = 0; i < 6; i++) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5);
            double y = pos.getY() + 0.5 + (level.random.nextDouble() - 0.5);
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5);
            level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0.02, 0, 0);
        }

      //  boolean success = sp.gameMode.destroyBlock(pos);
        //if (success) {
          //  KenshiroMod.LOGGER.debug("KenshiroStyle casse AUTO le bloc {} à {}.",
            //        level.getBlockState(pos), pos);
        //}
    }
}