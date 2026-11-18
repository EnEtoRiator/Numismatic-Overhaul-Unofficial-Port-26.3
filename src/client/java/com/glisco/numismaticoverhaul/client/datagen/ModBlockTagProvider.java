package com.glisco.numismaticoverhaul.client.datagen;

import com.glisco.numismaticoverhaul.block.ModBlocks;
import com.glisco.numismaticoverhaul.tag.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {

    public ModBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        var piggyBuilder = builder(ModTags.PIGGY_BANKS)
                .add(ModBlocks.PIGGY_BANK.builtInRegistryHolder().key());
        ModBlocks.COLORED_PIGGY_BANKS.values().forEach(block ->
                piggyBuilder.add(block.builtInRegistryHolder().key())
        );

        builder(ModTags.VERY_HEAVY_BLOCKS)
                .add(
                        Blocks.ANVIL.builtInRegistryHolder().key(),
                        Blocks.CHIPPED_ANVIL.builtInRegistryHolder().key(),
                        Blocks.DAMAGED_ANVIL.builtInRegistryHolder().key()
                );

        builder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
                        ModBlocks.SHOP.builtInRegistryHolder().key(),
                        ModBlocks.PAWN_SHOP.builtInRegistryHolder().key(),
                        ModBlocks.INEXHAUSTIBLE_SHOP.builtInRegistryHolder().key(),
                        ModBlocks.INEXHAUSTIBLE_PAWN_SHOP.builtInRegistryHolder().key()
                );
    }
}