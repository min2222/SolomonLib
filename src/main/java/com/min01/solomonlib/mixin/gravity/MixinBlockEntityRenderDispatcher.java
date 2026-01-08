package com.min01.solomonlib.mixin.gravity;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.solomonlib.gravity.RotationUtil;
import com.min01.solomonlib.util.SolomonUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

//FIXME bed blocks culling issue;
@Mixin(value = BlockEntityRenderDispatcher.class, priority = -10000)
public class MixinBlockEntityRenderDispatcher
{
	@Inject(method = "setupAndRender", at = @At("HEAD"), cancellable = true)
	private static <T extends BlockEntity> void setupAndRenderBefore(BlockEntityRenderer<T> pRenderer, T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, CallbackInfo ci) 
	{
		Level level = pBlockEntity.getLevel();
		BlockPos pos = pBlockEntity.getBlockPos();
		if(level != null && SolomonUtil.isBlockUpsideDown(level, pos))
		{
			pPoseStack.pushPose();
			pPoseStack.translate(0.5F, 0.5F, 0.5F);
			Quaternionf quat = new Quaternionf(RotationUtil.getWorldRotationQuaternion(Direction.UP));
			pPoseStack.mulPose(quat);
			pPoseStack.scale(-1.0F, 1.0F, 1.0F); 
			pPoseStack.translate(-0.5F, -0.5F, -0.5F);
			RenderSystem.disableCull();
		}
	}
	
	@Inject(method = "setupAndRender", at = @At("TAIL"), cancellable = true)
	private static <T extends BlockEntity> void setupAndRenderAfter(BlockEntityRenderer<T> pRenderer, T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, CallbackInfo ci) 
	{
		Level level = pBlockEntity.getLevel();
		BlockPos pos = pBlockEntity.getBlockPos();
		if(level != null && SolomonUtil.isBlockUpsideDown(level, pos))
		{
            RenderSystem.enableCull();
            pPoseStack.popPose();
		}
	}
}
