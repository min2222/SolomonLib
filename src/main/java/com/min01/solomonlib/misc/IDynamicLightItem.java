package com.min01.solomonlib.misc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface IDynamicLightItem
{
	public boolean shouldUpdateDynamicLight(Entity player, ItemStack stack);
	
	public Vec3 getDynamicLightPos(Entity player, ItemStack stack);
}
