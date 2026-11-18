package com.glisco.numismaticoverhaul.villagers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;

import java.util.Optional;

public record TradeData(
        ItemCost buyItem,
        Optional<ItemCost> buyItemB,
        ItemStack sellItem,
        int maxUses,
        int experience,
        float priceMultiplier
) {

    public static final MapCodec<TradeData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemCost.CODEC.fieldOf("buy").forGetter(TradeData::buyItem),
            ItemCost.CODEC.optionalFieldOf("buy_b").forGetter(TradeData::buyItemB),
            ItemStack.CODEC.fieldOf("sell").forGetter(TradeData::sellItem),
            Codec.INT.optionalFieldOf("max_uses", 12).forGetter(TradeData::maxUses),
            Codec.INT.optionalFieldOf("villager_experience", 5).forGetter(TradeData::experience),
            Codec.FLOAT.optionalFieldOf("price_multiplier", 0.05F).forGetter(TradeData::priceMultiplier)
    ).apply(instance, TradeData::new));

    public MerchantOffer toMerchantOffer() {
        return new MerchantOffer(
                this.buyItem,
                this.buyItemB,
                this.sellItem,
                0,
                this.maxUses,
                this.experience,
                this.priceMultiplier,
                0
        );
    }
}