package com.atomicstrykers.kenshiro.registry;

import com.atomicstrykers.kenshiro.KenshiroMod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class KenshiroSounds {

    // PLUS de NeoForgeRegistries.SOUND_EVENTS ici !
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, KenshiroMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> KENSHIRO_CHARGE =
            SOUNDS.register("kenshirocharge",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(KenshiroMod.MOD_ID, "kenshirocharge")
                    ));

    public static final DeferredHolder<SoundEvent, SoundEvent> KENSHIRO_STYLE =
            SOUNDS.register("kenshirostyle",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(KenshiroMod.MOD_ID, "kenshirostyle")
                    ));

    public static final DeferredHolder<SoundEvent, SoundEvent> KENSHIRO_SHINDEIRU =
            SOUNDS.register("kenshiroshindeiru",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(KenshiroMod.MOD_ID, "kenshiroshindeiru")
                    ));

    public static final DeferredHolder<SoundEvent, SoundEvent> KENSHIRO_HEARTBEAT =
            SOUNDS.register("kenshiroheartbeat",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(KenshiroMod.MOD_ID, "kenshiroheartbeat")
                    ));
    public static final DeferredHolder<SoundEvent, SoundEvent> KENSHIRO_PUNCH =
            SOUNDS.register("kenshiropunch",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(KenshiroMod.MOD_ID, "kenshiropunch")
                    ));
    public static final DeferredHolder<SoundEvent, SoundEvent> KENSHIRO_SMASH =
            SOUNDS.register("kenshirosmash",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(KenshiroMod.MOD_ID, "kenshirosmash")
                    ));
}