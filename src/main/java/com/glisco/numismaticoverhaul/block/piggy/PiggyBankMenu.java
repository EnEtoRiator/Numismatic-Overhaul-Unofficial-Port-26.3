package com.glisco.numismaticoverhaul.block.piggy;

import com.glisco.numismaticoverhaul.item.ModItems;
import com.glisco.numismaticoverhaul.menu.ModMenus;
import com.glisco.numismaticoverhaul.tag.ModTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PiggyBankMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerLevelAccess access;

    // Клиентский конструктор
    public PiggyBankMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(3), ContainerLevelAccess.NULL);
    }

    // Серверный конструктор
    public PiggyBankMenu(int containerId, Inventory playerInventory, Container container, ContainerLevelAccess access) {
        super(ModMenus.PIGGY_BANK, containerId);
        checkContainerSize(container, 3);
        this.container = container;
        this.access = access;
        container.startOpen(playerInventory.player);

        // 3 слота под бронзовые, серебряные и золотые монеты
        this.addSlot(new CoinSlot(container, 0, 62, 26, ModItems.BRONZE_COIN));
        this.addSlot(new CoinSlot(container, 1, 80, 26, ModItems.SILVER_COIN));
        this.addSlot(new CoinSlot(container, 2, 98, 26, ModItems.GOLD_COIN));

        // Стандартный инвентарь игрока (3 ряда по 9 слотов)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 63 + row * 18));
            }
        }

        // Хотбар игрока (9 слотов)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 121));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (index < 3) {
                // Из копилки в инвентарь игрока
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Из инвентаря игрока в подходящий слот копилки
                if (!this.moveItemStackTo(slotStack, 0, 3, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, pos) ->
                        level.getBlockState(pos).is(ModTags.PIGGY_BANKS)
                                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0,
                true
        );
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    private static class CoinSlot extends Slot {
        private final Item allowedCoin;

        public CoinSlot(Container container, int slot, int x, int y, Item allowedCoin) {
            super(container, slot, x, y);
            this.allowedCoin = allowedCoin;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(this.allowedCoin);
        }
    }
}