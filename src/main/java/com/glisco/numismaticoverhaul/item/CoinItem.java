package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.currency.Currency;
import com.glisco.numismaticoverhaul.currency.PurseHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class CoinItem extends Item implements CurrencyItem {

    public final Currency currency;
    public final Style nameStyle;

    public CoinItem(Currency currency, Properties properties) {
        super(properties.stacksTo(99));
        this.currency = currency;
        this.nameStyle = Style.EMPTY.withColor(currency.getNameColor());
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack clickedStack, ItemStack cursorStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (slot instanceof MerchantResultSlot) return false;
        if (action != ClickAction.PRIMARY) return false;

        if ((cursorStack.is(this) && cursorStack.getCount() + clickedStack.getCount() <= cursorStack.getMaxStackSize())
                || !(cursorStack.getItem() instanceof CurrencyItem)) {
            return false;
        }

        ItemStack bag = MoneyBagItem.create(clickedStack, cursorStack);
        if (!slot.mayPlace(bag)) return false;

        slot.set(bag);
        access.set(ItemStack.EMPTY);
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        long rawValue = this.currency.getRawValue(stack.getCount());

        if (!level.isClientSide()) {
            PurseHelper.modify(player, rawValue);
            stack.setCount(0);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new CurrencyTooltipData(this.currency.getRawValue(stack.getCount()), -1));
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().setStyle(this.nameStyle);
    }

    @Override
    public boolean wasAdjusted(ItemStack other) {
        return !other.is(this);
    }

    @Override
    public long getValue(ItemStack stack) {
        return this.currency.getRawValue(stack.getCount());
    }

    @Override
    public long[] getCombinedValue(ItemStack stack) {
        long[] values = new long[3];
        values[this.currency.ordinal()] = stack.getCount();
        return values;
    }
}