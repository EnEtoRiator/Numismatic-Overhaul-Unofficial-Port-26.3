package com.glisco.numismaticoverhaul.client.gui;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.shop.ShopMenu;
import com.glisco.numismaticoverhaul.block.shop.ShopOffer;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.item.ModItems;
import com.glisco.numismaticoverhaul.network.ShopActionC2SPacket;
import com.glisco.numismaticoverhaul.network.UpdateShopScreenS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ShopScreen extends AbstractContainerScreen<ShopMenu> {

    private static final Identifier STORAGE_TEXTURE = NumismaticOverhaul.id("textures/gui/shop_gui.png");
    private static final Identifier TRADES_TEXTURE = NumismaticOverhaul.id("textures/gui/shop_gui_trades.png");

    private static final ItemStack ICON_STORAGE = new ItemStack(Items.CHEST);
    private static final ItemStack ICON_TRADES = new ItemStack(ModItems.GOLD_COIN);

    private List<ShopOffer> offers = List.of();
    private long storedCurrency = 0L;
    private boolean transferEnabled = false;
    private int page = 0;

    private Button storageTabButton;
    private Button tradesTabButton;
    private Button extractButton;
    private Button hopperButton;
    private Button saveOfferButton;
    private Button deleteOfferButton;
    private Button prevPageButton;
    private Button nextPageButton;
    private EditBox priceBox;

    public ShopScreen(ShopMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 5;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 76;

        this.storageTabButton = this.addRenderableWidget(
                Button.builder(Component.empty(), btn -> this.switchTab(true))
                        .bounds(this.leftPos - 26, this.topPos + 10, 24, 24)
                        .build()
        );
        this.tradesTabButton = this.addRenderableWidget(
                Button.builder(Component.empty(), btn -> this.switchTab(false))
                        .bounds(this.leftPos - 26, this.topPos + 37, 24, 24)
                        .build()
        );

        int panelX = this.leftPos + this.imageWidth + 6;
        this.extractButton = this.addRenderableWidget(
                Button.builder(Component.translatable("gui.numismatic_overhaul.shop.extract"), btn ->
                        ClientPlayNetworking.send(new ShopActionC2SPacket(ShopActionC2SPacket.Action.EXTRACT_CURRENCY))
                ).bounds(panelX, this.topPos + 46, 76, 18).build()
        );
        this.hopperButton = this.addRenderableWidget(
                Button.builder(this.hopperLabel(), btn ->
                        ClientPlayNetworking.send(new ShopActionC2SPacket(ShopActionC2SPacket.Action.TOGGLE_TRANSFER))
                ).bounds(panelX, this.topPos + 68, 76, 18).build()
        );

        // Элементы левой рамки сдвинуты выше и левее (x = 11..79, y = 11..66)
        this.priceBox = new EditBox(this.font, this.leftPos + 11, this.topPos + 32, 68, 14, Component.empty());
        this.priceBox.setMaxLength(9);
        this.priceBox.setHint(Component.literal("Цена...").withStyle(ChatFormatting.DARK_GRAY));
        this.priceBox.setResponder(str -> {
            String digits = str.replaceAll("[^0-9]", "");
            if (!digits.equals(str)) {
                this.priceBox.setValue(digits);
            }
        });
        this.addRenderableWidget(this.priceBox);

        this.saveOfferButton = this.addRenderableWidget(
                Button.builder(Component.literal("OK").withStyle(ChatFormatting.GREEN), btn -> {
                    long price = this.parsePrice();
                    ItemStack buffer = this.menu.getBufferStack();
                    if (price > 0 && !buffer.isEmpty()) {
                        ClientPlayNetworking.send(new ShopActionC2SPacket(ShopActionC2SPacket.Action.CREATE_OFFER, price, buffer.copy()));
                    }
                }).bounds(this.leftPos + 11, this.topPos + 49, 45, 15).build()
        );

        this.deleteOfferButton = this.addRenderableWidget(
                Button.builder(Component.literal("X").withStyle(ChatFormatting.RED), btn ->
                        ClientPlayNetworking.send(new ShopActionC2SPacket(ShopActionC2SPacket.Action.DELETE_OFFER, this.menu.getBufferStack().copy()))
                ).bounds(this.leftPos + 58, this.topPos + 49, 21, 15).build()
        );

        // Кнопки страниц для правой рамки (появляются, когда сделок > 12)
        this.prevPageButton = this.addRenderableWidget(
                Button.builder(Component.literal("<"), btn -> {
                    if (this.page > 0) this.page--;
                }).bounds(this.leftPos + 91, this.topPos + 73, 14, 11).build()
        );
        this.nextPageButton = this.addRenderableWidget(
                Button.builder(Component.literal(">"), btn -> {
                    int maxPage = Math.max(0, (this.offers.size() - 1) / 12);
                    if (this.page < maxPage) this.page++;
                }).bounds(this.leftPos + 149, this.topPos + 73, 14, 11).build()
        );

        this.updateWidgetVisibility();
    }

    private void switchTab(boolean storageTab) {
        this.menu.setInventoryTabActive(storageTab);
        this.updateWidgetVisibility();
    }

    private void updateWidgetVisibility() {
        boolean isStorage = this.menu.isInventoryTabActive();
        this.storageTabButton.active = !isStorage;
        this.tradesTabButton.active = isStorage;

        this.extractButton.visible = isStorage;
        this.extractButton.active = isStorage && this.storedCurrency > 0;
        this.hopperButton.visible = isStorage;
        this.hopperButton.setMessage(this.hopperLabel());

        this.priceBox.visible = !isStorage;
        this.saveOfferButton.visible = !isStorage;
        this.saveOfferButton.active = !isStorage && !this.menu.getBufferStack().isEmpty() && this.parsePrice() > 0;
        this.deleteOfferButton.visible = !isStorage;
        this.deleteOfferButton.active = !isStorage && !this.menu.getBufferStack().isEmpty();

        int maxPage = Math.max(0, (this.offers.size() - 1) / 12);
        if (this.page > maxPage) this.page = maxPage;
        boolean showPager = !isStorage && this.offers.size() > 12;
        this.prevPageButton.visible = showPager;
        this.prevPageButton.active = showPager && this.page > 0;
        this.nextPageButton.visible = showPager;
        this.nextPageButton.active = showPager && this.page < maxPage;
    }

    private Component hopperLabel() {
        return Component.literal(this.transferEnabled ? "Hopper: ON" : "Hopper: OFF")
                .withStyle(this.transferEnabled ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private long parsePrice() {
        String text = this.priceBox.getValue();
        if (text.isEmpty()) return 0L;
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private void selectBufferItem(ItemStack stack) {
        ItemStack copy = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        this.menu.setClientBufferStack(copy);
        ClientPlayNetworking.send(new ShopActionC2SPacket(ShopActionC2SPacket.Action.CLICK_BUFFER, copy));
        if (!copy.isEmpty()) {
            for (ShopOffer offer : this.offers) {
                if (ItemStack.isSameItemSameComponents(offer.getSellStack(), copy)) {
                    this.priceBox.setValue(String.valueOf(offer.price()));
                    break;
                }
            }
        }
        this.updateWidgetVisibility();
    }

    public void updateFromServer(UpdateShopScreenS2CPacket packet) {
        this.offers = packet.offers();
        this.storedCurrency = packet.storedCurrency();
        this.transferEnabled = packet.transferEnabled();
        this.menu.setClientBufferStack(packet.bufferStack());

        if (!packet.bufferStack().isEmpty()) {
            for (ShopOffer offer : this.offers) {
                if (ItemStack.isSameItemSameComponents(offer.getSellStack(), packet.bufferStack())) {
                    this.priceBox.setValue(String.valueOf(offer.price()));
                    break;
                }
            }
        }
        this.updateWidgetVisibility();
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.menu.isInventoryTabActive()) {
            graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
        }
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF404040, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.updateWidgetVisibility();
        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.fakeItem(ICON_STORAGE, this.leftPos - 22, this.topPos + 14);
        graphics.fakeItem(ICON_TRADES, this.leftPos - 22, this.topPos + 41);
    }

    private void drawSlotBox(GuiGraphicsExtractor graphics, int x, int y, boolean hovered, boolean selected) {
        graphics.fill(x, y, x + 18, y + 18, selected ? 0xFFFFD700 : 0xFF373737);
        if (!selected) {
            graphics.fill(x + 1, y + 1, x + 18, y + 18, 0xFFFFFFFF);
        }
        graphics.fill(x + 1, y + 1, x + 17, y + 17, hovered ? 0xFFA4A4A4 : 0xFF8B8B8B);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        boolean isStorage = this.menu.isInventoryTabActive();
        Identifier texture = isStorage ? STORAGE_TEXTURE : TRADES_TEXTURE;

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if (isStorage) {
            int panelX = this.leftPos + this.imageWidth + 2;
            int panelY = this.topPos + 2;
            graphics.fill(panelX, panelY, panelX + 84, panelY + 90, 0xDD1A1A1A);
            graphics.outline(panelX, panelY, 84, 90, 0xFF6E5A28);

            graphics.text(this.font, Component.literal("Баланс:"), panelX + 6, panelY + 6, 0xFFE6C15A, false);
            List<ItemStack> coins = CurrencyConverter.getAsItemStackList(this.storedCurrency);
            if (coins.isEmpty()) {
                graphics.text(this.font, Component.literal("0"), panelX + 6, panelY + 22, 0xFFAAAAAA, false);
            } else {
                for (int i = 0; i < coins.size(); i++) {
                    ItemStack coin = coins.get(i);
                    graphics.fakeItem(coin.copyWithCount(1), panelX + 4 + i * 26, panelY + 19);
                    graphics.text(this.font, String.valueOf(coin.getCount()), panelX + 19 + i * 26, panelY + 24, 0xFFFFFFFF, true);
                }
            }
        } else {
            // Левая рамка: слот выбранного товара (11, 11)
            int bufX = this.leftPos + 11;
            int bufY = this.topPos + 11;
            boolean bufHovered = this.isHovering(11, 11, 18, 18, mouseX, mouseY);
            ItemStack bufferStack = this.menu.getBufferStack();
            this.drawSlotBox(graphics, bufX, bufY, bufHovered, !bufferStack.isEmpty());

            if (!bufferStack.isEmpty()) {
                graphics.fakeItem(bufferStack, bufX + 1, bufY + 1);
                graphics.itemDecorations(this.font, bufferStack, bufX + 1, bufY + 1);
            }

            graphics.text(this.font, Component.literal("Цена"), this.leftPos + 32, this.topPos + 12, 0xFF404040, false);
            graphics.text(this.font, Component.literal("(бронза):"), this.leftPos + 32, this.topPos + 21, 0xFF555555, false);

            // Правая рамка: активные сделки (4x3 слота, y = 10..64)
            int startIndex = this.page * 12;
            if (this.offers.isEmpty()) {
                graphics.centeredText(
                        this.font,
                        Component.literal("Нет сделок").withStyle(ChatFormatting.DARK_GRAY),
                        this.leftPos + 127,
                        this.topPos + 34,
                        0xFF555555
                );
            } else {
                for (int idx = 0; idx < 12; idx++) {
                    int offerIndex = startIndex + idx;
                    if (offerIndex >= this.offers.size()) break;

                    int col = idx % 4;
                    int row = idx / 4;
                    int relX = 91 + col * 18;
                    int relY = 10 + row * 18;
                    int slotX = this.leftPos + relX;
                    int slotY = this.topPos + relY;

                    boolean hovered = this.isHovering(relX, relY, 18, 18, mouseX, mouseY);
                    boolean selected = !bufferStack.isEmpty()
                            && ItemStack.isSameItemSameComponents(this.offers.get(offerIndex).getSellStack(), bufferStack);

                    this.drawSlotBox(graphics, slotX, slotY, hovered, selected);

                    ItemStack sellStack = this.offers.get(offerIndex).getSellStack();
                    graphics.fakeItem(sellStack, slotX + 1, slotY + 1);
                    graphics.itemDecorations(this.font, sellStack, slotX + 1, slotY + 1);
                }

                if (this.offers.size() > 12) {
                    int totalPages = (this.offers.size() + 11) / 12;
                    graphics.centeredText(
                            this.font,
                            Component.literal((this.page + 1) + "/" + totalPages),
                            this.leftPos + 127,
                            this.topPos + 75,
                            0xFF404040
                    );
                }
            }

            // Карточка конвертации цены справа от окна
            long previewPrice = this.parsePrice();
            long[] split = CurrencyResolver.splitValues(previewPrice);
            int cardX = this.leftPos + this.imageWidth + 4;
            int cardY = this.topPos + 10;
            graphics.fill(cardX, cardY, cardX + 82, cardY + 68, 0xDD1A1A1A);
            graphics.outline(cardX, cardY, 82, 68, 0xFF6E5A28);
            graphics.text(this.font, Component.literal("Итого цена:"), cardX + 6, cardY + 5, 0xFFE6C15A, false);

            ItemStack[] coinIcons = {
                    new ItemStack(ModItems.GOLD_COIN),
                    new ItemStack(ModItems.SILVER_COIN),
                    new ItemStack(ModItems.BRONZE_COIN)
            };
            long[] counts = { split[2], split[1], split[0] };
            for (int i = 0; i < 3; i++) {
                int rowY = cardY + 16 + i * 16;
                graphics.fakeItem(coinIcons[i], cardX + 6, rowY);
                graphics.text(this.font, String.valueOf(counts[i]), cardX + 25, rowY + 4, 0xFFFFFFFF, true);
            }
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        if (this.isHovering(-26, 10, 24, 24, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(Component.translatable("gui.numismatic_overhaul.shop.inventory_title"), mouseX, mouseY);
            return;
        }
        if (this.isHovering(-26, 37, 24, 24, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(Component.literal("Настройка сделок"), mouseX, mouseY);
            return;
        }

        if (this.menu.isInventoryTabActive()) return;

        if (this.isHovering(11, 11, 18, 18, mouseX, mouseY)) {
            ItemStack buffer = this.menu.getBufferStack();
            if (!buffer.isEmpty()) {
                graphics.setTooltipForNextFrame(this.font, buffer, mouseX, mouseY);
            } else {
                List<Component> hint = List.of(
                        Component.literal("Выбор товара").withStyle(ChatFormatting.GOLD),
                        Component.literal("Shift+ЛКМ / ПКМ по предмету в инвентаре").withStyle(ChatFormatting.GRAY),
                        Component.literal("или кликните сюда предметом с курсора").withStyle(ChatFormatting.GRAY)
                );
                graphics.setComponentTooltipForNextFrame(this.font, hint, mouseX, mouseY);
            }
            return;
        }

        int startIndex = this.page * 12;
        for (int idx = 0; idx < 12; idx++) {
            int offerIndex = startIndex + idx;
            if (offerIndex >= this.offers.size()) break;

            int col = idx % 4;
            int row = idx / 4;
            int relX = 91 + col * 18;
            int relY = 10 + row * 18;
            if (this.isHovering(relX, relY, 18, 18, mouseX, mouseY)) {
                ShopOffer offer = this.offers.get(offerIndex);
                long[] split = CurrencyResolver.splitValues(offer.price());
                List<Component> lines = new ArrayList<>(getTooltipFromItem(this.minecraft, offer.getSellStack()));
                lines.add(Component.literal("Цена: " + split[2] + "з " + split[1] + "с " + split[0] + "б")
                        .withStyle(ChatFormatting.GOLD));
                lines.add(Component.literal("ЛКМ — изменить | Shift+ПКМ — убрать сделку")
                        .withStyle(ChatFormatting.GRAY));
                graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!this.menu.isInventoryTabActive()) {
            // 1. Клик ЛКМ по слоту товара (11, 11) с предметом на курсоре
            if (event.button() == 0 && this.isHovering(11, 11, 18, 18, event.x(), event.y())) {
                this.selectBufferItem(this.menu.getCarried());
                return true;
            }

            // 2. Shift + ЛКМ или ПКМ по любому слоту инвентаря игрока выбирает предмет
            if (event.button() == 1 || (event.button() == 0 && event.hasShiftDown())) {
                for (int i = 27; i < this.menu.slots.size(); i++) {
                    Slot slot = this.menu.slots.get(i);
                    if (slot.hasItem() && this.isHovering(slot.x, slot.y, 16, 16, event.x(), event.y())) {
                        this.selectBufferItem(slot.getItem());
                        return true;
                    }
                }
            }

            // 3. Взаимодействие с карточками сделок в правом окне
            int startIndex = this.page * 12;
            for (int idx = 0; idx < 12; idx++) {
                int offerIndex = startIndex + idx;
                if (offerIndex >= this.offers.size()) break;

                int col = idx % 4;
                int row = idx / 4;
                int relX = 91 + col * 18;
                int relY = 10 + row * 18;
                if (this.isHovering(relX, relY, 18, 18, event.x(), event.y())) {
                    ShopOffer offer = this.offers.get(offerIndex);
                    // Shift + ПКМ удаляет сделку сразу
                    if (event.button() == 1 && event.hasShiftDown()) {
                        ClientPlayNetworking.send(new ShopActionC2SPacket(ShopActionC2SPacket.Action.DELETE_OFFER, offer.getSellStack()));
                        return true;
                    }
                    // Обычный ЛКМ загружает сделку в редактор слева и сразу подсвечивает её
                    if (event.button() == 0) {
                        this.menu.setClientBufferStack(offer.getSellStack());
                        this.priceBox.setValue(String.valueOf(offer.price()));
                        this.updateWidgetVisibility();
                        ClientPlayNetworking.send(new ShopActionC2SPacket(ShopActionC2SPacket.Action.LOAD_OFFER, offerIndex));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}