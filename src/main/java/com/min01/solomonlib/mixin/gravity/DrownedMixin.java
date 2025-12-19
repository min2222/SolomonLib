package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Drowned;

@Mixin(Drowned.class)
public abstract class DrownedMixin {
	@Redirect(method = "Lnet/minecraft/world/entity/monster/Drowned;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getX()D", ordinal = 0))
	private double redirect_attack_getX_0(LivingEntity target) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(target);
		if(gravityDirection == Direction.DOWN)
		{
			return target.getX();
		}

		return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getBbHeight() * 0.3333333333333333D, 0.0D, gravityDirection)).x;
	}

	@Redirect(method = "Lnet/minecraft/world/entity/monster/Drowned;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getY(D)D", ordinal = 0))
	private double redirect_attack_getBodyY_0(LivingEntity target, double heightScale) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(target);
		if(gravityDirection == Direction.DOWN) 
		{
			return target.getY(heightScale);
		}

		return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getBbHeight() * 0.3333333333333333D, 0.0D, gravityDirection)).y;
	}

	@Redirect(method = "Lnet/minecraft/world/entity/monster/Drowned;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D", ordinal = 0))
	private double redirect_attack_getZ_0(LivingEntity target) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(target);
		if(gravityDirection == Direction.DOWN) 
		{
			return target.getZ();
		}

		return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getBbHeight() * 0.3333333333333333D, 0.0D, gravityDirection)).z;
	}

	@Redirect(method = "Lnet/minecraft/world/entity/monster/Drowned;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;sqrt(D)D"))
	private double redirect_attack_sqrt_0(double value, LivingEntity target, float pullProgress)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(target);
		if(gravityDirection == Direction.DOWN)
		{
			return Math.sqrt(value);
		}

		return Math.sqrt(Math.sqrt(value));
	}
}
