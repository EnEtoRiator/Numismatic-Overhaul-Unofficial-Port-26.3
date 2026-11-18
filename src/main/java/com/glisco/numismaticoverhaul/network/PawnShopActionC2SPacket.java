package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.pawn.PawnShopMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record PawnShopActionC2SPacket(Action action, long value, ItemStack stack) implements CustomPacketPayload {

    public static final Type<PawnShopActionC2SPacket> TYPE = new Type<>(NumismaticOverhaul.id("pawn_shop_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PawnShopActionC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(id -> Action.values()[id], Action::ordinal), PawnShopActionC2SPacket::action,
            ByteBufCodecs.VAR_LONG, PawnShopActionC2SPacket::value,
            ItemStack.OPTIONAL_STREAM_CODEC, PawnShopActionC2SPacket::stack,
            PawnShopActionC2SPacket::new
    );

    public PawnShopActionC2SPacket(Action action) {
        this(action, 0L, ItemStack.EMPTY);
    }

    public PawnShopActionC2SPacket(Action action, long value) {
        this(action, value, ItemStack.EMPTY);
    }

    public PawnShopActionC2SPacket(Action action, ItemStack stack) {
        this(action, 0L, stack);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(PawnShopActionC2SPacket packet, ServerPlayer player) {
        if (!(player.containerMenu instanceof PawnShopMenu menu)) return;
        switch (packet.action()) {
            case LOAD_OFFER -> menu.loadOffer(packet.value());
            case CREATE_OFFER -> menu.createOffer(packet.value(), packet.stack());
            case DELETE_OFFER -> menu.deleteOffer(packet.stack());
            case INSERT_CURRENCY -> menu.insertCurrency();
            case EXTRACT_CURRENCY -> menu.extractCurrency();
            case TOGGLE_TRANSFER -> menu.toggleTransfer();
            case CLICK_BUFFER -> menu.setBufferFromClient(packet.stack());
        }
    }

    public enum Action {
        CREATE_OFFER, DELETE_OFFER, LOAD_OFFER, INSERT_CURRENCY, EXTRACT_CURRENCY, TOGGLE_TRANSFER, CLICK_BUFFER
    }
}