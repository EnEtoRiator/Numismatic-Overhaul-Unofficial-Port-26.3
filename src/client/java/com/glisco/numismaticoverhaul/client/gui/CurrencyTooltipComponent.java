package com.glisco.numismaticoverhaul.client.gui;

import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.item.CurrencyTooltipData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CurrencyTooltipComponent implements ClientTooltipComponent {

    private static final Identifier STRIKETHROUGH_SPRITE =
            Identifier.withDefaultNamespace("container/villager/discount_strikethrough");

    private final CurrencyTooltipData data;
    private final List<Component> textLines = new ArrayList<>();
    private int widthCache = -1;

    public CurrencyTooltipComponent(CurrencyTooltipData data) {
        this.data = data;
        if (data.original().length > 0 && data.original()[0] != -1) {
            for (ItemStack stack : CurrencyConverter.getAsItemStackList(data.original())) {
                this.textLines.add(Component.literal(String.valueOf(stack.getCount())).withStyle(ChatFormatting.GRAY));
            }
        }
        for (ItemStack stack : CurrencyConverter.getAsItemStackList(data.value())) {
            this.textLines.add(Component.literal(String.valueOf(stack.getCount())).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public int getHeight(Font font) {
        return 10 * this.textLines.size();
    }

    @Override
    public int getWidth(Font font) {
        if (this.widthCache == -1) {
            int maxTextWidth = 0;
            for (Component line : this.textLines) {
                maxTextWidth = Math.max(maxTextWidth, font.width(line));
            }
            this.widthCache = maxTextWidth + 14;
        }
        return this.widthCache;
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        for (int i = 0; i < this.textLines.size(); i++) {
            graphics.text(font, this.textLines.get(i), x + 12, y + i * 10, 0xFFFFFFFF, true);
        }
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        List<ItemStack> originalCoins = (this.data.original().length > 0 && this.data.original()[0] != -1)
                ? CurrencyConverter.getAsItemStackList(this.data.original())
                : List.of();
        List<ItemStack> coins = CurrencyConverter.getAsItemStackList(this.data.value());

        for (int i = 0; i < originalCoins.size(); i++) {
            ItemStack coin = originalCoins.get(i);
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    STRIKETHROUGH_SPRITE,
                    x + (coin.getCount() > 9 ? 14 : 11),
                    y + 3 + i * 10,
                    9,
                    2
            );
            graphics.fakeItem(coin.copyWithCount(1), x - 4, y - 5 + i * 10);
        }

        int offsetY = originalCoins.isEmpty() ? 0 : originalCoins.size() * 10;
        for (int i = 0; i < coins.size(); i++) {
            graphics.fakeItem(coins.get(i).copyWithCount(1), x - 4, y - 5 + i * 10 + offsetY);
        }
    }
}