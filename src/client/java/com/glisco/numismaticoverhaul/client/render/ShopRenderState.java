package com.glisco.numismaticoverhaul.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class ShopRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState itemState = new ItemStackRenderState();
    public boolean hasItem = false;
    public float rotationDegrees = 0.0F;
}