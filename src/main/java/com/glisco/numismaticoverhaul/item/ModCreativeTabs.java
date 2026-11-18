package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public final class ModCreativeTabs {

    public static final CreativeModeTab MAIN_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            NumismaticOverhaul.id("main"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> MoneyBagItem.fromRawValue(100))
                    .title(Component.translatable("itemGroup.numismatic_overhaul.main"))
                    .displayItems((context, entries) -> {
                        entries.accept(ModItems.BRONZE_COIN);
                        entries.accept(ModItems.SILVER_COIN);
                        entries.accept(ModItems.GOLD_COIN);
                        entries.accept(ModItems.MONEY_BAG);
                        entries.accept(ModBlocks.PIGGY_BANK);
                        ModBlocks.COLORED_PIGGY_BANKS.values().forEach(entries::accept);
                        entries.accept(ModBlocks.SHOP);
                        entries.accept(ModBlocks.PAWN_SHOP);
                        entries.accept(ModBlocks.INEXHAUSTIBLE_SHOP);
                        entries.accept(ModBlocks.INEXHAUSTIBLE_PAWN_SHOP);
                    })
                    .build()
    );

    public static void initialize() {
        NumismaticOverhaul.LOGGER.info("[ModCreativeTabs] Вкладка креатива зарегистрирована.");
    }
}