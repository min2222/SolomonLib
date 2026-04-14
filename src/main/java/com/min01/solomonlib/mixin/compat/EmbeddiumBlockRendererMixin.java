package com.min01.solomonlib.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.solomonlib.gravity.GravityBakedModelWrapper;
import com.min01.solomonlib.gravity.GravityZoneManager;
import com.min01.solomonlib.util.SolomonClientUtil;

import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

@Mixin(value = BlockRenderer.class, remap = false)
public class EmbeddiumBlockRendererMixin
{
	@WrapOperation(method = "getGeometry", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;model()Lnet/minecraft/client/resources/model/BakedModel;"))
	private BakedModel solomon$wrapGravityModel(BlockRenderContext context, Operation<BakedModel> original)
	{
		BakedModel model = original.call(context);
		if(model instanceof GravityBakedModelWrapper)
		{
			return model;
		}
		Level level = SolomonClientUtil.MC.level;
		if(level == null)
		{
			return model;
		}
		Direction grav = GravityZoneManager.getDirection(level, context.pos());
		if(grav == Direction.DOWN)
		{
			return model;
		}
		return new GravityBakedModelWrapper(model, grav);
	}

	@Inject(method = "isFaceVisible", at = @At("HEAD"), cancellable = true)
	private void solomon$faceVisibleNearGravityZone(BlockRenderContext ctx, Direction face, CallbackInfoReturnable<Boolean> cir)
	{
		Level level = SolomonClientUtil.MC.level;
		if(level == null)
		{
			return;
		}
		BlockPos adjacent = ctx.pos().relative(face);
		if(GravityZoneManager.getDirection(level, adjacent) != Direction.DOWN)
		{
			cir.setReturnValue(true);
		}
	}
}
