package com.glisco.numismaticoverhaul.command;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.Currency;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.currency.PurseHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.commands.Commands.*;

public final class NumismaticCommand {

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(NumismaticCommand::register);
        NumismaticOverhaul.LOGGER.info("[NumismaticCommand] Команда /numismatic зарегистрирована.");
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection environment) {
        var root = literal("numismatic")
                .then(literal("balance")
                        .executes(NumismaticCommand::getSelf)
                        .then(argument("player", EntityArgument.players())
                                .requires(src -> src.checkPermission(Identifier.fromNamespaceAndPath("numismatic_overhaul", "command.numismatic_overhaul.balance"), PermissionLevel.GAMEMASTERS))
                                .then(literal("get").executes(NumismaticCommand::get))
                                .then(longSubcommand("set", "value", NumismaticCommand::set))
                                .then(longSubcommand("add", "amount", modify(1L)))
                                .then(longSubcommand("subtract", "amount", modify(-1L)))
                        )
                )
                .then(literal("serverworth")
                        .executes(NumismaticCommand::serverWorth)
                );

        dispatcher.register(root);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> longSubcommand(String name, String argName, Command<CommandSourceStack> command) {
        return literal(name).then(argument(argName, LongArgumentType.longArg(0L)).executes(command));
    }

    private static int getSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        long coins = PurseHelper.getBalance(player);
        long[] values = CurrencyResolver.splitValues(coins);
        ctx.getSource().sendSuccess(() -> Component.translatable("chat.numismatic_overhaul.balance", values[0], values[1], values[2]), false);
        return CurrencyConverter.asInt(coins);
    }

    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var players = EntityArgument.getPlayers(ctx, "player");
        long total = 0L;
        for (ServerPlayer player : players) {
            long balance = PurseHelper.getBalance(player);
            total += balance;
            long[] values = CurrencyResolver.splitValues(balance);
            ctx.getSource().sendSuccess(() -> Component.translatable(
                    "chat.numismatic_overhaul.balance.player",
                    player.getName().getString(), values[0], values[1], values[2]
            ), false);
        }
        return CurrencyConverter.asInt(total);
    }

    private static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        long value = LongArgumentType.getLong(ctx, "value");
        var players = EntityArgument.getPlayers(ctx, "player");
        for (ServerPlayer player : players) {
            PurseHelper.setBalance(player, value);
            ctx.getSource().sendSuccess(() -> prefix()
                    .append(Component.literal("balance of " + player.getName().getString() + " set to: " + value)
                            .withStyle(ChatFormatting.GRAY)), false);
        }
        return CurrencyConverter.asInt(value);
    }

    private static Command<CommandSourceStack> modify(long multiplier) {
        return ctx -> {
            long amount = LongArgumentType.getLong(ctx, "amount");
            var players = EntityArgument.getPlayers(ctx, "player");
            long lastValue = 0L;
            for (ServerPlayer player : players) {
                PurseHelper.silentModify(player, amount * multiplier);
                lastValue = PurseHelper.getBalance(player);
                long display = lastValue;
                ctx.getSource().sendSuccess(() -> prefix()
                        .append(Component.literal("balance of " + player.getName().getString() + " set to: " + display)
                                .withStyle(ChatFormatting.GRAY)), false);
            }
            return CurrencyConverter.asInt(lastValue);
        };
    }

    private static int serverWorth(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        Set<UUID> onlineUUIDs = new HashSet<>();
        long worth = 0L;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            onlineUUIDs.add(player.getUUID());
            worth += PurseHelper.getBalance(player);
        }

        // Читаем оффлайн-балансы из playerdata/*.dat
        Path playerDataDir = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        if (Files.isDirectory(playerDataDir)) {
            try (var stream = Files.list(playerDataDir)) {
                for (Path file : stream.filter(p -> p.toString().endsWith(".dat")).toList()) {
                    String fileName = file.getFileName().toString();
                    String uuidStr = fileName.substring(0, fileName.length() - 4);
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        if (onlineUUIDs.contains(uuid)) continue;

                        CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
                        CompoundTag attachments = root.getCompoundOrEmpty("fabric:attachments");
                        worth += attachments.getLongOr("numismatic_overhaul:currency", 0L);
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                NumismaticOverhaul.LOGGER.warn("Ошибка при подсчете оффлайн-балансов в serverworth", e);
            }
        }

        long finalWorth = worth;
        ctx.getSource().sendSuccess(() -> prefix()
                .append(Component.literal("server net worth: " + finalWorth).withStyle(ChatFormatting.GRAY)), false);
        return CurrencyConverter.asInt(finalWorth);
    }

    private static net.minecraft.network.chat.MutableComponent prefix() {
        return Component.literal("numismatic ").withColor(Currency.GOLD.getNameColor())
                .append(Component.literal("> ").withStyle(ChatFormatting.GRAY));
    }
}