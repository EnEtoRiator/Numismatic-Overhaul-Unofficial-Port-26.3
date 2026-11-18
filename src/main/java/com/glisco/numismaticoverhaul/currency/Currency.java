package com.glisco.numismaticoverhaul.currency;

import com.glisco.numismaticoverhaul.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public enum Currency implements ItemLike {
    BRONZE(0xAE5B3C, 1L) {
        @Override
        public Item asItem() {
            return ModItems.BRONZE_COIN;
        }
    },
    SILVER(0x617174, 100L) {
        @Override
        public Item asItem() {
            return ModItems.SILVER_COIN;
        }
    },
    GOLD(0xBD9838, 10000L) {
        @Override
        public Item asItem() {
            return ModItems.GOLD_COIN;
        }
    };

    public static final int BRONZE_VALUE = 1;
    public static final int SILVER_VALUE = 100;
    public static final int GOLD_VALUE = 10000;

    private final int nameColor;
    private final long unitValue;

    Currency(int nameColor, long unitValue) {
        this.nameColor = nameColor;
        this.unitValue = unitValue;
    }

    public int getNameColor() {
        return this.nameColor;
    }

    public long getRawValue(long amount) {
        return amount * this.unitValue;
    }
}