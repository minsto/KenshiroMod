package com.atomicstrykers.kenshiro.event;

import com.atomicstrykers.kenshiro.KenshiroMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;


@EventBusSubscriber(modid = KenshiroMod.MOD_ID, value = Dist.CLIENT)
public final class AutoLeftSwingAfterRight {
    private AutoLeftSwingAfterRight() {}

    // On demande un swing offhand dès que la prochaine fin de swing main droite arrive
    private static boolean pendingOffhandSwing = false;

    // Pour gérer le clic maintenu (minage) sans spam
    private static boolean wasAttackDown = false;

    // Anti spam pour clics très rapides
    private static long lastTriggerMs = 0L;

    @SubscribeEvent
    public static void onAttackKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (event.getKeyMapping() != mc.options.keyAttack) return;

        // laisse vanilla faire l'action + swing main droite
        requestOffhandAfterRight(mc);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Option : uniquement 1ère personne
        if (!mc.options.getCameraType().isFirstPerson()) return;

        // ✅ Couvre le MINAGE (clic maintenu)
        boolean attackDown = mc.options.keyAttack.isDown();
        if (attackDown && !wasAttackDown) {
            // nouveau maintien clic -> on va faire droite puis gauche
            requestOffhandAfterRight(mc);
        }
        wasAttackDown = attackDown;

        if (!pendingOffhandSwing) return;

        // On ne veut déclencher l'offhand que si la main droite a fini son swing
        boolean rightSwingFinished =  mc.player.swingingArm != InteractionHand.MAIN_HAND||!mc.player.swinging ;

        if (rightSwingFinished) {
            // Si tu veux que ça marche même mains vides, on swing offhand quand même
            mc.player.swing(InteractionHand.OFF_HAND);
            pendingOffhandSwing = false ;
        }
    }

    private static void requestOffhandAfterRight(Minecraft mc) {
        // Option : seulement quand les 2 mains sont vides
        // (Tu as dit "aussi si rien dans les 2 mains" -> donc on le fait exactement dans ce cas)
        ItemStack main = mc.player.getMainHandItem();
        ItemStack off  = mc.player.getOffhandItem();
        if (!main.isEmpty() || !off.isEmpty()) return;

        long now = System.currentTimeMillis();
        if (now - lastTriggerMs < 1) return; // évite spam
        lastTriggerMs = now;

        pendingOffhandSwing = true;
    }

}