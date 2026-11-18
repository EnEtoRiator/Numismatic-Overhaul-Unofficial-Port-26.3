package com.glisco.numismaticoverhaul.block.pawn;

import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.Optional;

public record PawnShopOffer(ItemStack buy, long price) {

    public static final Codec<PawnShopOffer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("buy").forGetter(PawnShopOffer::buy),
            Codec.LONG.fieldOf("price").forGetter(PawnShopOffer::price)
    ).apply(instance, PawnShopOffer::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PawnShopOffer> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, PawnShopOffer::buy,
            ByteBufCodecs.VAR_LONG, PawnShopOffer::price,
            PawnShopOffer::new
    );

    public PawnShopOffer {
        if (buy.isEmpty()) throw new IllegalArgumentException("Buy stack must not be empty");
        if (price <= 0) throw new IllegalArgumentException("Price must be greater than zero");
    }

    public static void add(PawnShopBlockEntity pawnShop, ItemStack boughtStack) {
        NonNullList<ItemStack> shopItems = pawnShop.getItems();
        ItemStack remaining = boughtStack.copy();

        for (ItemStack stack : shopItems) {
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, remaining) && stack.getCount() < stack.getMaxStackSize()) {
                int transferable = Math.min(remaining.getCount(), stack.getMaxStackSize() - stack.getCount());
                stack.grow(transferable);
                remaining.shrink(transferable);
                if (remaining.isEmpty()) {
                    pawnShop.setChanged();
                    return;
                }
            }
        }

        for (int i = 0; i < shopItems.size(); i++) {
            if (shopItems.get(i).isEmpty()) {
                pawnShop.setItem(i, remaining);
                pawnShop.setChanged();
                return;
            }
        }
    }

    public MerchantOffer toMerchantOffer(PawnShopBlockEntity shop, boolean inexhaustible) {
        int maxUses = inexhaustible ? Integer.MAX_VALUE : (int) (shop.getStoredCurrency() / this.price);
        if (!inexhaustible && !shop.getItems().contains(ItemStack.EMPTY)) {
            maxUses = 0;
        }

        ItemStack money = CurrencyConverter.getRequiredCurrencyTypes(this.price) == 1
                ? CurrencyConverter.getAsItemStackList(this.price).getFirst()
                : MoneyBagItem.fromRawValue(this.price);

        ItemCost buyItem = new ItemCost(this.buy.getItem(), this.buy.getCount());
        return new MerchantOffer(buyItem, Optional.empty(), money, 0, maxUses, 0, 0.0F, 0);
    }

    public ItemStack getBuyStack() {
        return this.buy.copy();
    }
}