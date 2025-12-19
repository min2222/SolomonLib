package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

	@Shadow
	public ServerPlayer player;

	@Shadow
	private double lastGoodX, lastGoodY, lastGoodZ;

	@ModifyVariable(
			method = "handleMovePlayer",
			at = @At(value = "STORE", ordinal = 0),
			ordinal = 0,
			name = "flag"
	)
	private boolean modifyFlagBasedOnGravity(boolean originalFlag, ServerboundMovePlayerPacket packet) {
		Direction gravity = GravityAPI.getGravityDirection(player);

		double dx = packet.getX(player.getX()) - lastGoodX;
		double dy = packet.getY(player.getY()) - lastGoodY;
		double dz = packet.getZ(player.getZ()) - lastGoodZ;

		Vec3 localVec = RotationUtil.vecWorldToPlayer(dx, dy, dz, gravity);
		return localVec.y > 0.0;
	}
}