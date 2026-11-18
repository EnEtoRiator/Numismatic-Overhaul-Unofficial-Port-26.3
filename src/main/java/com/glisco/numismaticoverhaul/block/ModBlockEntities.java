package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.pawn.PawnShopBlockEntity;
import com.glisco.numismaticoverhaul.block.piggy.PiggyBankBlockEntity;
import com.glisco.numismaticoverhaul.block.shop.ShopBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;

public final class ModBlockEntities {

    public static final BlockEntityType<PiggyBankBlockEntity> PIGGY_BANK = registerPiggyBankEntity();

    private static BlockEntityType<PiggyBankBlockEntity> registerPiggyBankEntity() {
        List<Block> allPiggyBanks = new ArrayList<>();
        allPiggyBanks.add(ModBlocks.PIGGY_BANK);
        allPiggyBanks.addAll(ModBlocks.COLORED_PIGGY_BANKS.values());

        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                NumismaticOverhaul.id("piggy_bank"),
                FabricBlockEntityTypeBuilder.create(
                        PiggyBankBlockEntity::new,
                        allPiggyBanks.toArray(new Block[0])
                ).build()
        );
    }

    public static final BlockEntityType<ShopBlockEntity> SHOP = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            NumismaticOverhaul.id("shop"),
            FabricBlockEntityTypeBuilder.create(
                    ShopBlockEntity::new,
                    ModBlocks.SHOP,
                    ModBlocks.INEXHAUSTIBLE_SHOP
            ).build()
    );

    public static final BlockEntityType<PawnShopBlockEntity> PAWN_SHOP = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            NumismaticOverhaul.id("pawn_shop"),
            FabricBlockEntityTypeBuilder.create(
                    PawnShopBlockEntity::new,
                    ModBlocks.PAWN_SHOP,
                    ModBlocks.INEXHAUSTIBLE_PAWN_SHOP
            ).build()
    );

    public static void initialize() {
        NumismaticOverhaul.LOGGER.info("[ModBlockEntities] BlockEntity зарегистрированы.");
    }
}