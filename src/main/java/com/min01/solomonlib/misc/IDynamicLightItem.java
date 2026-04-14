package com.min01.solomonlib.misc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface IDynamicLightItem
{
	boolean shouldUpdateDynamicLight(Entity player, ItemStack stack);

	Vec3 getDynamicLightPos(Entity player, ItemStack stack);

	default int getDynamicLightLuminance(Entity player, ItemStack stack)
	{
		return 15;
	}
}
