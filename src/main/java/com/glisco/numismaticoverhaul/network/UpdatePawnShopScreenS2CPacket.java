package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.pawn.PawnShopBlockEntity;
import com.glisco.numismaticoverhaul.block.pawn.PawnShopOffer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record UpdatePawnShopScreenS2CPacket(
        List<PawnShopOffer> offers,
        long storedCurrency,
        boolean transferEnabled,
        ItemStack bufferStack
) implements CustomPacketPayload {

    public static final Type<UpdatePawnShopScreenS2CPacket> TYPE = new Type<>(NumismaticOverhaul.id("update_pawn_shop_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePawnShopScreenS2CPacket> STREAM_CODEC = StreamCodec.composite(
            PawnShopOffer.STREAM_CODEC.apply(ByteBufCodecs.list()), UpdatePawnShopScreenS2CPacket::offers,
            ByteBufCodecs.VAR_LONG, UpdatePawnShopScreenS2CPacket::storedCurrency,
            ByteBufCodecs.BOOL, UpdatePawnShopScreenS2CPacket::transferEnabled,
            ItemStack.OPTIONAL_STREAM_CODEC, UpdatePawnShopScreenS2CPacket::bufferStack,
            UpdatePawnShopScreenS2CPacket::new
    );

    public UpdatePawnShopScreenS2CPacket(PawnShopBlockEntity shop, ItemStack bufferStack) {
        this(List.copyOf(shop.getOffers()), shop.getStoredCurrency(), shop.isTransferEnabled(), bufferStack.copy());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}