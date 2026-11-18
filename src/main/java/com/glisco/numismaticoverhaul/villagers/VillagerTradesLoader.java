package com.glisco.numismaticoverhaul.villagers;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.config.NumismaticConfig;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jspecify.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class VillagerTradesLoader {

    @FunctionalInterface
    public interface TradeFactory {
        @Nullable MerchantOffer create(ServerLevel level, Entity trader, RandomSource random);
    }

    public static final Identifier WANDERING_TRADER_ID = Identifier.withDefaultNamespace("wandering_trader");

    private static final Map<Identifier, Map<Integer, List<TradeFactory>>> TRADES = new HashMap<>();

    private VillagerTradesLoader() {}

    public static boolean hasTrades(Identifier professionId) {
        return TRADES.containsKey(professionId);
    }

    public static List<TradeFactory> getTrades(Identifier professionId, int level) {
        Map<Integer, List<TradeFactory>> byLevel = TRADES.get(professionId);
        if (byLevel == null) return List.of();
        return byLevel.getOrDefault(level, List.of());
    }

    public static void populateOffers(
            ServerLevel level,
            MerchantOffers offers,
            List<TradeFactory> pool,
            Entity trader,
            RandomSource random,
            int count
    ) {
        if (!NumismaticConfig.get().enableVillagerTrading || pool.isEmpty()) return;

        List<TradeFactory> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, new java.util.Random(random.nextLong()));

        int added = 0;
        for (TradeFactory factory : copy) {
            if (added >= count) break;
            MerchantOffer offer = factory.create(level, trader, random);
            if (offer != null) {
                offers.add(offer);
                added++;
            }
        }
    }

    /**
     * Конвертирует любые оставшиеся изумрудные сделки (например, из других модов или неперекрытых профессий) в монеты.
     */
    public static void convertEmeraldOffers(MerchantOffers offers) {
        NumismaticConfig config = NumismaticConfig.get();
        if (!config.enableVillagerTrading) return;

        long emeraldRate = config.emeraldExchangeRate > 0 ? config.emeraldExchangeRate : 100L;

        for (int i = 0; i < offers.size(); i++) {
            MerchantOffer old = offers.get(i);
            ItemStack costA = old.getBaseCostA();
            ItemStack costB = old.getCostB();
            ItemStack result = old.getResult();

            boolean hasEmerald = isEmerald(costA) || isEmerald(costB) || isEmerald(result);
            if (!hasEmerald) continue;

            ItemCost newCostA = old.getItemCostA();
            Optional<ItemCost> newCostB = old.getItemCostB();
            ItemStack newResult = result.copy();

            long totalPayBronze = 0L;
            ItemStack nonEmeraldItem = ItemStack.EMPTY;

            if (isEmerald(costA)) {
                totalPayBronze += getEmeraldValue(costA, emeraldRate);
            } else if (!costA.isEmpty()) {
                nonEmeraldItem = costA.copy();
            }

            if (isEmerald(costB)) {
                totalPayBronze += getEmeraldValue(costB, emeraldRate);
            } else if (!costB.isEmpty()) {
                nonEmeraldItem = costB.copy();
            }

            if (totalPayBronze > 0) {
                List<ItemStack> coins = toTwoSlotCoins(totalPayBronze);
                if (!nonEmeraldItem.isEmpty()) {
                    newCostA = new ItemCost(nonEmeraldItem.getItem(), nonEmeraldItem.getCount());
                    newCostB = coins.isEmpty()
                            ? Optional.empty()
                            : Optional.of(new ItemCost(coins.get(0).getItem(), coins.get(0).getCount()));
                } else if (!coins.isEmpty()) {
                    newCostA = new ItemCost(coins.get(0).getItem(), coins.get(0).getCount());
                    newCostB = coins.size() > 1
                            ? Optional.of(new ItemCost(coins.get(1).getItem(), coins.get(1).getCount()))
                            : Optional.empty();
                }
            }

            if (isEmerald(result)) {
                long rewardBronze = getEmeraldValue(result, emeraldRate);
                List<ItemStack> coins = CurrencyConverter.getAsItemStackList(rewardBronze);
                newResult = coins.size() == 1 ? coins.get(0) : MoneyBagItem.fromRawValue(rewardBronze);
            }

            offers.set(i, new MerchantOffer(
                    newCostA,
                    newCostB,
                    newResult,
                    old.getUses(),
                    old.getMaxUses(),
                    old.getXp(),
                    old.getPriceMultiplier(),
                    old.getDemand()
            ));
        }
    }

    private static boolean isEmerald(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(Items.EMERALD) || stack.is(Items.EMERALD_BLOCK));
    }

    private static long getEmeraldValue(ItemStack stack, long rate) {
        int multiplier = stack.is(Items.EMERALD_BLOCK) ? 9 : 1;
        return (long) stack.getCount() * multiplier * rate;
    }

    public static void reload(ResourceManager resourceManager, RegistryAccess registryAccess) {
        TRADES.clear();
        Map<Identifier, Resource> resources = resourceManager.listResources(
                "villager_trades",
                id -> id.getNamespace().equals(NumismaticOverhaul.MOD_ID) && id.getPath().endsWith(".json")
        );

        int loadedFiles = 0;
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();
            try (var reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                Identifier professionId;
                if (root.has("profession")) {
                    professionId = Identifier.parse(root.get("profession").getAsString());
                } else if (fileId.getPath().contains("wandering_trader")) {
                    professionId = WANDERING_TRADER_ID;
                } else {
                    String name = fileId.getPath()
                            .replace("villager_trades/", "")
                            .replace(".json", "");
                    professionId = Identifier.withDefaultNamespace(name);
                }

                if (!root.has("trades") || !root.get("trades").isJsonObject()) continue;
                JsonObject tradesObj = root.getAsJsonObject("trades");

                Map<Integer, List<TradeFactory>> levelMap = new HashMap<>();
                for (Map.Entry<String, JsonElement> levelEntry : tradesObj.entrySet()) {
                    int lvl;
                    try {
                        lvl = Integer.parseInt(levelEntry.getKey());
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    List<TradeFactory> factories = new ArrayList<>();
                    JsonArray arr = levelEntry.getValue().getAsJsonArray();
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        TradeFactory parsed = parseFactory(el.getAsJsonObject(), registryAccess);
                        if (parsed != null) {
                            factories.add(parsed);
                        }
                    }
                    if (!factories.isEmpty()) {
                        levelMap.put(lvl, factories);
                    }
                }

                if (!levelMap.isEmpty()) {
                    TRADES.put(professionId, levelMap);
                    loadedFiles++;
                }
            } catch (Exception e) {
                NumismaticOverhaul.LOGGER.error("Ошибка загрузки файла торгов {}: {}", fileId, e.getMessage());
            }
        }

        NumismaticOverhaul.LOGGER.info("Загружено {} наборов торгов жителей для Numismatic Overhaul.", loadedFiles);
    }

    private static @Nullable TradeFactory parseFactory(JsonObject obj, RegistryAccess registryAccess) {
        String rawType = obj.has("type") ? obj.get("type").getAsString() : "";
        String type = rawType.contains(":") ? rawType.substring(rawType.indexOf(':') + 1) : rawType;

        int maxUses = obj.has("max_uses") ? obj.get("max_uses").getAsInt() : 12;
        int xp = obj.has("experience") ? obj.get("experience").getAsInt() : 2;
        float multiplier = obj.has("price_multiplier") ? obj.get("price_multiplier").getAsFloat() : 0.05F;

        return switch (type) {
            case "buy_stack" -> {
                ItemStack buy = parseStack(obj.get("buy"));
                long price = obj.get("price").getAsLong();
                if (buy.isEmpty() || price <= 0) yield null;
                yield (lvl, trader, rnd) -> {
                    List<ItemStack> coins = CurrencyConverter.getAsItemStackList(price);
                    ItemStack reward = coins.size() == 1 ? coins.get(0) : MoneyBagItem.fromRawValue(price);
                    return new MerchantOffer(
                            new ItemCost(buy.getItem(), buy.getCount()),
                            Optional.empty(),
                            reward,
                            maxUses, xp, multiplier
                    );
                };
            }
            case "sell_stack" -> {
                ItemStack sell = parseStack(obj.get("sell"));
                long price = obj.get("price").getAsLong();
                if (sell.isEmpty() || price <= 0) yield null;
                yield (lvl, trader, rnd) -> createCoinPaymentOffer(price, Optional.empty(), sell.copy(), maxUses, xp, multiplier);
            }
            case "process_item" -> {
                ItemStack buy = parseStack(obj.get("buy"));
                ItemStack sell = parseStack(obj.get("sell"));
                long price = obj.get("price").getAsLong();
                if (buy.isEmpty() || sell.isEmpty() || price <= 0) yield null;
                yield (lvl, trader, rnd) -> {
                    List<ItemStack> coins = toTwoSlotCoins(price);
                    if (coins.isEmpty()) return null;
                    return new MerchantOffer(
                            new ItemCost(coins.get(0).getItem(), coins.get(0).getCount()),
                            Optional.of(new ItemCost(buy.getItem(), buy.getCount())),
                            sell.copy(),
                            maxUses, xp, multiplier
                    );
                };
            }
            case "enchant_item" -> {
                ItemStack baseItem = parseStack(obj.has("item") ? obj.get("item") : obj.get("sell"));
                long basePrice = obj.has("base_price") ? obj.get("base_price").getAsLong() : obj.get("price").getAsLong();
                if (baseItem.isEmpty()) yield null;
                yield (lvl, trader, rnd) -> {
                    int enchLevel = 5 + rnd.nextInt(15);
                    ItemStack enchanted = EnchantmentHelper.enchantItem(
                            rnd,
                            baseItem.copy(),
                            enchLevel,
                            registryAccess,
                            Optional.empty()
                    );
                    long finalPrice = Math.max(100L, basePrice + (long) enchLevel * 50L);
                    return createCoinPaymentOffer(finalPrice, Optional.empty(), enchanted, maxUses, xp, multiplier);
                };
            }
            case "sell_single_enchantment" -> {
                long basePrice = obj.has("base_price") ? obj.get("base_price").getAsLong() : 800L;
                yield (lvl, trader, rnd) -> {
                    var enchRegistry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
                    Optional<HolderSet.Named<Enchantment>> tradeable = enchRegistry.get(EnchantmentTags.TRADEABLE);
                    List<Holder<Enchantment>> candidates = tradeable
                            .map(set -> set.stream().toList())
                            .orElseGet(() -> enchRegistry.listElements().map(ref -> (Holder<Enchantment>) ref).toList());
                    if (candidates.isEmpty()) return null;

                    Holder<Enchantment> chosen = candidates.get(rnd.nextInt(candidates.size()));
                    int enchLvl = Mth.nextInt(rnd, chosen.value().getMinLevel(), chosen.value().getMaxLevel());
                    ItemStack book = EnchantmentHelper.createBook(new EnchantmentInstance(chosen, enchLvl));
                    long price = basePrice * enchLvl + rnd.nextInt(300) * 10L;
                    if (chosen.is(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
                        price *= 2;
                    }
                    return createCoinPaymentOffer(price, Optional.empty(), book, maxUses, xp, multiplier);
                };
            }
            case "sell_dyed_armor" -> {
                ItemStack baseArmor = parseStack(obj.has("item") ? obj.get("item") : obj.get("sell"));
                long price = obj.get("price").getAsLong();
                if (baseArmor.isEmpty() || price <= 0) yield null;
                yield (lvl, trader, rnd) -> {
                    ItemStack armor = baseArmor.copy();
                    DyeColor[] colors = DyeColor.values();
                    int rgb = colors[rnd.nextInt(colors.length)].getTextureDiffuseColor();

                    // С шансом 30% смешиваем со вторым случайным цветом
                    if (rnd.nextFloat() > 0.7F) {
                        int rgb2 = colors[rnd.nextInt(colors.length)].getTextureDiffuseColor();
                        int r = (((rgb >> 16) & 0xFF) + ((rgb2 >> 16) & 0xFF)) / 2;
                        int g = (((rgb >> 8) & 0xFF) + ((rgb2 >> 8) & 0xFF)) / 2;
                        int b = ((rgb & 0xFF) + (rgb2 & 0xFF)) / 2;
                        rgb = (r << 16) | (g << 8) | b;
                    }

                    armor.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb));
                    return createCoinPaymentOffer(price, Optional.empty(), armor, maxUses, xp, multiplier);
                };
            }
            case "sell_tag" -> {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(obj.get("tag").getAsString()));
                int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
                long price = obj.get("price").getAsLong();
                yield (lvl, trader, rnd) -> {
                    Optional<HolderSet.Named<Item>> set = BuiltInRegistries.ITEM.get(tag);
                    if (set.isEmpty() || set.get().size() == 0) return null;
                    Item item = set.get().get(rnd.nextInt(set.get().size())).value();
                    return createCoinPaymentOffer(price, Optional.empty(), new ItemStack(item, count), maxUses, xp, multiplier);
                };
            }
            case "buy_tag" -> {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(obj.get("tag").getAsString()));
                int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
                long price = obj.get("price").getAsLong();
                yield (lvl, trader, rnd) -> {
                    Optional<HolderSet.Named<Item>> set = BuiltInRegistries.ITEM.get(tag);
                    if (set.isEmpty() || set.get().size() == 0) return null;
                    Item item = set.get().get(rnd.nextInt(set.get().size())).value();
                    List<ItemStack> coins = CurrencyConverter.getAsItemStackList(price);
                    ItemStack reward = coins.size() == 1 ? coins.get(0) : MoneyBagItem.fromRawValue(price);
                    return new MerchantOffer(new ItemCost(item, count), Optional.empty(), reward, maxUses, xp, multiplier);
                };
            }
            default -> null;
        };
    }

    private static ItemStack parseStack(JsonElement element) {
        if (element == null) return ItemStack.EMPTY;
        if (element.isJsonPrimitive()) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(element.getAsString()));
            return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        }
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            String idStr = obj.has("id") ? obj.get("id").getAsString() : obj.get("item").getAsString();
            int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(idStr));
            return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, count);
        }
        return ItemStack.EMPTY;
    }

    private static @Nullable MerchantOffer createCoinPaymentOffer(
            long price,
            Optional<ItemCost> extraCost,
            ItemStack result,
            int maxUses,
            int xp,
            float multiplier
    ) {
        List<ItemStack> coins = toTwoSlotCoins(price);
        if (coins.isEmpty()) return null;

        ItemCost costA = new ItemCost(coins.get(0).getItem(), coins.get(0).getCount());
        Optional<ItemCost> costB = extraCost.isPresent()
                ? extraCost
                : (coins.size() > 1 ? Optional.of(new ItemCost(coins.get(1).getItem(), coins.get(1).getCount())) : Optional.empty());

        return new MerchantOffer(costA, costB, result, maxUses, xp, multiplier);
    }

    private static List<ItemStack> toTwoSlotCoins(long bronzeAmount) {
        List<ItemStack> list = CurrencyConverter.getAsItemStackList(bronzeAmount);
        if (list.size() <= 2) return list;
        long rounded = bronzeAmount - (bronzeAmount % 100L);
        return CurrencyConverter.getAsItemStackList(rounded);
    }
}