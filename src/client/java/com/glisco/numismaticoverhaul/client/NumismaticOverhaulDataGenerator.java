package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.client.datagen.ModBlockLootSubProvider;
import com.glisco.numismaticoverhaul.client.datagen.ModBlockTagProvider;
import com.glisco.numismaticoverhaul.client.datagen.ModLanguageProvider;
import com.glisco.numismaticoverhaul.client.datagen.ModModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class NumismaticOverhaulDataGenerator implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ModModelProvider::new);
		pack.addProvider(ModBlockTagProvider::new);
		pack.addProvider(ModBlockLootSubProvider::new);
		pack.addProvider(ModLanguageProvider.English::new);
		pack.addProvider(ModLanguageProvider.Russian::new);
	}
}