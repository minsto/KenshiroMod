package com.atomicstrykers.kenshiro.Config;

import com.atomicstrykers.kenshiro.KenshiroMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = KenshiroMod.MOD_ID, value = Dist.CLIENT)
public class KenshiroKeyMappings {

    public static KeyMapping TECHNIQUE_KEY;
    public static KeyMapping SMASH_KEY;

    // ID de la catégorie
    public static final ResourceLocation KEY_CATEGORY_ID =
            ResourceLocation.fromNamespaceAndPath(KenshiroMod.MOD_ID, "key_category");

    // ✅ Catégorie enregistrée UNE SEULE FOIS
    public static final KeyMapping.Category KEY_CATEGORY =
            KeyMapping.Category.register(KEY_CATEGORY_ID);

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {

        TECHNIQUE_KEY = new KeyMapping(
                "key.kenshiromod.technique",
                GLFW.GLFW_KEY_R,
                KEY_CATEGORY       // ✅ pas de register ici
        );
        event.register(TECHNIQUE_KEY);

        SMASH_KEY = new KeyMapping(
                "key.kenshiromod.smash",
                GLFW.GLFW_KEY_B,
                KEY_CATEGORY       // ✅ on réutilise la même catégorie
        );
        event.register(SMASH_KEY);
    }
}