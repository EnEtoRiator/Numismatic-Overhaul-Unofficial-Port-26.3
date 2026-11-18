package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.component.ModComponents;
import com.glisco.numismaticoverhaul.component.MoneyBagComponent;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.currency.PurseHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

import java.util.List;
import java.util.Optional;

public class MoneyBagItem extends Item implements CurrencyItem {

    public MoneyBagItem(Properties properties) {
        super(properties.stacksTo(1).component(ModComponents.MONEY_BAG, MoneyBagComponent.EMPTY));
    }

    public static ItemStack create(ItemStack first, ItemStack second) {
        ItemStack bag = new ItemStack(ModItems.MONEY_BAG);
        if (first.has(ModComponents.MONEY_BAG) && second.has(ModComponents.MONEY_BAG)) {
            bag.set(ModComponents.MONEY_BAG, MoneyBagComponent.combine(first, second));
        } else if (first.getItem() instanceof CurrencyItem c1 && second.getItem() instanceof CurrencyItem c2) {
            bag.set(ModComponents.MONEY_BAG, MoneyBagComponent.combine(
                    c1.getCombinedValue(first),
                    c2.getCombinedValue(second)
            ));
        }
        return bag;
    }

    public static ItemStack fromValues(long[] values) {
        ItemStack bag = new ItemStack(ModItems.MONEY_BAG);
        bag.set(ModComponents.MONEY_BAG, MoneyBagComponent.of(values));
        return bag;
    }

    public static ItemStack fromRawValue(long value) {
        ItemStack bag = new ItemStack(ModItems.MONEY_BAG);
        bag.set(ModComponents.MONEY_BAG, MoneyBagComponent.of(value));
        return bag;
    }

    @Override
    public long getValue(ItemStack stack) {
        return stack.getOrDefault(ModComponents.MONEY_BAG, MoneyBagComponent.EMPTY).value();
    }

    @Override
    public long[] getCombinedValue(ItemStack stack) {
        MoneyBagComponent comp = stack.getOrDefault(ModComponents.MONEY_BAG, MoneyBagComponent.EMPTY);
        return new long[]{comp.bronze(), comp.silver(), comp.gold()};
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack clickedStack, ItemStack cursorStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (slot instanceof MerchantResultSlot) return false;

        if (action == ClickAction.SECONDARY && clickedStack.is(this) && cursorStack.isEmpty()) {
            long[] coins = getCombinedValue(clickedStack);
            List<ItemStack> stacks = CurrencyConverter.getAsValidStacks(coins);
            if (stacks.isEmpty()) return false;

            ItemStack extracted = stacks.getFirst();
            access.set(extracted);

            long[] remaining = getCombinedValue(clickedStack);
            remaining[((CoinItem) extracted.getItem()).currency.ordinal()] -= extracted.getCount();

            long newValue = CurrencyResolver.combineValues(remaining);
            if (newValue == 0) {
                slot.set(ItemStack.EMPTY);
            } else if (CurrencyResolver.canBeCompacted(remaining) && CurrencyConverter.getAsValidStacks(newValue).size() == 1) {
                slot.set(CurrencyConverter.getAsValidStacks(newValue).getFirst());
            } else {
                slot.set(fromValues(remaining));
            }
            return true;
        }

        if (action == ClickAction.PRIMARY) {
            if (!(cursorStack.getItem() instanceof CurrencyItem)) return false;

            ItemStack merged = MoneyBagItem.create(clickedStack, cursorStack);
            if (getValue(merged) == 0) return false;
            if (!slot.mayPlace(merged)) return false;

            slot.set(merged);
            access.set(ItemStack.EMPTY);
            return true;
        }

        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new CurrencyTooltipData(this.getCombinedValue(stack), new long[]{-1}));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        long value = getValue(stack);
        if (value <= 0) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            PurseHelper.modify(player, value);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.2F);
            if (!player.getAbilities().instabuild) {
                stack.setCount(0);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean wasAdjusted(ItemStack other) {
        return true;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().setStyle(ModItems.SILVER_COIN.nameStyle);
    }
}