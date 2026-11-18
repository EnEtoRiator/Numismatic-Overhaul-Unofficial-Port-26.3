package com.glisco.numismaticoverhaul.client.gui;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.attachment.ModAttachments;
import com.glisco.numismaticoverhaul.currency.Currency;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.item.ModItems;
import com.glisco.numismaticoverhaul.network.RequestPurseActionC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class PurseOverlay {

    private static final Identifier TEXTURE = NumismaticOverhaul.id("textures/gui/purse_widget.png");
    private static final int TEX_WIDTH = 128;
    private static final int TEX_HEIGHT = 64;

    private static final int BTN_U = 62;
    private static final int BTN_V_NORMAL = 0;
    private static final int BTN_V_ACTIVE = 13;
    private static final int BTN_WIDTH = 11;
    private static final int BTN_HEIGHT = 13;

    private static boolean expanded = false;
    private static boolean lastShiftDown = false;

    private PurseOverlay() {}

    public static void initialize() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!isSupportedScreen(screen)) return;

            ScreenMouseEvents.allowMouseClick(screen).register((scr, event) -> {
                lastShiftDown = event.hasShiftDown();
                return true;
            });

            // 1. Кнопка открытия/закрытия кошелька
            Button toggleButton = Button.builder(Component.empty(), btn -> expanded = !expanded)
                    .bounds(getButtonX(screen), getButtonY(screen), getHitboxWidth(screen), getHitboxHeight(screen))
                    .build();
            toggleButton.setAlpha(0.0F);

            // 2. Кнопки снятия монет по рядам (Золото, Серебро, Бронза)
            Currency[] order = { Currency.GOLD, Currency.SILVER, Currency.BRONZE };
            Button[] coinButtons = new Button[3];
            for (int row = 0; row < 3; row++) {
                final Currency currency = order[row];
                Button coinBtn = Button.builder(Component.empty(), btn -> {
                    int amount = lastShiftDown ? 64 : 1;
                    ClientPlayNetworking.send(RequestPurseActionC2SPacket.extract(currency.getRawValue(amount)));
                }).bounds(0, 0, 74, 16).build();
                coinBtn.setAlpha(0.0F);
                coinBtn.visible = expanded;
                coinButtons[row] = coinBtn;
            }

            // 3. Кнопка "+ Сложить" внутри карточки
            Button storeAllButton = Button.builder(Component.empty(), btn ->
                    ClientPlayNetworking.send(RequestPurseActionC2SPacket.storeAll())
            ).bounds(0, 0, 72, 13).build();
            storeAllButton.setAlpha(0.0F);
            storeAllButton.visible = expanded;

            List<AbstractWidget> widgets = Screens.getWidgets(screen);
            widgets.add(toggleButton);
            for (Button coinBtn : coinButtons) {
                widgets.add(coinBtn);
            }
            widgets.add(storeAllButton);

            ScreenEvents.afterExtract(screen).register((scr, graphics, mouseX, mouseY, tickDelta) ->
                    renderPurse(client, scr, graphics, mouseX, mouseY, toggleButton, coinButtons, storeAllButton)
            );
        });
    }

    private static boolean isSupportedScreen(Screen screen) {
        return screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
    }

    private static int getGuiWidth(Screen screen) {
        return screen instanceof CreativeModeInventoryScreen ? 195 : 176;
    }

    private static int getGuiHeight(Screen screen) {
        return screen instanceof CreativeModeInventoryScreen ? 136 : 166;
    }

    private static int getGuiLeft(Screen screen) {
        int guiWidth = getGuiWidth(screen);
        int defaultLeft = (screen.width - guiWidth) / 2;
        if (screen instanceof InventoryScreen) {
            for (GuiEventListener child : screen.children()) {
                if (child instanceof AbstractWidget widget && widget.getWidth() == 20 && widget.getHeight() == 18) {
                    return widget.getX() - 104;
                }
            }
        }
        return defaultLeft;
    }

    private static int getGuiTop(Screen screen) {
        return (screen.height - getGuiHeight(screen)) / 2;
    }

    private static int getButtonX(Screen screen) {
        if (screen instanceof CreativeModeInventoryScreen) {
            // Сразу за правым краем окна (не перекрывает скроллбар и вкладки)
            return getGuiLeft(screen) + 198;
        }
        return getGuiLeft(screen) + 158;
    }

    private static int getButtonY(Screen screen) {
        if (screen instanceof CreativeModeInventoryScreen) {
            return getGuiTop(screen) + 6;
        }
        return getGuiTop(screen) + 7;
    }

    private static int getHitboxWidth(Screen screen) {
        return screen instanceof CreativeModeInventoryScreen ? 17 : BTN_WIDTH;
    }

    private static int getHitboxHeight(Screen screen) {
        return screen instanceof CreativeModeInventoryScreen ? 17 : BTN_HEIGHT;
    }

    private static void renderPurse(
            Minecraft client,
            Screen screen,
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            Button toggleButton,
            Button[] coinButtons,
            Button storeAllButton
    ) {
        if (client.player == null) return;

        boolean isCreative = screen instanceof CreativeModeInventoryScreen;
        int btnX = getButtonX(screen);
        int btnY = getButtonY(screen);
        toggleButton.setX(btnX);
        toggleButton.setY(btnY);
        toggleButton.setWidth(getHitboxWidth(screen));
        toggleButton.setHeight(getHitboxHeight(screen));

        boolean hoveredBtn = toggleButton.isHovered();
        int btnV = (hoveredBtn || expanded) ? BTN_V_ACTIVE : BTN_V_NORMAL;

        if (isCreative) {
            // Аккуратная подложка-кнопка справа от окна креатива
            graphics.fill(btnX, btnY, btnX + 17, btnY + 17, (hoveredBtn || expanded) ? 0xEE2A2416 : 0xDD1A1A1A);
            graphics.outline(btnX, btnY, 17, 17, (hoveredBtn || expanded) ? 0xFFE6C15A : 0xFF6E5A28);
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    btnX + 3, btnY + 2,
                    BTN_U, btnV,
                    BTN_WIDTH, BTN_HEIGHT,
                    TEX_WIDTH, TEX_HEIGHT
            );
        } else {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    btnX, btnY,
                    BTN_U, btnV,
                    BTN_WIDTH, BTN_HEIGHT,
                    TEX_WIDTH, TEX_HEIGHT
            );
        }

        // В креативе карточка открывается сразу под кнопкой кошелька, в выживании — справа от инвентаря
        int cardX = isCreative ? btnX : (getGuiLeft(screen) + getGuiWidth(screen) + 4);
        int cardY = isCreative ? (btnY + 20) : (getGuiTop(screen) + 6);

        for (int row = 0; row < 3; row++) {
            coinButtons[row].visible = expanded;
            coinButtons[row].setX(cardX + 4);
            coinButtons[row].setY(cardY + 16 + row * 16);
        }
        storeAllButton.visible = expanded;
        storeAllButton.setX(cardX + 5);
        storeAllButton.setY(cardY + 70);

        if (!expanded) {
            if (hoveredBtn) {
                graphics.setTooltipForNextFrame(
                        Component.translatable("gui.numismatic_overhaul.purse_title"),
                        mouseX, mouseY
                );
            }
            return;
        }

        graphics.fill(cardX, cardY, cardX + 82, cardY + 88, 0xDD1A1A1A);
        graphics.outline(cardX, cardY, 82, 88, 0xFF6E5A28);
        graphics.text(client.font, Component.literal("Кошелёк:"), cardX + 6, cardY + 5, 0xFFE6C15A, false);

        long balance = client.player.getAttachedOrElse(ModAttachments.PURSE_BALANCE, 0L);
        long[] values = CurrencyResolver.splitValues(balance);

        ItemStack[] coinIcons = {
                new ItemStack(ModItems.GOLD_COIN),
                new ItemStack(ModItems.SILVER_COIN),
                new ItemStack(ModItems.BRONZE_COIN)
        };
        long[] displayCounts = { values[2], values[1], values[0] };

        for (int row = 0; row < 3; row++) {
            int rowY = cardY + 16 + row * 16;
            if (coinButtons[row].isHovered()) {
                graphics.fill(cardX + 4, rowY, cardX + 78, rowY + 16, 0x28FFD700);
                graphics.setTooltipForNextFrame(
                        Component.translatable("gui.numismatic_overhaul.purse.withdraw_hint"),
                        mouseX, mouseY
                );
            }
            graphics.fakeItem(coinIcons[row], cardX + 6, rowY);
            graphics.text(client.font, String.valueOf(displayCounts[row]), cardX + 25, rowY + 4, 0xFFFFFFFF, true);
        }

        graphics.fill(cardX + 5, cardY + 66, cardX + 77, cardY + 67, 0xFF4A3D1B);

        int depositY = cardY + 70;
        boolean depositHovered = storeAllButton.isHovered();
        graphics.fill(cardX + 5, depositY, cardX + 77, depositY + 13, depositHovered ? 0xFF3E6B4C : 0xFF264230);
        graphics.outline(cardX + 5, depositY, 72, 13, depositHovered ? 0xFF7EC896 : 0xFF4B7A5A);
        graphics.centeredText(client.font, Component.literal("+ Сложить"), cardX + 41, depositY + 3, 0xFFFFFFFF);
    }
}