package com.glisco.numismaticoverhaul.sound;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {

    public static final SoundEvent PIGGY_BANK_BREAK = register("piggy_bank_break");

    private static SoundEvent register(String name) {
        Identifier id = NumismaticOverhaul.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void initialize() {
        NumismaticOverhaul.LOGGER.info("[ModSounds] Звуки зарегистрированы.");
    }
}