package com.glisco.numismaticoverhaul.menu;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.pawn.PawnShopMenu;
import com.glisco.numismaticoverhaul.block.piggy.PiggyBankMenu;
import com.glisco.numismaticoverhaul.block.shop.ShopMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {

    public static final MenuType<PiggyBankMenu> PIGGY_BANK = Registry.register(
            BuiltInRegistries.MENU,
            NumismaticOverhaul.id("piggy_bank"),
            new MenuType<>(PiggyBankMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final MenuType<ShopMenu> SHOP = Registry.register(
            BuiltInRegistries.MENU,
            NumismaticOverhaul.id("shop"),
            new MenuType<>(ShopMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final MenuType<PawnShopMenu> PAWN_SHOP = Registry.register(
            BuiltInRegistries.MENU,
            NumismaticOverhaul.id("pawn_shop"),
            new MenuType<>(PawnShopMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static void initialize() {
        NumismaticOverhaul.LOGGER.info("[ModMenus] Меню контейнеров зарегистрированы.");
    }
}