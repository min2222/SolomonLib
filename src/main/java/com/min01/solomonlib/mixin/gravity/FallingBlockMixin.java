package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockMixin extends Entity
{
	public FallingBlockMixin(EntityType<?> type, Level world)
	{
		super(type, world);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
	private Vec3 wrapOperation_tick_addGravity(Vec3 vec3, double x, double y, double z, Operation<Vec3> original)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(this);
		double strength = GravityAPI.getGravityStrength(this);
		if(gravityDirection == Direction.DOWN)
			return original.call(vec3, x, y * strength, z);
		Vec3 gravityVec = RotationUtil.vecPlayerToWorld(0.0, y * strength, 0.0, gravityDirection);
		return original.call(vec3, gravityVec.x, gravityVec.y, gravityVec.z);
	}
}
