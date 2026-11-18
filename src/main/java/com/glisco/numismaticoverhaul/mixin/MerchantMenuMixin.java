package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.block.pawn.PawnShopMerchant;
import com.glisco.numismaticoverhaul.block.shop.ShopMerchant;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.currency.PurseHelper;
import com.glisco.numismaticoverhaul.item.CoinItem;
import com.glisco.numismaticoverhaul.item.ModItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin {

    @Shadow
    @Final
    private Merchant trader;

    @Inject(method = "tryMoveItems", at = @At("TAIL"))
    private void autofillFromPurse(int index, CallbackInfo ci) {
        MerchantMenu menu = (MerchantMenu) (Object) this;
        MerchantOffers offers = menu.getOffers();
        if (index < 0 || index >= offers.size()) return;

        MerchantOffer offer = offers.get(index);

        Player player = null;
        if (menu.slots.size() > 3 && menu.slots.get(3).container instanceof Inventory inv) {
            player = inv.player;
        }
        if (player == null || player.level().isClientSide()) return;

        numismatic$autofillSlot(menu, 0, offer.getCostA(), player);
        ItemStack costB = offer.getCostB();
        if (!costB.isEmpty()) {
            numismatic$autofillSlot(menu, 1, costB, player);
        }
    }

    @Unique
    private static void numismatic$autofillSlot(MerchantMenu menu, int slotIndex, ItemStack requiredStack, Player player) {
        if (requiredStack.isEmpty()) return;

        if (requiredStack.getItem() instanceof CoinItem coinItem) {
            long requiredValue = coinItem.currency.getRawValue(requiredStack.getCount());
            ItemStack currentInSlot = menu.getSlot(slotIndex).getItem();
            long presentValue = currentInSlot.is(coinItem)
                    ? coinItem.currency.getRawValue(currentInSlot.getCount())
                    : 0L;

            long neededValue = requiredValue - presentValue;
            if (neededValue > 0 && PurseHelper.getBalance(player) >= neededValue) {
                PurseHelper.modify(player, -neededValue);
                menu.getSlot(slotIndex).set(requiredStack.copy());
            }
        } else if (requiredStack.is(ModItems.MONEY_BAG)) {
            ItemStack currentInSlot = menu.getSlot(slotIndex).getItem();
            if (ItemStack.isSameItemSameComponents(requiredStack, currentInSlot)) return;

            long requiredValue = ModItems.MONEY_BAG.getValue(requiredStack);
            long invMoney = CurrencyHelper.getMoneyInInventory(player, false);
            long neededFromPurse = requiredValue - invMoney;

            if (neededFromPurse > PurseHelper.getBalance(player)) return;

            if (neededFromPurse <= 0) {
                CurrencyHelper.deductFromInventory(player, requiredValue);
            } else {
                CurrencyHelper.deductFromInventory(player, invMoney);
                PurseHelper.modify(player, -neededFromPurse);
            }
            menu.getSlot(slotIndex).set(requiredStack.copy());
        }
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void checkShopDistance(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (this.trader instanceof ShopMerchant shopMerchant) {
            var shop = shopMerchant.shop();
            if (shop.getLevel() == null
                    || shop.getLevel().getBlockEntity(shop.getBlockPos()) != shop
                    || shop.getBlockPos().distToCenterSqr(player.position()) > 100.0) {
                cir.setReturnValue(false);
            }
        } else if (this.trader instanceof PawnShopMerchant pawnMerchant) {
            var shop = pawnMerchant.shop();
            if (shop.getLevel() == null
                    || shop.getLevel().getBlockEntity(shop.getBlockPos()) != shop
                    || shop.getBlockPos().distToCenterSqr(player.position()) > 100.0) {
                cir.setReturnValue(false);
            }
        }
    }
}