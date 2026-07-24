package com.min01.solomonlib.light;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface IDynamicLight
{
	default boolean shouldUpdate()
	{
		return true;
	}
	
	default boolean shouldUpdate(Entity entity, ItemStack stack)
	{
		return true;
	}

	default Vec3 getDynamicLightPos()
	{
		return Vec3.ZERO;
	}
	
	default Vec3 getDynamicLightPos(Entity entity, ItemStack stack)
	{
		return Vec3.ZERO;
	}
	
	default int getDynamicLightLuminance()
	{
		return 15;
	}

	default int getDynamicLightLuminance(Entity entity, ItemStack stack)
	{
		return 15;
	}
}
