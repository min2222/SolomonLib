package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin 
{
	@Redirect(method = "Lnet/minecraft/world/item/CrossbowItem;shootProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;FZFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getX()D", ordinal = 0))
	private static double redirect_shoot_getX_0(LivingEntity livingEntity) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(livingEntity);
		if(gravityDirection == Direction.DOWN)
		{
			return livingEntity.getX();
		}

		return livingEntity.getEyePosition().subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.15000000596046448D, 0.0D, gravityDirection)).x;
	}

	@Redirect(method = "Lnet/minecraft/world/item/CrossbowItem;shootProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;FZFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getEyeY()D", ordinal = 0))
	private static double redirect_shoot_getEyeY_0(LivingEntity livingEntity)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(livingEntity);
		if(gravityDirection == Direction.DOWN) 
		{
			return livingEntity.getEyeY();
		}

		return livingEntity.getEyePosition().subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.15000000596046448D, 0.0D, gravityDirection)).y + 0.15000000596046448D;
	}

	@Redirect(method = "Lnet/minecraft/world/item/CrossbowItem;shootProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;FZFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D", ordinal = 0))
	private static double redirect_shoot_getZ_0(LivingEntity livingEntity)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(livingEntity);
		if(gravityDirection == Direction.DOWN) 
		{
			return livingEntity.getZ();
		}

		return livingEntity.getEyePosition().subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.15000000596046448D, 0.0D, gravityDirection)).z;
	}
}
