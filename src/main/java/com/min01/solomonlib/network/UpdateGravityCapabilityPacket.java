package com.min01.solomonlib.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.GravityCapabilityImpl;

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

	public static UpdateGravityCapabilityPacket read(FriendlyByteBuf buf)
	{
		return new UpdateGravityCapabilityPacket(buf.readBoolean(), buf.readUUID(), buf.readEnum(Direction.class), buf.readEnum(Direction.class), buf.readDouble(), buf.readDouble());
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeBoolean(this.noAnimation);
		buf.writeUUID(this.entityUUID);
		buf.writeEnum(this.baseGravityDirection);
		buf.writeEnum(this.currentGravityDirection);
		buf.writeDouble(this.baseGravityStrength);
		buf.writeDouble(this.currentGravityStrength);
	}
	
	public static boolean handle(UpdateGravityCapabilityPacket message, Supplier<NetworkEvent.Context> ctx) 
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isClient())
			{
				GravityAPI.getClientLevel(level -> 
				{
					Entity entity = GravityAPI.getEntityByUUID(level, message.entityUUID);
					entity.getCapability(GravityCapabilityImpl.GRAVITY).ifPresent(cap -> 
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
