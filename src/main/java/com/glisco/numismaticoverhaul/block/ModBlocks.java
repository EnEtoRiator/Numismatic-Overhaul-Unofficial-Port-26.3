package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.pawn.PawnShopBlock;
import com.glisco.numismaticoverhaul.block.piggy.PiggyBankBlock;
import com.glisco.numismaticoverhaul.block.shop.ShopBlock;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.item.CurrencyTooltipData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ModBlocks {

    public static final Block PIGGY_BANK = registerPiggyBank("piggy_bank", null);
    public static final Map<DyeColor, Block> COLORED_PIGGY_BANKS = new EnumMap<>(DyeColor.class);

    public static final Block SHOP = registerShop("shop", false, Rarity.COMMON);
    public static final Block PAWN_SHOP = registerPawnShop("pawn_shop", false, Rarity.COMMON);
    public static final Block INEXHAUSTIBLE_SHOP = registerShop("inexhaustible_shop", true, Rarity.EPIC);
    public static final Block INEXHAUSTIBLE_PAWN_SHOP = registerPawnShop("inexhaustible_pawn_shop", true, Rarity.EPIC);

    static {
        for (DyeColor color : DyeColor.values()) {
            COLORED_PIGGY_BANKS.put(color, registerPiggyBank(color.getName() + "_piggy_bank", color));
        }
    }

    private static Block registerPiggyBank(String name, @Nullable DyeColor color) {
        BlockBehaviour.Properties blockProps = BlockBehaviour.Properties.of()
                .strength(1.25F, 4.2F)
                .noOcclusion();

        return registerBlock(
                name,
                blockProps,
                props -> new PiggyBankBlock(color, props),
                (block, itemProps) -> new BlockItem(
                        block,
                        itemProps.stacksTo(1).equippable(EquipmentSlot.HEAD)
                ) {
                    @Override
                    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
                        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
                        long[] values = CurrencyHelper.getFromContainer(contents);
                        return Optional.of(new CurrencyTooltipData(values, new long[]{-1}));
                    }
                }
        );
    }

    private static Block registerShop(String name, boolean inexhaustible, Rarity rarity) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .strength(5.0F)
                .noOcclusion();
        return registerBlock(
                name,
                props,
                p -> new ShopBlock(inexhaustible, p),
                (block, itemProps) -> new BlockItem(block, itemProps.rarity(rarity)) {
                    @Override
                    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
                        tooltip.accept(Component.translatable(block.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
                    }
                }
        );
    }

    private static Block registerPawnShop(String name, boolean inexhaustible, Rarity rarity) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .strength(5.0F)
                .noOcclusion();
        return registerBlock(
                name,
                props,
                p -> new PawnShopBlock(inexhaustible, p),
                (block, itemProps) -> new BlockItem(block, itemProps.rarity(rarity)) {
                    @Override
                    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
                        tooltip.accept(Component.translatable(block.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
                    }
                }
        );
    }

    public static <T extends Block> T registerBlock(
            String name,
            BlockBehaviour.Properties blockProperties,
            Function<BlockBehaviour.Properties, T> blockFactory,
            Function<T, BlockItem> itemFactory
    ) {
        Identifier id = NumismaticOverhaul.id(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        blockProperties.setId(blockKey);

        T block = blockFactory.apply(blockProperties);
        T registeredBlock = Registry.register(BuiltInRegistries.BLOCK, id, block);

        BlockItem blockItem = itemFactory.apply(registeredBlock);
        Registry.register(BuiltInRegistries.ITEM, id, blockItem);

        return registeredBlock;
    }

    public static <T extends Block> T registerBlock(
            String name,
            BlockBehaviour.Properties blockProperties,
            Function<BlockBehaviour.Properties, T> blockFactory,
            java.util.function.BiFunction<T, Item.Properties, BlockItem> itemFactory
    ) {
        Identifier id = NumismaticOverhaul.id(name);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Item.Properties itemProps = new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(itemKey);

        return registerBlock(name, blockProperties, blockFactory, block -> itemFactory.apply(block, itemProps));
    }

    public static void initialize() {
        NumismaticOverhaul.LOGGER.info("[ModBlocks] Копилки (17 вариантов) зарегистрированы.");
    }
}