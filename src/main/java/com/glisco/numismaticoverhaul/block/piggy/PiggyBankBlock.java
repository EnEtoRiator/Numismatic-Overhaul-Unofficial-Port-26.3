package com.glisco.numismaticoverhaul.block.piggy;

import com.glisco.numismaticoverhaul.block.ModBlocks;
import com.glisco.numismaticoverhaul.sound.ModSounds;
import com.glisco.numismaticoverhaul.tag.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PiggyBankBlock extends HorizontalDirectionalBlock implements EntityBlock {

    @Nullable
    private final DyeColor color;

    private static final VoxelShape NORTH_SHAPE = Stream.of(
            Block.box(7, 2, 4, 9, 4, 5),
            Block.box(5, 1, 5, 11, 6, 11),
            Block.box(5, 0, 5, 6, 1, 7),
            Block.box(5, 0, 9, 6, 1, 11),
            Block.box(10, 0, 9, 11, 1, 11),
            Block.box(10, 0, 5, 11, 1, 7)
    ).reduce(Shapes::or).get();

    private static final VoxelShape SOUTH_SHAPE = Stream.of(
            Block.box(7, 2, 11, 9, 4, 12),
            Block.box(5, 1, 5, 11, 6, 11),
            Block.box(10, 0, 9, 11, 1, 11),
            Block.box(10, 0, 5, 11, 1, 7),
            Block.box(5, 0, 5, 6, 1, 7),
            Block.box(5, 0, 9, 6, 1, 11)
    ).reduce(Shapes::or).get();

    private static final VoxelShape EAST_SHAPE = Stream.of(
            Block.box(11, 2, 7, 12, 4, 9),
            Block.box(5, 1, 5, 11, 6, 11),
            Block.box(9, 0, 5, 11, 1, 6),
            Block.box(5, 0, 5, 7, 1, 6),
            Block.box(5, 0, 10, 7, 1, 11),
            Block.box(9, 0, 10, 11, 1, 11)
    ).reduce(Shapes::or).get();

    private static final VoxelShape WEST_SHAPE = Stream.of(
            Block.box(4, 2, 7, 5, 4, 9),
            Block.box(5, 1, 5, 11, 6, 11),
            Block.box(5, 0, 10, 7, 1, 11),
            Block.box(9, 0, 10, 11, 1, 11),
            Block.box(9, 0, 5, 11, 1, 6),
            Block.box(5, 0, 5, 7, 1, 6)
    ).reduce(Shapes::or).get();

    public PiggyBankBlock(@Nullable DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    public DyeColor getColor() {
        return this.color;
    }

    public static ItemStack getPiggy(@Nullable DyeColor color) {
        if (color == null) return new ItemStack(ModBlocks.PIGGY_BANK);
        return new ItemStack(ModBlocks.COLORED_PIGGY_BANKS.get(color));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MenuProvider provider) {
            player.openMenu(provider);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        if (entity instanceof FallingBlockEntity fallingBlock
                && fallingBlock.getBlockState().is(ModTags.VERY_HEAVY_BLOCKS)
                && !level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof PiggyBankBlockEntity piggyBank) {
                BlockPos dropPos = pos.relative(state.getValue(FACING).getOpposite());
                Containers.dropContents(level, dropPos, piggyBank);
            }
            level.removeBlock(pos, false);
            breakEffects(level, pos, state, Math.max(1, (int) Math.round(fallDistance)));
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof PiggyBankBlockEntity piggyBank
                && player.isCreative()
                && !level.isClientSide()
                && !piggyBank.isEmpty()) {
            ItemStack stack = getPiggy(this.color);
            stack.applyComponents(piggyBank.collectComponents());
            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof PiggyBankBlockEntity piggyBank) {
            if (params.getOptionalParameter(LootContextParams.TOOL) instanceof DataComponentHolder holder) {
                Component customName = holder.get(DataComponents.CUSTOM_NAME);
                if (customName != null && Objects.equals(customName.getString(), "Hammer")) {
                    Level level = piggyBank.getLevel();
                    if (level != null) {
                        breakEffects(level, piggyBank.getBlockPos(), state, 5);
                    }
                    List<ItemStack> drops = new ArrayList<>();
                    for (int i = 0; i < piggyBank.getContainerSize(); i++) {
                        ItemStack stack = piggyBank.getItem(i);
                        if (!stack.isEmpty()) {
                            drops.add(stack.copy());
                        }
                    }
                    return drops;
                }
            }
        }
        return super.getDrops(state, params);
    }

    private static void breakEffects(Level level, BlockPos pos, BlockState state, int intensity) {
        level.playSound(null, pos, ModSounds.PIGGY_BANK_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                    6 * intensity, 0.25, 0.2, 0.25, 0.1
            );
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PiggyBankBlockEntity(pos, state);
    }
}