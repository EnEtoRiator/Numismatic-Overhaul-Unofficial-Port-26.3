package com.glisco.numismaticoverhaul.block.shop;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.PurseHelper;
import com.glisco.numismaticoverhaul.menu.ModMenus;
import com.glisco.numismaticoverhaul.network.ShopActionC2SPacket;
import com.glisco.numismaticoverhaul.network.UpdateShopScreenS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShopMenu extends AbstractContainerMenu {

    private final Player owner;
    private final Container shopInventory;
    @Nullable
    private ShopBlockEntity shop;
    private ItemStack bufferStack = ItemStack.EMPTY;
    private boolean inventoryTabActive = true;

    public ShopMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(27));
    }

    public ShopMenu(int containerId, Inventory playerInventory, Container shopInventory) {
        super(ModMenus.SHOP, containerId);
        checkContainerSize(shopInventory, 27);
        this.shopInventory = shopInventory;
        this.owner = playerInventory.player;

        if (!this.owner.level().isClientSide() && shopInventory instanceof ShopBlockEntity blockEntity) {
            this.shop = blockEntity;
        }

        // 27 слотов склада магазина (скрываются при переключении на вкладку сделок)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new AutoHidingSlot(shopInventory, col + row * 9, 8 + col * 18, 17 + row * 18));
            }
        }

        // Инвентарь игрока
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 85 + row * 18));
            }
        }

        // Хотбар
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 143));
        }
    }

    public void setInventoryTabActive(boolean active) {
        this.inventoryTabActive = active;
    }

    public boolean isInventoryTabActive() {
        return this.inventoryTabActive;
    }

    public ItemStack getBufferStack() {
        return this.bufferStack;
    }

    public void setClientBufferStack(ItemStack stack) {
        this.bufferStack = stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.shopInventory.stillValid(player);
    }

    public void loadOffer(long index) {
        if (this.shop == null) return;
        List<ShopOffer> offers = this.shop.getOffers();
        if (index < 0 || index >= offers.size()) {
            NumismaticOverhaul.LOGGER.error("Игрок {} попытался загрузить неверный индекс сделки {}", this.owner.getName().getString(), index);
            return;
        }
        this.bufferStack = offers.get((int) index).getSellStack();
        this.updateClient();
    }

    public void createOffer(long price, ItemStack clientStack) {
        if (this.shop == null || price <= 0) return;
        ItemStack target = !clientStack.isEmpty() ? clientStack : this.bufferStack;
        if (target.isEmpty()) return;
        this.bufferStack = target.copy();
        this.shop.addOrReplaceOffer(new ShopOffer(this.bufferStack.copy(), price));
        this.updateClient();
    }

    public void setBufferFromClient(ItemStack stack) {
        if (this.shop == null) return;
        ItemStack source = !stack.isEmpty() ? stack : this.getCarried();
        this.bufferStack = source.isEmpty() ? ItemStack.EMPTY : source.copy();
        this.updateClient();
    }

    public void deleteOffer(ItemStack clientStack) {
        if (this.shop == null) return;
        ItemStack target = !clientStack.isEmpty() ? clientStack : this.bufferStack;
        if (target.isEmpty()) return;
        this.shop.deleteOffer(target);
        if (ItemStack.isSameItemSameComponents(this.bufferStack, target)) {
            this.bufferStack = ItemStack.EMPTY;
        }
        this.updateClient();
    }

    public void extractCurrency() {
        if (this.shop == null) return;
        long stored = this.shop.getStoredCurrency();
        if (stored > 0) {
            PurseHelper.modify(this.owner, stored);
            this.shop.setStoredCurrency(0L);
        }
        this.updateClient();
    }

    public void toggleTransfer() {
        if (this.shop == null) return;
        this.shop.toggleTransfer();
        this.updateClient();
    }

    public void handleBufferClick() {
        if (this.shop == null) return;
        this.bufferStack = this.getCarried().copy();
        this.updateClient();
    }

    private void updateClient() {
        if (this.shop != null && this.owner instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new UpdateShopScreenS2CPacket(this.shop, this.bufferStack));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (index < 27) {
                if (!this.moveItemStackTo(slotStack, 27, 63, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(slotStack, 0, 27, false)) {
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
    public void removed(Player player) {
        super.removed(player);
        if (this.shop != null) {
            this.shop.busy = false;
        }
    }

    private class AutoHidingSlot extends Slot {
        public AutoHidingSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return ShopMenu.this.inventoryTabActive;
        }
    }
}