package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.shop.ShopBlockEntity;
import com.glisco.numismaticoverhaul.block.shop.ShopOffer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record UpdateShopScreenS2CPacket(
        List<ShopOffer> offers,
        long storedCurrency,
        boolean transferEnabled,
        ItemStack bufferStack
) implements CustomPacketPayload {

    public static final Type<UpdateShopScreenS2CPacket> TYPE = new Type<>(NumismaticOverhaul.id("update_shop_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateShopScreenS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ShopOffer.STREAM_CODEC.apply(ByteBufCodecs.list()), UpdateShopScreenS2CPacket::offers,
            ByteBufCodecs.VAR_LONG, UpdateShopScreenS2CPacket::storedCurrency,
            ByteBufCodecs.BOOL, UpdateShopScreenS2CPacket::transferEnabled,
            ItemStack.OPTIONAL_STREAM_CODEC, UpdateShopScreenS2CPacket::bufferStack,
            UpdateShopScreenS2CPacket::new
    );

    public UpdateShopScreenS2CPacket(ShopBlockEntity shop, ItemStack bufferStack) {
        this(List.copyOf(shop.getOffers()), shop.getStoredCurrency(), shop.isTransferEnabled(), bufferStack.copy());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}