package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.currency.PurseHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record RequestPurseActionC2SPacket(Action action, long value) implements CustomPacketPayload {

    public static final Type<RequestPurseActionC2SPacket> TYPE = new Type<>(NumismaticOverhaul.id("purse_action"));

    public static final StreamCodec<ByteBuf, RequestPurseActionC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(id -> Action.values()[id], Action::ordinal), RequestPurseActionC2SPacket::action,
            ByteBufCodecs.VAR_LONG, RequestPurseActionC2SPacket::value,
            RequestPurseActionC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static RequestPurseActionC2SPacket storeAll() {
        return new RequestPurseActionC2SPacket(Action.STORE_ALL, 0L);
    }

    public static RequestPurseActionC2SPacket extractAll() {
        return new RequestPurseActionC2SPacket(Action.EXTRACT_ALL, 0L);
    }

    public static RequestPurseActionC2SPacket extract(long amount) {
        return new RequestPurseActionC2SPacket(Action.EXTRACT, amount);
    }

    public static void handleServer(RequestPurseActionC2SPacket packet, ServerPlayer player) {
        switch (packet.action()) {
            case STORE_ALL -> {
                long collected = CurrencyHelper.getMoneyInInventory(player, true);
                if (collected > 0) {
                    PurseHelper.modify(player, collected);
                }
            }
            case EXTRACT -> {
                long current = PurseHelper.getBalance(player);
                long extracting = Math.max(0L, Math.min(packet.value(), current));
                if (extracting > 0) {
                    CurrencyHelper.offerAsCoins(player, extracting);
                    PurseHelper.modify(player, -extracting);
                }
            }
            case EXTRACT_ALL -> {
                long current = PurseHelper.getBalance(player);
                if (current > 0) {
                    CurrencyHelper.offerAsCoins(player, current);
                    PurseHelper.modify(player, -current);
                }
            }
        }
    }

    public enum Action {
        STORE_ALL,
        EXTRACT,
        EXTRACT_ALL
    }
}