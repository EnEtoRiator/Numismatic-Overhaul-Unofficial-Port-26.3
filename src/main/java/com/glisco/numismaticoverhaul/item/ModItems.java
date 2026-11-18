package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.Currency;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public final class ModItems {

    public static final CoinItem BRONZE_COIN = registerItem(
            "bronze_coin",
            new Item.Properties(),
            props -> new CoinItem(Currency.BRONZE, props)
    );

    public static final CoinItem SILVER_COIN = registerItem(
            "silver_coin",
            new Item.Properties(),
            props -> new CoinItem(Currency.SILVER, props)
    );

    public static final CoinItem GOLD_COIN = registerItem(
            "gold_coin",
            new Item.Properties(),
            props -> new CoinItem(Currency.GOLD, props)
    );

    public static final MoneyBagItem MONEY_BAG = registerItem(
            "money_bag",
            new Item.Properties(),
            MoneyBagItem::new
    );

    private static <T extends Item> T registerItem(String name, Item.Properties properties, Function<Item.Properties, T> factory) {
        Identifier id = NumismaticOverhaul.id(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        properties.setId(key);
        T item = factory.apply(properties);
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    public static void initialize() {
        NumismaticOverhaul.LOGGER.info("[ModItems] Предметы валюты зарегистрированы.");
    }
}