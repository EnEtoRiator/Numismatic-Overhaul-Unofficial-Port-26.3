package com.glisco.numismaticoverhaul.block.shop;

import com.glisco.numismaticoverhaul.block.ModBlockEntities;
import com.glisco.numismaticoverhaul.block.pawn.PawnShopBlockEntity;
import com.glisco.numismaticoverhaul.network.UpdateShopScreenS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ShopBlock extends Block implements EntityBlock {

    private static final VoxelShape MAIN_PILLAR = Block.box(1, 0, 1, 15, 8, 15);
    private static final VoxelShape PLATE = Block.box(0, 8, 0, 16, 12, 16);
    private static final VoxelShape PILLAR_1 = Block.box(13, 0, 0, 16, 8, 3);
    private static final VoxelShape PILLAR_2 = Block.box(0, 0, 0, 3, 8, 3);
    private static final VoxelShape PILLAR_3 = Block.box(0, 0, 13, 3, 8, 16);
    private static final VoxelShape PILLAR_4 = Block.box(13, 0, 13, 16, 8, 16);
    private static final VoxelShape SHAPE = Shapes.or(MAIN_PILLAR, PLATE, PILLAR_1, PILLAR_2, PILLAR_3, PILLAR_4);

    private final boolean inexhaustible;

    public ShopBlock(boolean inexhaustible, Properties properties) {
        super(properties);
        this.inexhaustible = inexhaustible;
    }

    public boolean inexhaustible() {
        return this.inexhaustible;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ShopBlockEntity shop)) return InteractionResult.FAIL;
        if (shop.busy) return InteractionResult.SUCCESS;

        if (player.getUUID().equals(shop.getOwner()) && !player.isShiftKeyDown()) {
            player.openMenu(shop);
            if (player instanceof ServerPlayer serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new UpdateShopScreenS2CPacket(shop, ItemStack.EMPTY));
            }
            shop.busy = true;
            return InteractionResult.SUCCESS;
        }

        ShopMerchant merchant = shop.getMerchant();
        merchant.updateTrades();
        merchant.setTradingPlayer(player);
        merchant.openTradingScreen(player, Component.translatable("gui.numismatic_overhaul.shop.merchant_title"), 0);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide()) return;
        if (!(placer instanceof ServerPlayer)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (level.getBlockEntity(pos) instanceof ShopBlockEntity shop) {
            if (stack.has(DataComponents.CUSTOM_NAME)) {
                shop.setComponents(DataComponentMap.builder().set(DataComponents.CUSTOM_NAME, stack.getHoverName()).build());
            }
            shop.setOwner(placer.getUUID());
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShopBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.SHOP
                ? (lvl, pos, st, be) -> ShopBlockEntity.tick(lvl, pos, st, (ShopBlockEntity) be)
                : null;
    }
}