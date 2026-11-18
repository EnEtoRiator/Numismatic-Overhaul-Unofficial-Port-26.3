package com.glisco.numismaticoverhaul.client.datagen;

import com.glisco.numismaticoverhaul.block.ModBlocks;
import com.glisco.numismaticoverhaul.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;

import java.util.concurrent.CompletableFuture;

public class ModLanguageProvider {

    public static class English extends FabricLanguageProvider {
        public English(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
            super(dataOutput, "en_us", registryLookup);
        }

        @Override
        public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder builder) {
            builder.add(ModItems.BRONZE_COIN, "Bronze Coin");
            builder.add(ModItems.SILVER_COIN, "Silver Coin");
            builder.add(ModItems.GOLD_COIN, "Gold Coin");
            builder.add(ModItems.MONEY_BAG, "Money Bag");

            builder.add(ModBlocks.PIGGY_BANK, "Piggy Bank");
            for (DyeColor color : DyeColor.values()) {
                String readable = capitalize(color.getName().replace('_', ' '));
                builder.add(ModBlocks.COLORED_PIGGY_BANKS.get(color), readable + " Piggy Bank");
            }

            builder.add(ModBlocks.SHOP, "Shop");
            builder.add(ModBlocks.PAWN_SHOP, "Pawn Shop");
            builder.add(ModBlocks.INEXHAUSTIBLE_SHOP, "Inexhaustible Shop");
            builder.add(ModBlocks.INEXHAUSTIBLE_PAWN_SHOP, "Inexhaustible Pawn Shop");

            builder.add("block.numismatic_overhaul.shop.tooltip", "Allows selling items to other players");
            builder.add("block.numismatic_overhaul.pawn_shop.tooltip", "Allows buying items from other players");
            builder.add("block.numismatic_overhaul.inexhaustible_shop.tooltip", "Creative shop with infinite stock");
            builder.add("block.numismatic_overhaul.inexhaustible_pawn_shop.tooltip", "Creative pawn shop with infinite funds");

            builder.add("itemGroup.numismatic_overhaul.main", "Numismatic Overhaul");
            builder.add("container.numismatic_overhaul.piggy_bank", "Piggy Bank");
            builder.add("gui.numismatic_overhaul.purse_title", "Purse");
            builder.add("gui.numismatic_overhaul.purse.withdraw_hint", "Click to withdraw (Shift: x64)");
            builder.add("gui.numismatic_overhaul.shop.inventory_title", "Shop Storage");
            builder.add("gui.numismatic_overhaul.shop.merchant_title", "Shop");
            builder.add("gui.numismatic_overhaul.shop.extract", "Extract");
            builder.add("gui.numismatic_overhaul.shop.buffer_hint", "Click with an item to configure offer");
            builder.add("gui.numismatic_overhaul.pawn_shop.inventory_title", "Pawn Shop Storage");
            builder.add("gui.numismatic_overhaul.pawn_shop.merchant_title", "Pawn Shop");
            builder.add("gui.numismatic_overhaul.pawn_shop.insert", "Insert");
            builder.add("chat.numismatic_overhaul.balance", "Purse balance: %s bronze, %s silver, %s gold");
            builder.add("chat.numismatic_overhaul.balance.player", "%s's balance: %s bronze, %s silver, %s gold");
            builder.add("currency.numismatic_overhaul.bronze", "Bronze");
            builder.add("currency.numismatic_overhaul.silver", "Silver");
            builder.add("currency.numismatic_overhaul.gold", "Gold");
        }
    }

    public static class Russian extends FabricLanguageProvider {
        public Russian(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
            super(dataOutput, "ru_ru", registryLookup);
        }

        @Override
        public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder builder) {
            builder.add(ModItems.BRONZE_COIN, "Бронзовая монета");
            builder.add(ModItems.SILVER_COIN, "Серебряная монета");
            builder.add(ModItems.GOLD_COIN, "Золотая монета");
            builder.add(ModItems.MONEY_BAG, "Мешочек с монетами");

            builder.add(ModBlocks.PIGGY_BANK, "Копилка");
            ModBlocks.COLORED_PIGGY_BANKS.forEach((color, block) ->
                    builder.add(block, "Окрашенная копилка (" + color.getName() + ")")
            );

            builder.add(ModBlocks.SHOP, "Магазин");
            builder.add(ModBlocks.PAWN_SHOP, "Лавка скупщика");
            builder.add(ModBlocks.INEXHAUSTIBLE_SHOP, "Бездонный магазин");
            builder.add(ModBlocks.INEXHAUSTIBLE_PAWN_SHOP, "Бездонная лавка скупщика");

            builder.add("block.numismatic_overhaul.shop.tooltip", "Позволяет продавать предметы другим игрокам");
            builder.add("block.numismatic_overhaul.pawn_shop.tooltip", "Позволяет скупать предметы у других игроков");
            builder.add("block.numismatic_overhaul.inexhaustible_shop.tooltip", "Творческий магазин с бесконечным запасом");
            builder.add("block.numismatic_overhaul.inexhaustible_pawn_shop.tooltip", "Творческий скупщик с бесконечной казной");

            builder.add("itemGroup.numismatic_overhaul.main", "Numismatic Overhaul");
            builder.add("container.numismatic_overhaul.piggy_bank", "Копилка");
            builder.add("gui.numismatic_overhaul.purse_title", "Кошелёк");
            builder.add("gui.numismatic_overhaul.purse.withdraw_hint", "Нажмите, чтобы снять (Shift: x64)");
            builder.add("gui.numismatic_overhaul.shop.inventory_title", "Склад магазина");
            builder.add("gui.numismatic_overhaul.shop.merchant_title", "Магазин");
            builder.add("gui.numismatic_overhaul.shop.extract", "Забрать");
            builder.add("gui.numismatic_overhaul.shop.buffer_hint", "Кликните предметом, чтобы настроить сделку");
            builder.add("gui.numismatic_overhaul.pawn_shop.inventory_title", "Склад скупщика");
            builder.add("gui.numismatic_overhaul.pawn_shop.merchant_title", "Лавка скупщика");
            builder.add("gui.numismatic_overhaul.pawn_shop.insert", "Пополнить");
            builder.add("chat.numismatic_overhaul.balance", "Баланс кошелька: %s бронзы, %s серебра, %s золота");
            builder.add("chat.numismatic_overhaul.balance.player", "Баланс %s: %s бронзы, %s серебра, %s золота");
            builder.add("currency.numismatic_overhaul.bronze", "Бронзы");
            builder.add("currency.numismatic_overhaul.silver", "Серебра");
            builder.add("currency.numismatic_overhaul.gold", "Золота");
        }
    }

    private static String capitalize(String str) {
        String[] parts = str.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}