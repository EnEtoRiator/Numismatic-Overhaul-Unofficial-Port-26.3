package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.shop.ShopMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record ShopActionC2SPacket(Action action, long value, ItemStack stack) implements CustomPacketPayload {

    public static final Type<ShopActionC2SPacket> TYPE = new Type<>(NumismaticOverhaul.id("shop_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShopActionC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(id -> Action.values()[id], Action::ordinal), ShopActionC2SPacket::action,
            ByteBufCodecs.VAR_LONG, ShopActionC2SPacket::value,
            ItemStack.OPTIONAL_STREAM_CODEC, ShopActionC2SPacket::stack,
            ShopActionC2SPacket::new
    );

    public ShopActionC2SPacket(Action action) {
        this(action, 0L, ItemStack.EMPTY);
    }

    public ShopActionC2SPacket(Action action, long value) {
        this(action, value, ItemStack.EMPTY);
    }

    public ShopActionC2SPacket(Action action, ItemStack stack) {
        this(action, 0L, stack);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(ShopActionC2SPacket packet, ServerPlayer player) {
        if (!(player.containerMenu instanceof ShopMenu menu)) return;
        switch (packet.action()) {
            case LOAD_OFFER -> menu.loadOffer(packet.value());
            case CREATE_OFFER -> menu.createOffer(packet.value(), packet.stack());
            case DELETE_OFFER -> menu.deleteOffer(packet.stack());
            case EXTRACT_CURRENCY -> menu.extractCurrency();
            case TOGGLE_TRANSFER -> menu.toggleTransfer();
            case CLICK_BUFFER -> menu.setBufferFromClient(packet.stack());
        }
    }

    public enum Action {
        CREATE_OFFER, DELETE_OFFER, LOAD_OFFER, EXTRACT_CURRENCY, TOGGLE_TRANSFER, CLICK_BUFFER
    }
}