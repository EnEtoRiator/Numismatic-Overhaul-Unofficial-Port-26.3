package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncPurseS2CPacket(long balance) implements CustomPacketPayload {

    public static final Type<SyncPurseS2CPacket> TYPE = new Type<>(NumismaticOverhaul.id("sync_purse"));

    public static final StreamCodec<ByteBuf, SyncPurseS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, SyncPurseS2CPacket::balance,
            SyncPurseS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}