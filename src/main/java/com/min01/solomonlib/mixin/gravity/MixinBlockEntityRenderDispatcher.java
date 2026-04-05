package com.min01.solomonlib.mixin.gravity;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.solomonlib.gravity.RotationUtil;
import com.min01.solomonlib.gravity.zone.GravityZoneManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(value = BlockEntityRenderDispatcher.class, priority = -10000)
public class MixinBlockEntityRenderDispatcher
{
    @Inject(method = "setupAndRender", at = @At("HEAD"), cancellable = true)
    private static <T extends BlockEntity> void setupAndRenderBefore(BlockEntityRenderer<T> pRenderer, T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, CallbackInfo ci)
    {
        Level level = pBlockEntity.getLevel();
        if(level == null)
        {
            return;
        }

        BlockPos pos = pBlockEntity.getBlockPos();
        Direction gravDir = GravityZoneManager.getDirection(level, pos);
        if(gravDir == Direction.DOWN)
        {
            return;
        }

        pPoseStack.pushPose();
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(new Quaternionf(RotationUtil.getCameraRotationQuaternion(gravDir)));

        if(gravDir == Direction.UP)
        {
            pPoseStack.scale(-1.0F, 1.0F, 1.0F);
            RenderSystem.disableCull();
        }

        pPoseStack.translate(-0.5F, -0.5F, -0.5F);
    }

    @Inject(method = "setupAndRender", at = @At("TAIL"))
    private static <T extends BlockEntity> void setupAndRenderAfter(BlockEntityRenderer<T> pRenderer, T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, CallbackInfo ci)
    {
        Level level = pBlockEntity.getLevel();
        if(level == null)
        {
            return;
        }

        BlockPos pos = pBlockEntity.getBlockPos();
        Direction gravDir = GravityZoneManager.getDirection(level, pos);
        if(gravDir == Direction.DOWN)
        {
            return;
        }

        if(gravDir == Direction.UP)
        {
            RenderSystem.enableCull();
        }
        pPoseStack.popPose();
    }
}
