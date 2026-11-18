package com.glisco.numismaticoverhaul.recipe;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ModRecipes {

    public static final RecipeSerializer<PiggyBankColoringRecipe> PIGGY_BANK_COLORING = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            NumismaticOverhaul.id("piggy_bank_coloring"),
            new RecipeSerializer<>(PiggyBankColoringRecipe.CODEC, PiggyBankColoringRecipe.STREAM_CODEC)
    );

    public static void initialize() {
        NumismaticOverhaul.LOGGER.info("[ModRecipes] Сериализаторы рецептов зарегистрированы.");
    }
}