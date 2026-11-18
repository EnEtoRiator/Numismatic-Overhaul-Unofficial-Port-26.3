package com.glisco.numismaticoverhaul.client.datagen;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.ModBlocks;
import com.glisco.numismaticoverhaul.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        // 3D-модели и blockstates копилок и магазинов уже лежат в assets/numismatic_overhaul/
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        // Определения items/*.json для 3D-моделей блоков (копилки и магазины)
        registerBlockItem(itemModelGenerator, ModBlocks.PIGGY_BANK, NumismaticOverhaul.id("block/piggy_bank_base"));
        ModBlocks.COLORED_PIGGY_BANKS.values().forEach(block ->
                registerBlockItem(itemModelGenerator, block, ModelLocationUtils.getModelLocation(block))
        );

        registerBlockItem(itemModelGenerator, ModBlocks.SHOP, ModelLocationUtils.getModelLocation(ModBlocks.SHOP));
        registerBlockItem(itemModelGenerator, ModBlocks.PAWN_SHOP, ModelLocationUtils.getModelLocation(ModBlocks.PAWN_SHOP));
        registerBlockItem(itemModelGenerator, ModBlocks.INEXHAUSTIBLE_SHOP, ModelLocationUtils.getModelLocation(ModBlocks.INEXHAUSTIBLE_SHOP));
        registerBlockItem(itemModelGenerator, ModBlocks.INEXHAUSTIBLE_PAWN_SHOP, ModelLocationUtils.getModelLocation(ModBlocks.INEXHAUSTIBLE_PAWN_SHOP));
    }

    private static void registerBlockItem(ItemModelGenerators generator, Block block, Identifier modelId) {
        generator.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));
    }
}