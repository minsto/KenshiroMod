package com.atomicstrykers.kenshiro.event;

import com.atomicstrykers.kenshiro.KenshiroMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderHandEvent;

import java.lang.reflect.Method;

// ⚠️ Remplace "tonmodid" par TON MODID
@EventBusSubscriber(modid = KenshiroMod.MOD_ID, value = Dist.CLIENT)
public final class ClientHandRenderHook {

    private ClientHandRenderHook() {}

    // --- Réflexion : méthodes privées/instables ---
    private static Method M_RENDER_PLAYER_ARM;
    private static Method M_GET_COLLECTOR;

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        // On ne touche qu'à la main gauche (OFF_HAND)
        if (event.getHand() != InteractionHand.OFF_HAND) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.options.getCameraType().isFirstPerson()) return;

        // On annule le rendu vanilla de l'offhand
        event.setCanceled(true);

        final PoseStack poseStack = event.getPoseStack();
        final int light = event.getPackedLight();

        // ✅ RenderHandEvent n'a plus de MultiBufferSource : on récupère SubmitNodeCollector
        final SubmitNodeCollector collector = getCollector(event);
        if (collector == null) return;

        final AbstractClientPlayer player = (AbstractClientPlayer) mc.player;
        final ItemInHandRenderer handRenderer = mc.gameRenderer.itemInHandRenderer;

        final float equipProgress = event.getEquipProgress();
        final float swingProgress = event.getSwingProgress();

        // 1) Rendre le bras DROIT à la place du bras gauche (comme une main droite)
        invokeRenderPlayerArm(handRenderer, poseStack, collector, light, equipProgress, swingProgress, HumanoidArm.LEFT);

        // 2) Rendre l'item OFFHAND en contexte "RIGHT_HAND" (signature 6 args en 1.21.10)
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty()) {
            handRenderer.renderItem(
                    player,
                    offhand,
                    ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                    poseStack,
                    collector,
                    light
            );
        }
    }

    /** Appelle ItemInHandRenderer#renderPlayerArm(...) qui est private en 1.21.10 */
    private static void invokeRenderPlayerArm(
            ItemInHandRenderer renderer,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            float equipProgress,
            float swingProgress,
            HumanoidArm arm
    ) {
        try {
            if (M_RENDER_PLAYER_ARM == null) {
                // Signature (1.21.10): renderPlayerArm(PoseStack, SubmitNodeCollector, int, float, float, HumanoidArm)
                M_RENDER_PLAYER_ARM = ItemInHandRenderer.class.getDeclaredMethod(
                        "renderPlayerArm",
                        PoseStack.class,
                        SubmitNodeCollector.class,
                        int.class,
                        float.class,
                        float.class,
                        HumanoidArm.class
                );
                M_RENDER_PLAYER_ARM.setAccessible(true);
            }

            M_RENDER_PLAYER_ARM.invoke(renderer, poseStack, collector, light, equipProgress, swingProgress, arm);
        } catch (Throwable t) {
            // Si ça casse à cause d'un rename mappings, tu le verras en log.
            System.err.println("[ClientHandRenderHook] Failed to invoke renderPlayerArm: " + t);
        }
    }

    /**
     * RenderHandEvent change souvent entre versions.
     * On récupère le SubmitNodeCollector sans dépendre d'un nom exact (getSubmitNodeCollector / getCollector / etc.)
     */
    private static SubmitNodeCollector getCollector(RenderHandEvent event) {
        try {
            if (M_GET_COLLECTOR == null) {
                for (Method m : event.getClass().getMethods()) {
                    if (SubmitNodeCollector.class.isAssignableFrom(m.getReturnType())
                            && m.getParameterCount() == 0) {
                        M_GET_COLLECTOR = m;
                        break;
                    }
                }
                if (M_GET_COLLECTOR != null) {
                    M_GET_COLLECTOR.setAccessible(true);
                }
            }

            if (M_GET_COLLECTOR == null) {
                System.err.println("[ClientHandRenderHook] No SubmitNodeCollector getter found on RenderHandEvent");
                return null;
            }

            return (SubmitNodeCollector) M_GET_COLLECTOR.invoke(event);
        } catch (Throwable t) {
            System.err.println("[ClientHandRenderHook] Failed to get SubmitNodeCollector: " + t);
            return null;
        }
    }
}