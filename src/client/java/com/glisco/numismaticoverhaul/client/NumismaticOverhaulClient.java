package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.ModBlockEntities;
import com.glisco.numismaticoverhaul.client.gui.*;
import com.glisco.numismaticoverhaul.client.network.ClientNetworking;
import com.glisco.numismaticoverhaul.client.render.PawnShopBlockEntityRenderer;
import com.glisco.numismaticoverhaul.client.render.ShopBlockEntityRenderer;
import com.glisco.numismaticoverhaul.item.CurrencyTooltipData;
import com.glisco.numismaticoverhaul.menu.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class NumismaticOverhaulClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientNetworking.initialize();

		MenuScreens.register(ModMenus.PIGGY_BANK, PiggyBankScreen::new);
		MenuScreens.register(ModMenus.SHOP, ShopScreen::new);
		MenuScreens.register(ModMenus.PAWN_SHOP, PawnShopScreen::new);
		PurseOverlay.initialize();
		BlockEntityRenderers.register(ModBlockEntities.SHOP, ShopBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.PAWN_SHOP, PawnShopBlockEntityRenderer::new);

		ClientTooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof CurrencyTooltipData currencyData) {
				return new CurrencyTooltipComponent(currencyData);
			}
			return null;
		});

		NumismaticOverhaul.LOGGER.info("[Client] Клиентская часть Numismatic Overhaul инициализирована.");
	}
}