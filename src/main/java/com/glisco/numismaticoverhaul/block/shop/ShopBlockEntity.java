package com.glisco.numismaticoverhaul.block.shop;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.ModBlockEntities;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class ShopBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {

    private static final int[] SLOTS = IntStream.range(0, 27).toArray();
    private static final int[] NO_SLOTS = new int[0];

    private NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
    public boolean busy = false;
    private final ShopMerchant merchant;
    private List<ShopOffer> offers = new ArrayList<>();
    private long storedCurrency = 0L;
    @Nullable
    private UUID owner;
    private boolean allowsTransfer = false;
    private int tradeIndex = 0;

    public ShopBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHOP, pos, state);
        boolean inexhaustible = (state.getBlock() instanceof ShopBlock shop) && shop.inexhaustible();
        this.merchant = new ShopMerchant(this, inexhaustible);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return this.allowsTransfer ? SLOTS : NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return this.allowsTransfer;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("gui.numismatic_overhaul.shop.inventory_title");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.inventory = items;
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    public ShopMerchant getMerchant() {
        return this.merchant;
    }

    public List<ShopOffer> getOffers() {
        return this.offers;
    }

    public long getStoredCurrency() {
        return this.storedCurrency;
    }

    public boolean isTransferEnabled() {
        return this.allowsTransfer;
    }

    public void toggleTransfer() {
        this.allowsTransfer = !this.allowsTransfer;
        this.setChanged();
    }

    public void setStoredCurrency(long storedCurrency) {
        this.storedCurrency = Math.max(0L, storedCurrency);
        this.setChanged();
    }

    public void addCurrency(long value) {
        this.storedCurrency += value;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.inventory);
        output.store("Offers", ShopOffer.CODEC.listOf(), this.offers);
        output.putBoolean("AllowsTransfer", this.allowsTransfer);
        output.putLong("StoredCurrency", this.storedCurrency);
        if (this.owner != null) {
            output.store("Owner", UUIDUtil.CODEC, this.owner);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inventory = NonNullList.withSize(27, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.inventory);
        this.offers = new ArrayList<>(input.read("Offers", ShopOffer.CODEC.listOf()).orElse(List.of()));
        this.allowsTransfer = input.getBooleanOr("AllowsTransfer", false);
        this.storedCurrency = input.getLongOr("StoredCurrency", 0L);
        this.owner = input.read("Owner", UUIDUtil.CODEC).orElse(null);
    }

    public void addOrReplaceOffer(ShopOffer offer) {
        int indexToReplace = -1;
        for (int i = 0; i < this.offers.size(); i++) {
            if (ItemStack.isSameItemSameComponents(offer.getSellStack(), this.offers.get(i).getSellStack())) {
                indexToReplace = i;
                break;
            }
        }
        if (indexToReplace == -1) {
            if (this.offers.size() >= 24) {
                NumismaticOverhaul.LOGGER.error("Попытка добавить более 24 сделок в магазин на {}", this.worldPosition);
                return;
            }
            this.offers.add(offer);
        } else {
            this.offers.set(indexToReplace, offer);
        }
        this.setChanged();
    }

    public void deleteOffer(ItemStack stack) {
        if (!this.offers.removeIf(offer -> ItemStack.isSameItemSameComponents(stack, offer.getSellStack()))) {
            NumismaticOverhaul.LOGGER.error("Попытка удалить несуществующую сделку {} из магазина на {}", stack, this.worldPosition);
            return;
        }
        this.setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ShopBlockEntity blockEntity) {
        if (level.getGameTime() % 60 == 0) {
            blockEntity.tradeIndex++;
        }
    }

    public ItemStack getItemToRender() {
        if (this.offers.isEmpty()) return ItemStack.EMPTY;
        if (this.tradeIndex >= this.offers.size()) this.tradeIndex = 0;
        return this.offers.get(this.tradeIndex).getSellStack();
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new ShopMenu(containerId, playerInventory, this);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null) return false;
        return player.getUUID().equals(this.owner)
                && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.merchant.setTradingPlayer(null);
        if (this.level != null && !this.level.isClientSide() && this.storedCurrency > 0) {
            CurrencyConverter.getAsValidStacks(this.storedCurrency)
                    .forEach(stack -> Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), stack));
            this.storedCurrency = 0L;
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Nullable
    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        output.store("Offers", ShopOffer.CODEC.listOf(), this.offers);
        return output.buildResult();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }
}