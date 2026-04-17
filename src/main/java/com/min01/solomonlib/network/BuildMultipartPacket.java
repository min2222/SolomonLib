package com.min01.solomonlib.network;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import com.min01.solomonlib.multipart.EntityBounds;
import com.min01.solomonlib.multipart.EntityPartBuilder;
import com.min01.solomonlib.multipart.EntityPartBuilder.Part;
import com.min01.solomonlib.multipart.IMultipart;
import com.min01.solomonlib.util.SolomonUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class BuildMultipartPacket 
{
	private static final int MAX_PARTS = 1024;
	private final UUID entityUUID;
	
	public final Map<String, Vec3> partOffset;
	public final Map<String, String> parts;
	public final Map<String, Part> partMap;
	public final EntityBounds bounds;

	public BuildMultipartPacket(UUID entityUUID, Map<String, Vec3> partOffset, Map<String, String> parts, Map<String, Part> partMap, EntityBounds bounds) 
	{
		this.entityUUID = entityUUID;
		this.partOffset = partOffset;
		this.parts = parts;
		this.partMap = partMap;
		this.bounds = bounds;
	}

	public static BuildMultipartPacket read(FriendlyByteBuf buf)
	{
	    return new BuildMultipartPacket(buf.readUUID(), buf.readMap(t -> t.readUtf(), t -> SolomonUtil.readVec3(t)), buf.readMap(t -> t.readUtf(), t -> t.readUtf()), buf.readMap(t -> t.readUtf(), t -> Part.read(t)), EntityBounds.read(buf));
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.entityUUID);
		buf.writeMap(this.partOffset, (t, u) -> t.writeUtf(u), (t, u) -> SolomonUtil.writeVec3(t, u));
		buf.writeMap(this.parts, (t, u) -> t.writeUtf(u), (t, u) -> t.writeUtf(u));
		buf.writeMap(this.partMap, (t, u) -> t.writeUtf(u), (t, u) -> Part.write(t, u));
		this.bounds.write(buf);
	}

	public static boolean handle(BuildMultipartPacket message, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isServer())
			{
				if(ctx.get().getSender() == null || message.parts.size() > MAX_PARTS || message.partMap.size() > MAX_PARTS || message.partOffset.size() > MAX_PARTS)
				{
					return;
				}
				Entity entity = SolomonUtil.getEntityByUUID(ctx.get().getSender().level, message.entityUUID);
				if(entity instanceof IMultipart multipart)
				{
					if(!entity.getUUID().equals(message.entityUUID))
					{
						return;
					}
					EntityPartBuilder<?> partBuilder = multipart.getPartBuilder();
					partBuilder.partOffset.clear();
					partBuilder.parts.clear();
					partBuilder.partMap.clear();
					partBuilder.partOffset.putAll(message.partOffset);
					partBuilder.parts.putAll(message.parts);
					for(Map.Entry<String, Part> entry : message.partMap.entrySet())
					{
						if(partBuilder.parts.containsKey(entry.getKey()) || EntityPartBuilder.ROOT.equals(entry.getKey()))
						{
							partBuilder.partMap.put(entry.getKey(), entry.getValue());
						}
					}
					partBuilder.hitbox = message.bounds;
				}
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}
