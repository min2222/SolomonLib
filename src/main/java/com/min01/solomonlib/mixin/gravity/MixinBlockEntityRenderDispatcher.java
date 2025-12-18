package com.min01.solomonlib.mixin.gravity;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.beyondtheabyss.util.MirroredCityUtil;
import com.min01.gravityapi.util.RotationUtil;
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
	private static <T extends BlockEntity> void setupAndRenderBefore(BlockEntityRenderer<T> p_112285_, T p_112286_, float p_112287_, PoseStack p_112288_, MultiBufferSource p_112289_, CallbackInfo ci) 
	{
		Level level = p_112286_.getLevel();
		BlockPos pos = p_112286_.getBlockPos();
		if(level != null && SolomonUtil.isBlockUpsideDown(pos, level))
		{
			p_112288_.pushPose();
			p_112288_.translate(0.5F, 0.5F, 0.5F);
			Quaternionf quat = new Quaternionf(RotationUtil.getWorldRotationQuaternion(Direction.UP));
			p_112288_.mulPose(quat);
			p_112288_.scale(-1.0F, 1.0F, 1.0F); 
			p_112288_.translate(-0.5F, -0.5F, -0.5F);
			RenderSystem.disableCull();
		}
	}
	
	@Inject(method = "setupAndRender", at = @At("TAIL"), cancellable = true)
	private static <T extends BlockEntity> void setupAndRenderAfter(BlockEntityRenderer<T> p_112285_, T p_112286_, float p_112287_, PoseStack p_112288_, MultiBufferSource p_112289_, CallbackInfo ci) 
	{
		Level level = p_112286_.getLevel();
		BlockPos pos = p_112286_.getBlockPos();
		if(level != null && SolomonUtil.isBlockUpsideDown(pos, level))
		{
            RenderSystem.enableCull();
            p_112288_.popPose();
		}
	}
}
