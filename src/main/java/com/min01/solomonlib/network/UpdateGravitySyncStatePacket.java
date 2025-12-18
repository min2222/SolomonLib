package com.min01.solomonlib.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.GravityCapabilityImpl;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class UpdateGravitySyncStatePacket 
{
	private final UUID entityUUID;
	
	public UpdateGravitySyncStatePacket(UUID entityUUID) 
	{
		this.entityUUID = entityUUID;
	}

	public UpdateGravitySyncStatePacket(FriendlyByteBuf buf)
	{
		this.entityUUID = buf.readUUID();
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.entityUUID);
	}
	
	public static class Handler 
	{
		public static boolean onMessage(UpdateGravitySyncStatePacket message, Supplier<NetworkEvent.Context> ctx) 
		{
			ctx.get().enqueueWork(() ->
			{
				if(ctx.get().getDirection().getReceptionSide().isServer())
				{
					Entity entity = GravityAPI.getEntityByUUID(ctx.get().getSender().level, message.entityUUID);
					GravityCapabilityImpl cap = GravityAPI.getGravityComponent(entity);
					cap.needsSync = false;
					cap.noAnimation = false;
				}
			});

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
