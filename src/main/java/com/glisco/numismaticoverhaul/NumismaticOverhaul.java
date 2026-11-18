package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.attachment.ModAttachments;
import com.glisco.numismaticoverhaul.block.ModBlockEntities;
import com.glisco.numismaticoverhaul.block.ModBlocks;
import com.glisco.numismaticoverhaul.command.NumismaticCommand;
import com.glisco.numismaticoverhaul.component.ModComponents;
import com.glisco.numismaticoverhaul.config.ModGameRules;
import com.glisco.numismaticoverhaul.config.NumismaticConfig;
import com.glisco.numismaticoverhaul.event.ModEvents;
import com.glisco.numismaticoverhaul.item.ModCreativeTabs;
import com.glisco.numismaticoverhaul.item.ModItems;
import com.glisco.numismaticoverhaul.loot.ModLoot;
import com.glisco.numismaticoverhaul.menu.ModMenus;
import com.glisco.numismaticoverhaul.network.ModNetworking;
import com.glisco.numismaticoverhaul.recipe.ModRecipes;
import com.glisco.numismaticoverhaul.sound.ModSounds;
import com.glisco.numismaticoverhaul.villagers.VillagerTradesLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumismaticOverhaul implements ModInitializer {
	public static final String MOD_ID = "numismatic_overhaul";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		NumismaticConfig.get();
		ModRecipes.initialize();
		ModAttachments.initialize();
		ModComponents.initialize();
		ModItems.initialize();
		ModCreativeTabs.initialize();
		ModNetworking.initialize();
		ModSounds.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModMenus.initialize();
		ModGameRules.initialize();
		ModLoot.initialize();
		ModEvents.initialize();
		NumismaticCommand.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register(server ->
				VillagerTradesLoader.reload(server.getResourceManager(), server.registryAccess())
		);
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (success) {
				VillagerTradesLoader.reload(resourceManager, server.registryAccess());
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}