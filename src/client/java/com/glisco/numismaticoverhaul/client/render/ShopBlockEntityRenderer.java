package com.glisco.numismaticoverhaul.client.render;

import com.glisco.numismaticoverhaul.block.shop.ShopBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ShopBlockEntityRenderer implements BlockEntityRenderer<ShopBlockEntity, ShopRenderState> {

    private final ItemModelResolver itemModelResolver;

    public ShopBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public ShopRenderState createRenderState() {
        return new ShopRenderState();
    }

    @Override
    public void extractRenderState(
            ShopBlockEntity blockEntity,
            ShopRenderState state,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumblingOverlay);
        ItemStack stack = blockEntity.getItemToRender();
        state.hasItem = !stack.isEmpty();
        if (state.hasItem && blockEntity.getLevel() != null) {
            this.itemModelResolver.updateForTopItem(
                    state.itemState,
                    stack,
                    ItemDisplayContext.GROUND,
                    blockEntity.getLevel(),
                    null,
                    0
            );
            state.rotationDegrees = (blockEntity.getLevel().getGameTime() + partialTick) * 2.0F;
        }
    }

    @Override
    public void submit(ShopRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.hasItem) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.9, 0.5);
        poseStack.mulPose(new Matrix4f().rotationY((float) Math.toRadians(state.rotationDegrees)));
        poseStack.scale(0.85F, 0.85F, 0.85F);

        state.itemState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}