package com.min01.solomonlib.util;

import java.lang.reflect.Method;
import java.util.UUID;

import com.min01.solomonlib.gravity.GravityZoneManager;
import com.min01.solomonlib.multipart.EntityBounds;
import com.min01.solomonlib.multipart.IMultipart;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class SolomonUtil
{
	public static final Method GET_ENTITY = ObfuscationReflectionHelper.findMethod(Level.class, "m_142646_");
	
	public static String getIntersectingMultiPart(EntityBounds bounds, Entity entity)
	{
		return bounds.intersects(entity.getBoundingBox());
	}

	public static String getIntersectingMultiPart(IMultipart multipart, Entity entity)
	{
		String raw = getIntersectingMultiPart(multipart.getBounds(), entity);
		return multipart.canonicalMultipartPartName(raw);
	}
	
	public static String getCollidingMultiPart(EntityBounds bounds, Entity entity)
	{
    	return bounds.raycast(entity.position(), entity.position().add(entity.getDeltaMovement()));
	}

	public static String getCollidingMultiPart(IMultipart multipart, Entity entity)
	{
		String raw = getCollidingMultiPart(multipart.getBounds(), entity);
		return multipart.canonicalMultipartPartName(raw);
	}
	
	public static String getMultiPart(EntityBounds bounds, Player player)
	{
        Vec3 pos = player.getEyePosition(1.0F);
        Vec3 dir = player.getViewVector(1.0F);
        double reach = player.getBlockReach();
    	return bounds.raycast(pos, pos.add(dir.scale(reach)));
	}

	public static String getMultiPart(IMultipart multipart, Player player)
	{
		String raw = getMultiPart(multipart.getBounds(), player);
		return multipart.canonicalMultipartPartName(raw);
	}
	
	public static ByteBuf writeVec3(FriendlyByteBuf buf, Vec3 vec3)
	{
		buf.writeDouble(vec3.x);
		buf.writeDouble(vec3.y);
		buf.writeDouble(vec3.z);
		return buf;
	}
	
	public static Vec3 readVec3(ByteBuf buf)
	{
		return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Entity> T getEntityByUUID(Level level, UUID uuid)
	{
		try 
		{
			LevelEntityGetter<Entity> entities = (LevelEntityGetter<Entity>) GET_ENTITY.invoke(level);
			return (T) entities.get(uuid);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
    public static Direction getBlockGravityDirection(Level level, BlockPos pos)
    {
        return GravityZoneManager.getDirection(level, pos);
    }

    public static Direction getEntityZoneDirection(Entity entity)
    {
        return GravityZoneManager.getDirection(entity.level, entity.blockPosition());
    }

    public static boolean isBlockUpsideDown(Level level, BlockPos pos)
    {
        return GravityZoneManager.getDirection(level, pos) == Direction.UP;
    }
}
