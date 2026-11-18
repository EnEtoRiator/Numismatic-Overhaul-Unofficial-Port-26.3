package com.glisco.numismaticoverhaul.villagers;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NumismaticVillagerTradesRegistry {

    private static final Map<VillagerProfession, Map<Integer, List<TradeData>>> TRADES = new HashMap<>();

    public static void registerTrade(VillagerProfession profession, int level, TradeData trade) {
        TRADES.computeIfAbsent(profession, p -> new HashMap<>())
                .computeIfAbsent(level, l -> new ArrayList<>())
                .add(trade);
    }

    public static void applyToVillager(VillagerProfession profession, int level, MerchantOffers offers) {
        Map<Integer, List<TradeData>> professionTrades = TRADES.get(profession);
        if (professionTrades == null) return;

        List<TradeData> levelTrades = professionTrades.get(level);
        if (levelTrades == null) return;

        for (TradeData data : levelTrades) {
            offers.add(data.toMerchantOffer());
        }
    }

    public static ItemCost remapEmeraldsToCoins(ItemCost original, int countMultiplier) {
        if (original.item().value().asItem() == Items.EMERALD) {
            long rawValue = (long) original.count() * countMultiplier * 125L;
            return CurrencyHelper.getClosestTradeItem(rawValue);
        }
        return original;
    }
}