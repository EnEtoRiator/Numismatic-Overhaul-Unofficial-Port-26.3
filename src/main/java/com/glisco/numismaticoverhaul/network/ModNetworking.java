package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.PurseHelper;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(SyncPurseS2CPacket.TYPE, SyncPurseS2CPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RequestPurseActionC2SPacket.TYPE, RequestPurseActionC2SPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                RequestPurseActionC2SPacket.TYPE,
                (payload, context) -> context.server().execute(() ->
                        RequestPurseActionC2SPacket.handleServer(payload, context.player())
                )
        );

        // Синхронизируем кошелек при входе на сервер и после возрождения
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                PurseHelper.syncToClient(handler.getPlayer())
        );
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                PurseHelper.syncToClient(newPlayer)
        );

        PayloadTypeRegistry.clientboundPlay().register(UpdateShopScreenS2CPacket.TYPE, UpdateShopScreenS2CPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(UpdatePawnShopScreenS2CPacket.TYPE, UpdatePawnShopScreenS2CPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ShopActionC2SPacket.TYPE, ShopActionC2SPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PawnShopActionC2SPacket.TYPE, PawnShopActionC2SPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                ShopActionC2SPacket.TYPE,
                (payload, context) -> context.server().execute(() ->
                        ShopActionC2SPacket.handleServer(payload, context.player())
                )
        );
        ServerPlayNetworking.registerGlobalReceiver(
                PawnShopActionC2SPacket.TYPE,
                (payload, context) -> context.server().execute(() ->
                        PawnShopActionC2SPacket.handleServer(payload, context.player())
                )
        );

        NumismaticOverhaul.LOGGER.info("[ModNetworking] Сетевые каналы зарегистрированы.");
    }
}