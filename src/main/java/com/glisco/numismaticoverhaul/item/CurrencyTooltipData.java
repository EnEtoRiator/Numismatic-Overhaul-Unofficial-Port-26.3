package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record CurrencyTooltipData(long[] value, long[] original) implements TooltipComponent {

    public CurrencyTooltipData(long value, long original) {
        this(
                CurrencyResolver.splitValues(value),
                original == -1 ? new long[]{-1} : CurrencyResolver.splitValues(original)
        );
    }
}