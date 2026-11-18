package com.glisco.numismaticoverhaul.currency;

import com.glisco.numismaticoverhaul.component.MoneyBagComponent;
import com.glisco.numismaticoverhaul.item.CurrencyItem;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.trading.ItemCost;

import java.util.ArrayList;
import java.util.List;

public final class CurrencyHelper {

    private CurrencyHelper() {}

    public static long getMoneyInInventory(Player player, boolean remove) {
        long value = 0L;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!(stack.getItem() instanceof CurrencyItem currencyItem)) continue;

            value += currencyItem.getValue(stack);
            if (remove) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        return value;
    }

    public static long[] getFromContainer(ItemContainerContents contents) {
        long total = 0L;
        for (ItemStackTemplate template : contents.nonEmptyItems()) {
            ItemStack stack = template.create();
            if (stack.getItem() instanceof CurrencyItem currencyItem) {
                total += currencyItem.getValue(stack);
            }
        }
        MoneyBagComponent comp = MoneyBagComponent.of(total);
        return new long[]{comp.bronze(), comp.silver(), comp.gold()};
    }

    public static long getValue(List<ItemStack> stacks) {
        long sum = 0L;
        for (ItemStack stack : stacks) {
            if (stack != null && stack.getItem() instanceof CurrencyItem currencyItem) {
                sum += currencyItem.getValue(stack);
            }
        }
        return sum;
    }

    public static void offerAsCoins(Player player, long value) {
        for (ItemStack stack : CurrencyConverter.getAsValidStacks(value)) {
            if (!player.getInventory().add(stack) && !stack.isEmpty() && !player.level().isClientSide()) {
                ItemEntity drop = new ItemEntity(
                        player.level(),
                        player.getX(),
                        player.getEyeY() - 0.3,
                        player.getZ(),
                        stack
                );
                drop.setPickUpDelay(40);
                player.level().addFreshEntity(drop);
            }
        }
    }

    public static boolean deductFromInventory(Player player, long value) {
        long present = getMoneyInInventory(player, false);
        if (present < value) return false;
        getMoneyInInventory(player, true);
        offerAsCoins(player, present - value);
        return true;
    }

    public static List<ItemStack> getAsStacks(long value, int maxStacks) {
        List<ItemStack> stacks = new ArrayList<>();
        List<ItemStack> rawStacks = CurrencyConverter.getAsValidStacks(value);
        if (rawStacks.size() <= maxStacks) {
            stacks.addAll(rawStacks);
        } else {
            stacks.add(MoneyBagItem.fromRawValue(value));
        }
        return stacks;
    }

    public static ItemStack getClosest(long value) {
        long[] values = CurrencyResolver.splitValues(value);
        for (int i = 0; i < 2; i++) {
            if (values[i + 1] == 0) break;
            values[i + 1] += Math.round(values[i] / 100f);
            values[i] = 0;
        }
        List<ItemStack> result = CurrencyConverter.getAsItemStackList(CurrencyResolver.combineValues(values));
        return result.isEmpty() ? ItemStack.EMPTY : result.getFirst();
    }

    public static ItemCost getClosestTradeItem(long price) {
        ItemStack closest = getClosest(price);
        return new ItemCost(closest.getItem(), closest.getCount());
    }
}