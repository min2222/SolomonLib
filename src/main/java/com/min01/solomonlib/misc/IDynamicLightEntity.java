package com.min01.solomonlib.misc;

import net.minecraft.world.phys.Vec3;

public interface IDynamicLightEntity 
{
	public boolean shouldUpdateDynamicLight();
	
	public Vec3 getDynamicLightPos();
}
