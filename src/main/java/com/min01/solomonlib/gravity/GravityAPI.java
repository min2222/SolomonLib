package com.min01.solomonlib.gravity;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class GravityAPI
{
	public static final Method GET_ENTITY = ObfuscationReflectionHelper.findMethod(Level.class, "m_142646_");
	
	public static void getClientLevel(Consumer<Level> consumer)
	{
		LogicalSidedProvider.CLIENTWORLD.get(LogicalSide.CLIENT).filter(ClientLevel.class::isInstance).ifPresent(level -> 
		{
			consumer.accept(level);
		});
	}
	
	@SuppressWarnings("unchecked")
	public static Iterable<Entity> getAllEntities(Level level)
	{
		try 
		{
			LevelEntityGetter<Entity> entities = (LevelEntityGetter<Entity>) GET_ENTITY.invoke(level);
			return entities.getAll();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static Entity getEntityByUUID(Level level, UUID uuid)
	{
		try 
		{
			LevelEntityGetter<Entity> entities = (LevelEntityGetter<Entity>) GET_ENTITY.invoke(level);
			return entities.get(uuid);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
    public static MutableComponent getLinkText(String link) 
    {
        return Component.literal(link).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link)).withUnderlined(true));
    }
    
    public static MutableComponent getDirectionText(Direction gravityDirection)
    {
        return Component.translatable("direction." + gravityDirection.getName());
    }
    
    public static double distanceToRange(double value, double rangeStart, double rangeEnd)
    {
        if(value < rangeStart)
        {
            return rangeStart - value;
        }
        
        if(value > rangeEnd)
        {
            return value - rangeEnd;
        }
        
        return 0;
    }
    
    public static boolean isClientPlayer(Entity entity) 
    {
        if(entity.level.isClientSide()) 
        {
            return entity instanceof LocalPlayer;
        }
        return false;
    }
    
    public static boolean isRemotePlayer(Entity entity) 
    {
        if(entity.level.isClientSide()) 
        {
            return entity instanceof RemotePlayer;
        }
        return false;
    }
    
    /**
     * Returns the applied gravity direction for the given entity
     */
    public static Direction getGravityDirection(Entity entity)
    {
        return getGravityCapability(entity).getCurrGravityDirection();
    }
    
    public static double getGravityStrength(Entity entity) 
    {
        return getGravityCapability(entity).getCurrGravityStrength();
    }
    
    public static double getBaseGravityStrength(Entity entity)
    {
        return getGravityCapability(entity).getBaseGravityStrength();
    }
    
    public static void setBaseGravityStrength(Entity entity, double strength) 
    {
        GravityCapabilityImpl cap = getGravityCapability(entity);
		cap.setBaseGravityStrength(strength);
    }
    
    public static void resetGravity(Entity entity)
    {
        getGravityCapability(entity).reset();
    }
    
    /**
     * Returns the main gravity direction for the given entity
     * This may not be the applied gravity direction for the player, see GravityChangerAPI#getAppliedGravityDirection
     */
    public static Direction getBaseGravityDirection(Entity entity) 
    {
        return getGravityCapability(entity).getBaseGravityDirection();
    }
    
    public static void setBaseGravityDirection(Entity entity, Direction gravityDirection) 
    {
        GravityCapabilityImpl cap = getGravityCapability(entity);
		cap.setBaseGravityDirection(gravityDirection);
    }
    
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static RotationAnimation getRotationAnimation(Entity entity)
    {
        return getGravityCapability(entity).getRotationAnimation();
    }
    
    /**
     * Instantly set gravity direction on client side without performing animation.
     * Not needed in normal cases.
     * (Used by iPortal)
     */
    public static void instantlySetClientBaseGravityDirection(Entity entity, Direction direction)
    {
        Validate.isTrue(entity.level.isClientSide(), "should only be used on client");
        
        GravityCapabilityImpl cap = getGravityCapability(entity);
        cap.setBaseGravityDirection(direction);
		cap.updateGravityStatus(false);
		cap.forceApplyGravityChange();
    }
    
    public static GravityCapabilityImpl getGravityCapability(Entity entity)
    {
    	return (GravityCapabilityImpl) entity.getCapability(GravityCapabilityImpl.GRAVITY).orElse(new GravityCapabilityImpl(entity));
    }

    /**
     * Returns the world relative velocity for the given entity
     * Using minecraft's methods to get the velocity will return entity local velocity
     */
    public static Vec3 getWorldVelocity(Entity entity)
    {
        return RotationUtil.vecPlayerToWorld(entity.getDeltaMovement(), getGravityDirection(entity));
    }
    
    /**
     * Sets the world relative velocity for the given player
     * Using minecraft's methods to set the velocity of an entity will set player relative velocity
     */
    public static void setWorldVelocity(Entity entity, Vec3 worldVelocity) 
    {
        entity.setDeltaMovement(RotationUtil.vecWorldToPlayer(worldVelocity, getGravityDirection(entity)));
    }
    
    /**
     * Returns eye position offset from feet position for the given entity
     */
    public static Vec3 getEyeOffset(Entity entity)
    {
        return RotationUtil.vecPlayerToWorld(0, (double) entity.getEyeHeight(), 0, getGravityDirection(entity));
    }
    
	public static double eyeX(Entity entity)
	{
		return getGravityDirection(entity) == Direction.DOWN ? entity.getX() : entity.getEyePosition().x;
	}

	public static double eyeY(Entity entity)
	{
		return getGravityDirection(entity) == Direction.DOWN ? entity.getY() : entity.getEyePosition().y;
	}

	public static double eyeZ(Entity entity)
	{
		return getGravityDirection(entity) == Direction.DOWN ? entity.getZ() : entity.getEyePosition().z;
	}

	public static Vec3 deltaMovement(net.minecraft.world.entity.LivingEntity target)
	{
		Direction grav = getGravityDirection(target);
		if (grav == Direction.DOWN) return target.getDeltaMovement();
		return RotationUtil.vecPlayerToWorld(target.getDeltaMovement(), grav);
	}

	private static Vec3 projectileSpawnVec(LivingEntity shooter)
	{
		Direction g = getGravityDirection(shooter);
		if(g == Direction.DOWN)
		{
			return new Vec3(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
		}
		return shooter.getEyePosition().subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.1D, 0.0D, g));
	}

	public static double projectileSpawnX(LivingEntity shooter)
	{
		return projectileSpawnVec(shooter).x;
	}

	public static double projectileSpawnY(LivingEntity shooter)
	{
		return projectileSpawnVec(shooter).y;
	}

	public static double projectileSpawnZ(LivingEntity shooter)
	{
		return projectileSpawnVec(shooter).z;
	}

	public static double rangedBodyTargetX(net.minecraft.world.entity.LivingEntity target)
	{
		Direction grav = getGravityDirection(target);
		if(grav == Direction.DOWN)
		{
			return target.getX();
		}
		return target.position().add(RotationUtil.vecPlayerToWorld(0.0, target.getBbHeight() * 0.3333333333333333, 0.0, grav)).x;
	}

	public static double rangedBodyTargetY(net.minecraft.world.entity.LivingEntity target, double heightScale)
	{
		Direction grav = getGravityDirection(target);
		if(grav == Direction.DOWN)
		{
			return target.getY(heightScale);
		}
		return target.position().add(RotationUtil.vecPlayerToWorld(0.0, target.getBbHeight() * 0.3333333333333333, 0.0, grav)).y;
	}

	public static double rangedBodyTargetZ(net.minecraft.world.entity.LivingEntity target)
	{
		Direction grav = getGravityDirection(target);
		if(grav == Direction.DOWN)
		{
			return target.getZ();
		}
		return target.position().add(RotationUtil.vecPlayerToWorld(0.0, target.getBbHeight() * 0.3333333333333333, 0.0, grav)).z;
	}

	private static final double RANGED_WITCH_EYE_OFFSET = 1.100000023841858D;

	public static double rangedEyeTargetX(net.minecraft.world.entity.LivingEntity target)
	{
		Direction grav = getGravityDirection(target);
		if(grav == Direction.DOWN)
		{
			return target.getX();
		}
		return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getEyeHeight() - RANGED_WITCH_EYE_OFFSET, 0.0D, grav)).x;
	}

	public static double rangedEyeTargetY(net.minecraft.world.entity.LivingEntity target)
	{
		Direction grav = getGravityDirection(target);
		if(grav == Direction.DOWN)
		{
			return target.getEyeY();
		}
		return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getEyeHeight() - RANGED_WITCH_EYE_OFFSET, 0.0D, grav)).y + RANGED_WITCH_EYE_OFFSET;
	}

	public static double rangedEyeTargetZ(net.minecraft.world.entity.LivingEntity target)
	{
		Direction grav = getGravityDirection(target);
		if(grav == Direction.DOWN)
		{
			return target.getZ();
		}
		return target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getEyeHeight() - RANGED_WITCH_EYE_OFFSET, 0.0D, grav)).z;
	}

	public static double rangedSqrt(double value, net.minecraft.world.entity.LivingEntity target)
	{
		if(getGravityDirection(target) == Direction.DOWN)
		{
			return Math.sqrt(value);
		}
		return Math.sqrt(Math.sqrt(value));
	}
	
	public static Vec3 addWithGravity(Vec3 vec, double x, double y, double z, Entity entity)
	{
		return vec.add(x, y * getGravityStrength(entity), z);
	}

	public static double scale(double constant, Entity entity)
	{
		return constant * getGravityStrength(entity);
	}

	public static float scaleF(float value, Entity entity)
	{
		return value * (float) getGravityStrength(entity);
	}
}
