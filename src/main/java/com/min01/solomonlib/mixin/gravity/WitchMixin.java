package com.min01.solomonlib.mixin.gravity;

import com.min01.solomonlib.gravity.GravityAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.phys.Vec3;

@Mixin(Witch.class)
public abstract class WitchMixin
{
	@ModifyVariable(method = "Lnet/minecraft/world/entity/monster/Witch;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/LivingEntity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;", ordinal = 0), ordinal = 0)
	private Vec3 modify_attack_Vec3d_0(Vec3 value, LivingEntity target, float pullProgress)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(target);
		if(gravityDirection == Direction.DOWN) 
		{
			return value;
		}

		return RotationUtil.vecPlayerToWorld(value, gravityDirection);
	}

	@Redirect(method = "Lnet/minecraft/world/entity/monster/Witch;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getX()D", ordinal = 0))
	private double redirect_attack_getX_0(LivingEntity target) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(target);
		if(gravityDirection == Direction.DOWN) 
		{
			return target.getX();
		}

		return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getEyeHeight() - 1.100000023841858D, 0.0D, gravityDirection)).x;
	}

	@Redirect(method = "Lnet/minecraft/world/entity/monster/Witch;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getEyeY()D", ordinal = 0))
	private double redirect_attack_getEyeY_0(LivingEntity target) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(target);
		if(gravityDirection == Direction.DOWN)
		{
			return target.getEyeY();
		}

		return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getEyeHeight() - 1.100000023841858D, 0.0D, gravityDirection)).y + 1.100000023841858D;
	}

	@Redirect(method = "Lnet/minecraft/world/entity/monster/Witch;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D", ordinal = 0))
	private double redirect_attack_getZ_0(LivingEntity target) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(target);
		if(gravityDirection == Direction.DOWN)
		{
			return target.getZ();
		}

		return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getEyeHeight() - 1.100000023841858D, 0.0D, gravityDirection)).z;
	}

	@Redirect(method = "Lnet/minecraft/world/entity/monster/Witch;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;sqrt(D)D"))
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
