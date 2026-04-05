package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin 
{
	@Redirect(method = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;performRangedAttack(ILnet/minecraft/world/entity/LivingEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getX()D", ordinal = 0))
	private double redirect_shootSkullAt_getX_0(LivingEntity target) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(target);
		if(gravityDirection == Direction.DOWN)
		{
			return target.getX();
		}

		return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getEyeHeight() * 0.5D, 0.0D, gravityDirection)).x;
	}

	@Redirect(method = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;performRangedAttack(ILnet/minecraft/world/entity/LivingEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getY()D", ordinal = 0))
	private double redirect_shootSkullAt_getY_0(LivingEntity target)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(target);
		if(gravityDirection == Direction.DOWN)
		{
			return target.getY();
		}

		return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getEyeHeight() * 0.5D, 0.0D, gravityDirection)).y - target.getEyeHeight() * 0.5D;
	}

	@Redirect(method = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;performRangedAttack(ILnet/minecraft/world/entity/LivingEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D", ordinal = 0))
	private double redirect_shootSkullAt_getZ_0(LivingEntity target)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(target);
		if(gravityDirection == Direction.DOWN)
		{
			return target.getZ();
		}

		return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getEyeHeight() * 0.5D, 0.0D, gravityDirection)).z;
	}
}
