package com.glisco.numismaticoverhaul.recipe;

import com.glisco.numismaticoverhaul.block.piggy.PiggyBankBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class PiggyBankColoringRecipe extends CustomRecipe {

    public static final PiggyBankColoringRecipe INSTANCE = new PiggyBankColoringRecipe(CraftingBookCategory.MISC);
    public static final MapCodec<PiggyBankColoringRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, PiggyBankColoringRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public PiggyBankColoringRecipe(CraftingBookCategory category) {
        super();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int piggyCount = 0;
        int dyeCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (Block.byItem(stack.getItem()) instanceof PiggyBankBlock) {
                piggyCount++;
            } else if (stack.getItem() instanceof DyeItem) {
                dyeCount++;
            } else {
                return false;
            }

            if (piggyCount > 1 || dyeCount > 1) return false;
        }

        return piggyCount == 1 && dyeCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack piggyStack = ItemStack.EMPTY;
        ItemStack dyeStack = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (Block.byItem(stack.getItem()) instanceof PiggyBankBlock) {
                piggyStack = stack;
            } else if (stack.getItem() instanceof DyeItem) {
                dyeStack = stack;
            }
        }

        if (piggyStack.isEmpty() || dyeStack.isEmpty()) return ItemStack.EMPTY;

        // Если в 26.3 краситель хранит цвет в DataComponents.DYE:
        DyeColor color = dyeStack.get(DataComponents.DYE);
        // (если такого компонента нет — вызови метод из ((DyeItem) dyeStack.getItem()))
        if (color == null) return ItemStack.EMPTY;

        ItemStack result = PiggyBankBlock.getPiggy(color);
        result.applyComponents(piggyStack.getComponentsPatch());
        return result;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipes.PIGGY_BANK_COLORING;
    }
}