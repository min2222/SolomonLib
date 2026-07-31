package com.min01.solomonlib.util;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SolomonUtil
{
	public static ByteBuf writeVec3(FriendlyByteBuf buf, Vec3 vec3)
	{
		buf.writeDouble(vec3.x);
		buf.writeDouble(vec3.y);
		buf.writeDouble(vec3.z);
		return buf;
	}
	
	public static Vec3 readVec3(FriendlyByteBuf buf)
	{
		return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
	}
	
	public static Iterable<Entity> getAllEntities(Level level)
	{
		return level.getEntities().getAll();
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Entity> T getEntityByUUID(Level level, UUID uuid)
	{
		return (T) level.getEntities().get(uuid);
	}
}
