package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;

@Mixin(value = Direction.class, priority = 1001)
public abstract class DirectionMixin
{
	@WrapOperation(method = "orderedByNearest", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F", ordinal = 0))
	private static float wrapOperation_getEntityFacingOrder_getYaw_0(Entity entity, float tickDelta, Operation<Float> original) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(entity);
		if(gravityDirection == Direction.DOWN) 
		{
			return original.call(entity, tickDelta);
		}

		return RotationUtil.rotPlayerToWorld(original.call(entity, tickDelta), entity.getViewXRot(tickDelta), gravityDirection).x;
	}

	@WrapOperation(method = "orderedByNearest", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F", ordinal = 0))
	private static float wrapOperation_getEntityFacingOrder_getPitch_0(Entity entity, float tickDelta, Operation<Float> original)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(entity);
		if(gravityDirection == Direction.DOWN) 
		{
			return original.call(entity, tickDelta);
		}

		return RotationUtil.rotPlayerToWorld(entity.getViewYRot(tickDelta), original.call(entity, tickDelta), gravityDirection).y;
	}

	@WrapOperation(method = "getFacingAxis", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F", ordinal = 0))
	private static float wrapOperation_getLookDirectionForAxis_getYaw_0(Entity entity, float tickDelta, Operation<Float> original) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(entity);
		if(gravityDirection == Direction.DOWN)
		{
			return original.call(entity, tickDelta);
		}

		return RotationUtil.rotPlayerToWorld(original.call(entity, tickDelta), entity.getViewXRot(tickDelta), gravityDirection).x;
	}

	@WrapOperation(method = "getFacingAxis", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F", ordinal = 1))
	private static float wrapOperation_getLookDirectionForAxis_getYaw_1(Entity entity, float tickDelta, Operation<Float> original)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(entity);
		if(gravityDirection == Direction.DOWN) 
		{
			return original.call(entity, tickDelta);
		}

		return RotationUtil.rotPlayerToWorld(original.call(entity, tickDelta), entity.getViewXRot(tickDelta), gravityDirection).x;
	}

	@WrapOperation(method = "getFacingAxis", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F", ordinal = 0))
	private static float wrapOperation_getLookDirectionForAxis_getPitch_0(Entity entity, float tickDelta, Operation<Float> original) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(entity);
		if(gravityDirection == Direction.DOWN)
		{
			return original.call(entity, tickDelta);
		}

		return RotationUtil.rotPlayerToWorld(entity.getViewYRot(tickDelta), original.call(entity, tickDelta), gravityDirection).y;
	}
}
