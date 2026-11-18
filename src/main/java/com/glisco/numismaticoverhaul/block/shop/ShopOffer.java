package com.glisco.numismaticoverhaul.block.shop;

import com.glisco.numismaticoverhaul.component.ModComponents;
import com.glisco.numismaticoverhaul.component.MoneyBagComponent;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;
import java.util.Optional;

public record ShopOffer(ItemStack sell, long price) {

    public static final Codec<ShopOffer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("sell").forGetter(ShopOffer::sell),
            Codec.LONG.fieldOf("price").forGetter(ShopOffer::price)
    ).apply(instance, ShopOffer::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShopOffer> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ShopOffer::sell,
            ByteBufCodecs.VAR_LONG, ShopOffer::price,
            ShopOffer::new
    );

    public ShopOffer {
        if (sell.isEmpty()) throw new IllegalArgumentException("Sell stack must not be empty");
        if (price <= 0) throw new IllegalArgumentException("Price must be greater than zero");
    }

    public MerchantOffer toMerchantOffer(ShopBlockEntity shop, boolean inexhaustible) {
        boolean isPocketChange = CurrencyConverter.getRequiredCurrencyTypes(price) == 1;
        ItemStack buyStack = isPocketChange
                ? CurrencyConverter.getAsItemStackList(price).getFirst()
                : MoneyBagItem.fromRawValue(price);

        int maxUses = inexhaustible
                ? Integer.MAX_VALUE
                : count(shop.getItems(), sell) / sell.getCount();

        ItemCost cost = isPocketChange
                ? new ItemCost(buyStack.getItem(), buyStack.getCount())
                : new ItemCost(
                BuiltInRegistries.ITEM.wrapAsHolder(buyStack.getItem()),
                1,
                DataComponentExactPredicate.builder()
                        .expect(ModComponents.MONEY_BAG, MoneyBagComponent.of(price))
                        .build()
        );

        return new MerchantOffer(cost, Optional.empty(), sell.copy(), 0, maxUses, 0, 0.0F, 0);
    }

    public ItemStack getSellStack() {
        return this.sell.copy();
    }

    public static int count(List<ItemStack> stacks, ItemStack testStack) {
        int count = 0;
        for (ItemStack stack : stacks) {
            if (ItemStack.isSameItemSameComponents(stack, testStack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int remove(List<ItemStack> stacks, ItemStack removeStack) {
        int toRemove = removeStack.getCount();
        for (ItemStack stack : stacks) {
            if (!ItemStack.isSameItemSameComponents(stack, removeStack)) continue;
            int removed = Math.min(stack.getCount(), toRemove);
            stack.shrink(removed);
            toRemove -= removed;
            if (toRemove <= 0) break;
        }
        return removeStack.getCount() - toRemove;
    }
}