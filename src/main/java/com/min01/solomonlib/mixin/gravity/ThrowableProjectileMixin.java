package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.Vec3;

@Mixin(ThrowableProjectile.class)
public abstract class ThrowableProjectileMixin 
{
	@Shadow
	protected abstract float getGravity();

	@ModifyVariable(method = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;tick()V", at = @At(value = "STORE"), ordinal = 0)
	public Vec3 tick(Vec3 modify) 
	{
		modify = new Vec3(modify.x, modify.y + this.getGravity(), modify.z);
		modify = RotationUtil.vecWorldToPlayer(modify, GravityAPI.getGravityDirection((ThrowableProjectile) (Object) this));
		modify = new Vec3(modify.x, modify.y - this.getGravity(), modify.z);
		modify = RotationUtil.vecPlayerToWorld(modify, GravityAPI.getGravityDirection((ThrowableProjectile) (Object) this));
		return modify;
	}

	@ModifyReturnValue(method = "getGravity", at = @At("RETURN"))
	private float multiplyGravity(float original) 
	{
		return original * (float) GravityAPI.getGravityStrength(((Entity) (Object) this));
	}
}
