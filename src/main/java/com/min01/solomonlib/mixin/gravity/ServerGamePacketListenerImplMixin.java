package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin
{
	@Shadow
	public ServerPlayer player;

	@Shadow
	private double lastGoodX, lastGoodY, lastGoodZ;

	@ModifyVariable(method = "handleMovePlayer", at = @At(value = "STORE", ordinal = 0), ordinal = 0, name = "flag")
	private boolean modifyFlagBasedOnGravity(boolean originalFlag, ServerboundMovePlayerPacket packet)
	{
		Direction gravity = GravityAPI.getGravityDirection(this.player);

		double dx = packet.getX(this.player.getX()) - this.lastGoodX;
		double dy = packet.getY(this.player.getY()) - this.lastGoodY;
		double dz = packet.getZ(this.player.getZ()) - this.lastGoodZ;

		Vec3 localVec = RotationUtil.vecWorldToPlayer(dx, dy, dz, gravity);
		return localVec.y > 0.0;
	}

	@ModifyArg(method = "handleMoveVehicle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"), index = 1)
	private Vec3 modify_onVehicleMove_move_0(Vec3 vec3d) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(this.player);
		if(gravityDirection == Direction.DOWN) 
		{
			return vec3d;
		}

		return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
	}

	@WrapOperation(method = "noBlocksAround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;expandTowards(DDD)Lnet/minecraft/world/phys/AABB;"))
	private AABB modify_onVehicleMove_move_0(AABB instance, double x, double y, double z, Operation<AABB> original)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(this.player);
		Vec3 argVec = new Vec3(x, y, z);
		argVec = RotationUtil.vecWorldToPlayer(argVec, gravityDirection);
		return original.call(instance, argVec.x, argVec.y, argVec.z);
	}
}