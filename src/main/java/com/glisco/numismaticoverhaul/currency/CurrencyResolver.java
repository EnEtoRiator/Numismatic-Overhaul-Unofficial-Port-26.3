package com.glisco.numismaticoverhaul.currency;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;

public final class CurrencyResolver {

    private CurrencyResolver() {}

    /**
     * Разбивает сумму на массив long[]{BRONZE, SILVER, GOLD}
     */
    public static long[] splitValues(long rawValue) {
        long gold = rawValue / Currency.GOLD_VALUE;
        long remainder = rawValue % Currency.GOLD_VALUE;
        long silver = remainder / Currency.SILVER_VALUE;
        long bronze = remainder % Currency.SILVER_VALUE;
        return new long[]{Math.max(0L, bronze), Math.max(0L, silver), Math.max(0L, gold)};
    }

    public static long combineValues(long[] values) {
        if (values.length != 3) {
            NumismaticOverhaul.LOGGER.error("Некорректная длина массива валюты: {}", values.length);
            return 0L;
        }
        return combineValues(values[0], values[1], values[2]);
    }

    public static long combineValues(long bronze, long silver, long gold) {
        return Currency.BRONZE.getRawValue(bronze)
                + Currency.SILVER.getRawValue(silver)
                + Currency.GOLD.getRawValue(gold);
    }

    public static boolean canBeCompacted(long[] values) {
        return values[0] < 100 && values[1] < 100 && values[2] < 100;
    }
}