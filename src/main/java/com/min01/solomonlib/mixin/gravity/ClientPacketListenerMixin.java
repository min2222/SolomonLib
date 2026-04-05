package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

@Mixin(value = ClientPacketListener.class, priority = 1001)
public abstract class ClientPacketListenerMixin
{
	@WrapOperation(method = "handleExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;", ordinal = 0))
	private Vec3 wrapOperation_onExplosion_add_0(Vec3 vec3d, double x, double y, double z, Operation<Vec3> original)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(Minecraft.getInstance().player);
		if(gravityDirection == Direction.DOWN) 
		{
			return original.call(vec3d, x, y, z);
		}

		Vec3 player = RotationUtil.vecWorldToPlayer(x, y, z, gravityDirection);
		return original.call(vec3d, player.x, player.y, player.z);
	}
}
