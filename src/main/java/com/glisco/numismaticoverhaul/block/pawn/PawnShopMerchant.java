package com.glisco.numismaticoverhaul.block.pawn;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PawnShopMerchant implements Merchant {

    private final PawnShopBlockEntity shop;
    private final boolean inexhaustible;
    private MerchantOffers offers = new MerchantOffers();
    @Nullable
    private Player customer;

    public PawnShopMerchant(PawnShopBlockEntity shop, boolean inexhaustible) {
        this.shop = shop;
        this.inexhaustible = inexhaustible;
    }

    public void updateTrades() {
        this.offers.clear();
        for (PawnShopOffer offer : this.shop.getOffers()) {
            this.offers.add(offer.toMerchantOffer(this.shop, this.inexhaustible));
        }
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.customer = player;
        this.shop.busy = player != null;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.customer;
    }

    @Override
    public MerchantOffers getOffers() {
        return this.offers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        if (!this.inexhaustible) {
            PawnShopOffer.add(this.shop, offer.getCostA());
            this.shop.removeCurrency(CurrencyHelper.getValue(List.of(offer.getResult())));
            this.updateTrades();
            if (this.customer instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendMerchantOffers(
                        serverPlayer.containerMenu.containerId,
                        this.offers,
                        0, 0, false, false
                );
            }
        }
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {}

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {}

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.shop != null
                && this.shop.getLevel() != null
                && this.shop.getLevel().getBlockEntity(this.shop.getBlockPos()) == this.shop
                && this.shop.getBlockPos().distToCenterSqr(player.position()) <= 100.0;
    }

    public PawnShopBlockEntity shop() {
        return this.shop;
    }
}