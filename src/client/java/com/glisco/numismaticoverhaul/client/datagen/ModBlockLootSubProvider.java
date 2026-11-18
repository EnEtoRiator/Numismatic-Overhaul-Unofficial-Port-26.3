package com.glisco.numismaticoverhaul.client.datagen;

import com.glisco.numismaticoverhaul.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootSubProvider extends FabricBlockLootSubProvider {

    public ModBlockLootSubProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        dropSelf(ModBlocks.PIGGY_BANK);
        ModBlocks.COLORED_PIGGY_BANKS.values().forEach(this::dropSelf);

        dropSelf(ModBlocks.SHOP);
        dropSelf(ModBlocks.PAWN_SHOP);
        dropSelf(ModBlocks.INEXHAUSTIBLE_SHOP);
        dropSelf(ModBlocks.INEXHAUSTIBLE_PAWN_SHOP);
    }
}