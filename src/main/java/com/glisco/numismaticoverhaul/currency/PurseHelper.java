package com.glisco.numismaticoverhaul.currency;

import com.glisco.numismaticoverhaul.attachment.ModAttachments;
import com.glisco.numismaticoverhaul.config.NumismaticConfig;
import com.glisco.numismaticoverhaul.item.CoinItem;
import com.glisco.numismaticoverhaul.network.SyncPurseS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class PurseHelper {

    private PurseHelper() {}

    public static long getBalance(Player player) {
        return player.getAttachedOrCreate(ModAttachments.PURSE_BALANCE);
    }

    public static void setBalance(Player player, long newValue) {
        long clamped = Math.max(0L, newValue);
        player.setAttached(ModAttachments.PURSE_BALANCE, clamped);
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new SyncPurseS2CPacket(clamped));
        }
    }

    public static void syncToClient(ServerPlayer player) {
        ServerPlayNetworking.send(player, new SyncPurseS2CPacket(getBalance(player)));
    }

    public static void silentModify(Player player, long delta) {
        setBalance(player, getBalance(player) + delta);
    }

    public static void modify(Player player, long delta) {
        setBalance(player, getBalance(player) + delta);

        if (player.level().isClientSide() || delta == 0) return;

        long absValue = Math.abs(delta);
        List<ItemStack> stacks = CurrencyConverter.getAsItemStackList(absValue);
        if (stacks.isEmpty()) return;

        var location = NumismaticConfig.get().moneyMessageLocation;
        if (location == NumismaticConfig.MoneyMessageLocation.DISABLED) return;

        MutableComponent message = location == NumismaticConfig.MoneyMessageLocation.CHAT
                ? Component.literal("numismatic ").withColor(Currency.GOLD.getNameColor())
                .append(Component.literal("> ").withStyle(ChatFormatting.GRAY))
                : Component.empty();

        message.append(delta < 0
                ? Component.literal("- ").withStyle(ChatFormatting.RED)
                : Component.literal("+ ").withStyle(ChatFormatting.GREEN));
        message.append(Component.literal("[").withStyle(ChatFormatting.GRAY));

        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            Currency currency = ((CoinItem) stack.getItem()).currency;

            message.append(Component.literal(stack.getCount() + " ").withStyle(ChatFormatting.AQUA));
            message.append(Component.translatable("currency.numismatic_overhaul." + currency.name().toLowerCase())
                    .withColor(currency.getNameColor()));

            if (i < stacks.size() - 1) {
                message.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            }
        }
        message.append(Component.literal("]").withStyle(ChatFormatting.GRAY));

        if (location == NumismaticConfig.MoneyMessageLocation.ACTIONBAR) {
            player.sendOverlayMessage(message);
        } else {
            player.sendSystemMessage(message);
        }
    }
}