package com.glisco.numismaticoverhaul.currency;

import com.glisco.numismaticoverhaul.item.ModItems;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class CurrencyConverter {

    private CurrencyConverter() {}

    public static ItemStack[] getAsItemStackArray(long value) {
        long[] values = CurrencyResolver.splitValues(value);
        return new ItemStack[]{
                new ItemStack(ModItems.BRONZE_COIN, asInt(values[0])),
                new ItemStack(ModItems.SILVER_COIN, asInt(values[1])),
                new ItemStack(ModItems.GOLD_COIN, asInt(values[2]))
        };
    }

    public static List<ItemStack> getAsItemStackList(long value) {
        return getAsItemStackList(CurrencyResolver.splitValues(value));
    }

    public static List<ItemStack> getAsItemStackList(long[] values) {
        List<ItemStack> list = new ArrayList<>(3);
        Currency[] currencies = Currency.values();
        for (int i = currencies.length - 1; i >= 0; i--) {
            if (i < values.length && values[i] > 0) {
                list.add(new ItemStack(currencies[i], asInt(values[i])));
            }
        }
        return list;
    }

    public static int getRequiredCurrencyTypes(long value) {
        return splitAtMaxCount(getAsItemStackList(value)).size();
    }

    public static List<ItemStack> splitAtMaxCount(List<ItemStack> input) {
        List<ItemStack> output = new ArrayList<>();
        for (ItemStack stack : input) {
            int maxSize = stack.getMaxStackSize();
            if (stack.getCount() <= maxSize) {
                output.add(stack);
            } else {
                int fullStacks = stack.getCount() / maxSize;
                for (int i = 0; i < fullStacks; i++) {
                    ItemStack copy = stack.copy();
                    copy.setCount(maxSize);
                    output.add(copy);
                }
                int remainder = stack.getCount() % maxSize;
                if (remainder > 0) {
                    ItemStack copy = stack.copy();
                    copy.setCount(remainder);
                    output.add(copy);
                }
            }
        }
        return output;
    }

    public static List<ItemStack> getAsValidStacks(long value) {
        return splitAtMaxCount(getAsItemStackList(value));
    }

    public static List<ItemStack> getAsValidStacks(long[] values) {
        return splitAtMaxCount(getAsItemStackList(values));
    }

    public static int asInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }
}