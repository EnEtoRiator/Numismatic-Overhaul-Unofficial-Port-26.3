package com.glisco.numismaticoverhaul.config;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public final class ModGameRules {

    public static final GameRule<Integer> MONEY_DROP_PERCENTAGE = GameRuleBuilder
            .forInteger(10)
            .range(0, 100)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(NumismaticOverhaul.id("money_drop_percentage"));

    public static final GameRule<Integer> MONEY_MOB_DROP_VARIANCE = GameRuleBuilder
            .forInteger(50)
            .range(0, 100)
            .category(GameRuleCategory.MOBS)
            .buildAndRegister(NumismaticOverhaul.id("money_mob_drop_variance_percentage"));

    public static void initialize() {
        NumismaticOverhaul.LOGGER.info("[ModGameRules] Игровые правила зарегистрированы.");
    }
}