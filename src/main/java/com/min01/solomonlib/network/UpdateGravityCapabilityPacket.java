package com.min01.solomonlib.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.solomonlib.capabilities.SolomonCapabilities;
import com.min01.solomonlib.gravity.GravityAPI;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class UpdateGravityCapabilityPacket 
{
	private final UUID entityUUID;
	private final boolean noAnimation;
	private final Direction baseGravityDirection;
	private final Direction currentGravityDirection;
	private final double baseGravityStrength;
	private final double currentGravityStrength;
	
	public UpdateGravityCapabilityPacket(boolean noAnimation, UUID entityUUID, Direction baseGravityDirection, Direction currentGravityDirection, double baseGravityStrength, double currentGravityStrength) 
	{
		this.noAnimation = noAnimation;
		this.entityUUID = entityUUID;
		this.baseGravityDirection = baseGravityDirection;
		this.currentGravityDirection = currentGravityDirection;
		this.baseGravityStrength = baseGravityStrength;
		this.currentGravityStrength = currentGravityStrength;
	}

	public UpdateGravityCapabilityPacket(FriendlyByteBuf buf)
	{
		this.noAnimation = buf.readBoolean();
		this.entityUUID = buf.readUUID();
		this.baseGravityDirection = buf.readEnum(Direction.class);
		this.currentGravityDirection = buf.readEnum(Direction.class);
		this.baseGravityStrength = buf.readDouble();
		this.currentGravityStrength = buf.readDouble();
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeBoolean(this.noAnimation);
		buf.writeUUID(this.entityUUID);
		buf.writeEnum(this.baseGravityDirection);
		buf.writeEnum(this.currentGravityDirection);
		buf.writeDouble(this.baseGravityStrength);
		buf.writeDouble(this.currentGravityStrength);
	}
	
	public static class Handler 
	{
		public static boolean onMessage(UpdateGravityCapabilityPacket message, Supplier<NetworkEvent.Context> ctx) 
		{
			ctx.get().enqueueWork(() ->
			{
				if(ctx.get().getDirection().getReceptionSide().isClient())
				{
					GravityAPI.getClientLevel(level -> 
					{
						Entity entity = GravityAPI.getEntityByUUID(level, message.entityUUID);
						entity.getCapability(SolomonCapabilities.GRAVITY).ifPresent(cap -> 
						{
							cap.sync(message.noAnimation, message.baseGravityDirection, message.currentGravityDirection, message.baseGravityStrength, message.currentGravityStrength);
						});
					});
				}
			});

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
