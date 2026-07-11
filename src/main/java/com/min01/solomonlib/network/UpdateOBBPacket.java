package com.min01.solomonlib.network;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.min01.solomonlib.multipart.EntityPartBuilder;
import com.min01.solomonlib.multipart.EntityPartBuilder.OBBData;
import com.min01.solomonlib.multipart.IMultipart;
import com.min01.solomonlib.util.SolomonUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class UpdateOBBPacket 
{
	private final UUID entityUUID;
	private final List<OBBData> data;

	public UpdateOBBPacket(UUID uuid, List<OBBData> data) 
	{
		this.entityUUID = uuid;
		this.data = data;
	}

	public static UpdateOBBPacket read(FriendlyByteBuf buf)
	{
		return new UpdateOBBPacket(buf.readUUID(), buf.readList(t -> OBBData.read(t)));
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.entityUUID);
		buf.writeCollection(this.data, (t, u) -> u.write(t));
	}

	public static boolean handle(UpdateOBBPacket message, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isServer())
			{
				Entity entity = SolomonUtil.getEntityByUUID(ctx.get().getSender().level, message.entityUUID);
				if(entity instanceof IMultipart multipart) 
				{
					EntityPartBuilder builder = multipart.getPartBuilder();
					builder.update(message.data);
				}
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}
