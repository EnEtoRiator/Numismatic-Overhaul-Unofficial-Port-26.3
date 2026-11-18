package com.glisco.numismaticoverhaul.loot;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.config.NumismaticConfig;
import com.glisco.numismaticoverhaul.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

public final class ModLoot {

    public static final MapCodec<MoneyBagLootEntry> MONEY_BAG_ENTRY = Registry.register(
            BuiltInRegistries.LOOT_POOL_ENTRY_TYPE,
            NumismaticOverhaul.id("money_bag"),
            MoneyBagLootEntry.CODEC
    );

    public static void initialize() {
        if (!NumismaticConfig.get().generateCurrencyInChests) return;

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            var lootOpts = NumismaticConfig.get().lootOptions;

            if (isAny(key,
                    BuiltInLootTables.STRONGHOLD_LIBRARY,
                    BuiltInLootTables.BASTION_TREASURE,
                    BuiltInLootTables.STRONGHOLD_CORRIDOR,
                    BuiltInLootTables.PILLAGER_OUTPOST,
                    BuiltInLootTables.BURIED_TREASURE,
                    BuiltInLootTables.SIMPLE_DUNGEON,
                    BuiltInLootTables.ABANDONED_MINESHAFT)) {
                tableBuilder.withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(ModItems.GOLD_COIN))
                        .when(LootItemRandomChanceCondition.randomChance(0.01F)));
            }

            if (key.equals(BuiltInLootTables.DESERT_PYRAMID)) {
                tableBuilder.withPool(LootPool.lootPool()
                        .add(MoneyBagLootEntry.builder(lootOpts.desertMinLoot, lootOpts.desertMaxLoot))
                        .when(LootItemRandomChanceCondition.randomChance(0.45F)));
            } else if (isAny(key, BuiltInLootTables.SIMPLE_DUNGEON, BuiltInLootTables.ABANDONED_MINESHAFT)) {
                tableBuilder.withPool(LootPool.lootPool()
                        .add(MoneyBagLootEntry.builder(lootOpts.dungeonMinLoot, lootOpts.dungeonMaxLoot))
                        .when(LootItemRandomChanceCondition.randomChance(0.75F)));
            } else if (isAny(key,
                    BuiltInLootTables.BASTION_TREASURE,
                    BuiltInLootTables.STRONGHOLD_CORRIDOR,
                    BuiltInLootTables.PILLAGER_OUTPOST,
                    BuiltInLootTables.BURIED_TREASURE)) {
                tableBuilder.withPool(LootPool.lootPool()
                        .add(MoneyBagLootEntry.builder(lootOpts.structureMinLoot, lootOpts.structureMaxLoot))
                        .when(LootItemRandomChanceCondition.randomChance(0.75F)));
            } else if (key.equals(BuiltInLootTables.STRONGHOLD_LIBRARY)) {
                tableBuilder.withPool(LootPool.lootPool()
                        .add(MoneyBagLootEntry.builder(lootOpts.strongholdLibraryMinLoot, lootOpts.strongholdLibraryMaxLoot))
                        .when(LootItemRandomChanceCondition.randomChance(0.85F)));
            }
        });

        NumismaticOverhaul.LOGGER.info("[ModLoot] Модификаторы лута сундуков зарегистрированы.");
    }

    @SafeVarargs
    private static boolean isAny(ResourceKey<LootTable> target, ResourceKey<LootTable>... candidates) {
        for (ResourceKey<LootTable> candidate : candidates) {
            if (target.equals(candidate)) return true;
        }
        return false;
    }
}