package com.glisco.numismaticoverhaul.component;

import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record MoneyBagComponent(long bronze, long silver, long gold) {

    public static final MoneyBagComponent EMPTY = new MoneyBagComponent(0L, 0L, 0L);

    public static final Codec<MoneyBagComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("bronze", 0L).forGetter(MoneyBagComponent::bronze),
            Codec.LONG.optionalFieldOf("silver", 0L).forGetter(MoneyBagComponent::silver),
            Codec.LONG.optionalFieldOf("gold", 0L).forGetter(MoneyBagComponent::gold)
    ).apply(instance, MoneyBagComponent::new));

    public static final StreamCodec<ByteBuf, MoneyBagComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, MoneyBagComponent::bronze,
            ByteBufCodecs.VAR_LONG, MoneyBagComponent::silver,
            ByteBufCodecs.VAR_LONG, MoneyBagComponent::gold,
            MoneyBagComponent::new
    );

    public static MoneyBagComponent of(long[] values) {
        return new MoneyBagComponent(values[0], values[1], values[2]);
    }

    public static MoneyBagComponent of(long value) {
        return of(CurrencyResolver.splitValues(value));
    }

    public static MoneyBagComponent combine(long[] a, long[] b) {
        return new MoneyBagComponent(a[0] + b[0], a[1] + b[1], a[2] + b[2]);
    }

    public static MoneyBagComponent combine(ItemStack stack1, ItemStack stack2) {
        MoneyBagComponent c1 = stack1.getOrDefault(ModComponents.MONEY_BAG, EMPTY);
        MoneyBagComponent c2 = stack2.getOrDefault(ModComponents.MONEY_BAG, EMPTY);
        return new MoneyBagComponent(c1.bronze + c2.bronze, c1.silver + c2.silver, c1.gold + c2.gold);
    }

    public long value() {
        return CurrencyResolver.combineValues(bronze, silver, gold);
    }
}