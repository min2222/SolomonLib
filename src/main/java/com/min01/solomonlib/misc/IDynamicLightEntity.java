package com.min01.solomonlib.misc;

import net.minecraft.world.phys.Vec3;

public interface IDynamicLightEntity
{
	boolean shouldUpdateDynamicLight();

	Vec3 getDynamicLightPos();

	default int getDynamicLightLuminance()
	{
		return 15;
	}
}
