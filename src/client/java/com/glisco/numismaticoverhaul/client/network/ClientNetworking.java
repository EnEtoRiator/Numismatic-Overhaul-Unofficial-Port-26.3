package com.glisco.numismaticoverhaul.client.network;

import com.glisco.numismaticoverhaul.attachment.ModAttachments;
import com.glisco.numismaticoverhaul.block.pawn.PawnShopMenu;
import com.glisco.numismaticoverhaul.block.shop.ShopMenu;
import com.glisco.numismaticoverhaul.client.gui.PawnShopScreen;
import com.glisco.numismaticoverhaul.client.gui.ShopScreen;
import com.glisco.numismaticoverhaul.network.SyncPurseS2CPacket;
import com.glisco.numismaticoverhaul.network.UpdatePawnShopScreenS2CPacket;
import com.glisco.numismaticoverhaul.network.UpdateShopScreenS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientNetworking {

    private ClientNetworking() {}

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(SyncPurseS2CPacket.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().player != null) {
                        context.client().player.setAttached(ModAttachments.PURSE_BALANCE, payload.balance());
                    }
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(UpdateShopScreenS2CPacket.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().gui.screen() instanceof ShopScreen screen) {
                        screen.updateFromServer(payload);
                    } else if (context.client().player != null
                            && context.client().player.containerMenu instanceof ShopMenu menu) {
                        menu.setClientBufferStack(payload.bufferStack());
                    }
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(UpdatePawnShopScreenS2CPacket.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().gui.screen() instanceof PawnShopScreen screen) {
                        screen.updateFromServer(payload);
                    } else if (context.client().player != null
                            && context.client().player.containerMenu instanceof PawnShopMenu menu) {
                        menu.setClientBufferStack(payload.bufferStack());
                    }
                })
        );
    }
}