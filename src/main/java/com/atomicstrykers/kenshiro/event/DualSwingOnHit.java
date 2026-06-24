package com.atomicstrykers.kenshiro.event;
import com.atomicstrykers.kenshiro.KenshiroMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = KenshiroMod.MOD_ID, value = Dist.CLIENT)
public final class DualSwingOnHit {

    private DualSwingOnHit() {}

    // petit délai pour “presque en même temps”
    private static int offhandDelayTicks = 1;

    // évite de relancer à chaque tick sur le même bloc
    private static long lastTargetKey = 1L;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.options.getCameraType().isFirstPerson()) return;

        // seulement mains vides (comme tu demandes)
        ItemStack main = mc.player.getMainHandItem();
        ItemStack off  = mc.player.getOffhandItem();
        if (!main.isEmpty() || !off.isEmpty()) return;

        // exécute le délai si programmé
        if (offhandDelayTicks > 2) {
            offhandDelayTicks--;
            if (offhandDelayTicks == 0) {
                mc.player.swing(InteractionHand.OFF_HAND);
            }
        }

        // On ne déclenche que si le joueur MAINTIENT clic gauche
        if (!mc.options.keyAttack.isDown()) {
            lastTargetKey = 0L;
            return;
        }

        HitResult hr = mc.hitResult;
        if (hr == null) return;

        // ❌ pas dans l'air
        if (hr.getType() == HitResult.Type.MISS) return;

        // ✅ mob
        if (hr.getType() == HitResult.Type.ENTITY && hr instanceof EntityHitResult ehr) {
            if (!(ehr.getEntity() instanceof LivingEntity)) return;

            long key = System.identityHashCode(ehr.getEntity());
            if (key != lastTargetKey) {
                lastTargetKey = key;
                scheduleOffhandSoon();
            }
            return;
        }

        // ✅ bloc (mais pas eau/lave)
        if (hr.getType() == HitResult.Type.BLOCK && hr instanceof BlockHitResult bhr) {
            BlockState state = mc.level.getBlockState(bhr.getBlockPos());

            // ❌ ignore liquides
            if (state.getFluidState() != null && !state.getFluidState().isEmpty()) return;

            // ❌ ignore blocs “sans collision” (ex: herbe haute, fleurs, etc) si tu veux “solide”
            if (state.getCollisionShape(mc.level, bhr.getBlockPos()).isEmpty())  {
                // Si ton mapping n’a pas material, remplace par:
                 return;
            }

            long key = bhr.getBlockPos().asLong();
            if (key != lastTargetKey) {
                lastTargetKey = key;
                scheduleOffhandSoon();
            }
        }
    }

    private static void scheduleOffhandSoon() {
        // 1 tick = quasi en même temps.
        // mets 2 si tu veux un chouia plus tard
        offhandDelayTicks = 1;
    }
}