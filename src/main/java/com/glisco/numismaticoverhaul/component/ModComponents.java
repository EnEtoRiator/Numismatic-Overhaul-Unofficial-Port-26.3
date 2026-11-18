package com.glisco.numismaticoverhaul.component;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModComponents {

    public static final DataComponentType<MoneyBagComponent> MONEY_BAG = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            NumismaticOverhaul.id("money_bag"),
            DataComponentType.<MoneyBagComponent>builder()
                    .persistent(MoneyBagComponent.CODEC)
                    .networkSynchronized(MoneyBagComponent.STREAM_CODEC)
                    .build()
    );

    public static void initialize() {
        NumismaticOverhaul.LOGGER.info("[ModComponents] Компоненты данных зарегистрированы.");
    }
}