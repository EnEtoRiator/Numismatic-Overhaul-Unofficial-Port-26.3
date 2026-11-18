package com.glisco.numismaticoverhaul.event;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.config.ModGameRules;
import com.glisco.numismaticoverhaul.config.NumismaticConfig;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.currency.PurseHelper;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ModEvents {

    public static final Map<EntityType<?>, Integer> MOBS_IN_BOURGEOISIE = new HashMap<>();

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> reloadMobDropConfig());
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> reloadMobDropConfig());

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity.level() instanceof ServerLevel serverLevel)) return;

            // 1. Потеря процента кошелька при смерти игрока
            if (entity instanceof ServerPlayer player) {
                if (!serverLevel.getGameRules().get(GameRules.KEEP_INVENTORY)) {
                    float dropPercent = serverLevel.getGameRules().get(ModGameRules.MONEY_DROP_PERCENTAGE) * 0.01F;
                    long currentBalance = PurseHelper.getBalance(player);
                    long dropped = (long) (currentBalance * dropPercent);

                    if (dropped > 0) {
                        List<ItemStack> stacks = CurrencyConverter.getAsValidStacks(dropped);
                        for (ItemStack stack : stacks) {
                            for (int i = 0; i < stack.getCount(); i++) {
                                ItemStack single = stack.copyWithCount(1);
                                ItemEntity itemEntity = new ItemEntity(
                                        serverLevel, player.getX(), player.getY() + 0.5, player.getZ(), single
                                );
                                itemEntity.setPickUpDelay(40);
                                serverLevel.addFreshEntity(itemEntity);
                            }
                        }
                        PurseHelper.modify(player, -dropped);
                    }
                }
                return;
            }

            // 2. Дроп монет с мобов при убийстве игроком
            boolean killedByPlayer = (damageSource.getEntity() instanceof Player)
                    || (entity.getLastHurtByMob() instanceof Player);
            if (!killedByPlayer) return;

            Integer baseValue = MOBS_IN_BOURGEOISIE.get(entity.getType());
            if (baseValue == null || baseValue <= 0) return;

            var config = NumismaticConfig.get();
            float variance = serverLevel.getGameRules().get(ModGameRules.MONEY_MOB_DROP_VARIANCE) * 0.01F;
            float multiplier = variance > 0.02F
                    ? Mth.nextFloat(entity.getRandom(), 1.0F - variance, 1.0F + variance)
                    : 1.0F;

            if (config.scaleOnHealth) {
                multiplier *= (entity.getMaxHealth() / (20.0F * config.healthScaleReduction));
            }

            long finalValue = Math.max(0L, (long) (baseValue * multiplier));
            if (finalValue > 0) {
                for (ItemStack stack : CurrencyHelper.getAsStacks(finalValue, 4)) {
                    serverLevel.addFreshEntity(new ItemEntity(
                            serverLevel, entity.getX(), entity.getY() + 0.25, entity.getZ(), stack
                    ));
                }
            }
        });

        NumismaticOverhaul.LOGGER.info("[ModEvents] Обработчики смерти и дропа зарегистрированы.");
    }

    public static void reloadMobDropConfig() {
        MOBS_IN_BOURGEOISIE.clear();
        NumismaticConfig.load().mobsToBaseValues.forEach((key, baseValue) -> {
            if (key.startsWith(" ")) {
                String groupName = key.substring(1).trim().toUpperCase(Locale.ROOT);
                try {
                    MobCategory category = MobCategory.valueOf(groupName);
                    for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                        if (type.getCategory() == category) {
                            MOBS_IN_BOURGEOISIE.put(type, baseValue);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    NumismaticOverhaul.LOGGER.error("Неизвестная категория мобов '{}' в конфиге дропа", key);
                }
            } else if (key.startsWith("@")) {
                String namespace = key.substring(1);
                BuiltInRegistries.ENTITY_TYPE.entrySet().forEach(entry -> {
                    if (entry.getKey().identifier().getNamespace().equals(namespace)) {
                        MOBS_IN_BOURGEOISIE.put(entry.getValue(), baseValue);
                    }
                });
            } else if (key.startsWith("#")) {
                Identifier tagId = Identifier.tryParse(key.substring(1));
                if (tagId != null) {
                    TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, tagId);
                    BuiltInRegistries.ENTITY_TYPE.get(tag).ifPresentOrElse(
                            holders -> holders.forEach(holder -> MOBS_IN_BOURGEOISIE.put(holder.value(), baseValue)),
                            () -> NumismaticOverhaul.LOGGER.error("Не найден тег мобов '{}' из конфига дропа", key)
                    );
                }
            } else {
                Identifier entityId = Identifier.tryParse(key);
                if (entityId != null && BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                    MOBS_IN_BOURGEOISIE.put(BuiltInRegistries.ENTITY_TYPE.getValue(entityId), baseValue);
                } else {
                    NumismaticOverhaul.LOGGER.error("Не найден тип сущности '{}' из конфига дропа", key);
                }
            }
        });
    }
}