package com.glisco.numismaticoverhaul.client.gui;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.piggy.PiggyBankMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class PiggyBankScreen extends AbstractContainerScreen<PiggyBankMenu> {

    private static final Identifier TEXTURE = NumismaticOverhaul.id("textures/gui/piggy_bank.png");

    public PiggyBankScreen(PiggyBankMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 145);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 5;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int left = this.leftPos;
        int top = this.topPos;

        // Основной фон копилки (176x145)
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, left, top, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        // Иконки-подсказки монет в пустых слотах (u = 0, 16, 32; v = 145)
        for (int i = 0; i < 3; i++) {
            if (!this.menu.getSlot(i).hasItem()) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        TEXTURE,
                        left + 62 + i * 18,
                        top + 26,
                        i * 16,
                        145,
                        16,
                        16,
                        256,
                        256
                );
            }
        }
    }
}