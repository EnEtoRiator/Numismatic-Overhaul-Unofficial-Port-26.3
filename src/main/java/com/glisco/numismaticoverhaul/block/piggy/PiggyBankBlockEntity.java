package com.glisco.numismaticoverhaul.block.piggy;

import com.glisco.numismaticoverhaul.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PiggyBankBlockEntity extends BaseContainerBlockEntity {

    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    public PiggyBankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PIGGY_BANK, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.numismatic_overhaul.piggy_bank");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        ContainerLevelAccess access = this.level != null
                ? ContainerLevelAccess.create(this.level, this.worldPosition)
                : ContainerLevelAccess.NULL;
        return new PiggyBankMenu(containerId, playerInventory, this, access);
    }

    @Override
    public int getContainerSize() {
        return 3;
    }
}