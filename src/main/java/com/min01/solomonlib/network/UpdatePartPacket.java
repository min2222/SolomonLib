package com.min01.solomonlib.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.solomonlib.multipart.EntityPartBuilder.Part;
import com.min01.solomonlib.multipart.IMultipart;
import com.min01.solomonlib.util.SolomonUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class UpdatePartPacket 
{
	private final UUID entityUUID;
	
	public final String name;
	
	public float x;
	public float y;
	public float z;
	
	public float xRot;
	public float yRot;
	public float zRot;

	public UpdatePartPacket(UUID entityUUID, String name, float x, float y, float z, float xRot, float yRot, float zRot) 
	{
		this.entityUUID = entityUUID;
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.xRot = xRot;
		this.yRot = yRot;
		this.zRot = zRot;
	}

	public static UpdatePartPacket read(FriendlyByteBuf buf)
	{
		return new UpdatePartPacket(buf.readUUID(), buf.readUtf(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.entityUUID);
		buf.writeUtf(this.name);
		buf.writeFloat(this.x);
		buf.writeFloat(this.y);
		buf.writeFloat(this.z);
		buf.writeFloat(this.xRot);
		buf.writeFloat(this.yRot);
		buf.writeFloat(this.zRot);
	}

	public static boolean handle(UpdatePartPacket message, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isServer())
			{
				Entity entity = SolomonUtil.getEntityByUUID(ctx.get().getSender().level, message.entityUUID);
				if(entity instanceof IMultipart multipart) 
				{
					Part part = multipart.getPartBuilder().partMap.get(message.name);
					if(part != null)
					{
						part.tick(message.x, message.y, message.z, message.xRot, message.yRot, message.zRot);
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}
