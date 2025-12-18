package com.min01.solomonlib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class SolomonUtil 
{
	public static final String BTA_MODID = "beyondtheabyss";
	
 	public static final ResourceKey<Level> MIRRORED_CITY = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(BTA_MODID, "mirrored_city"));
 	
	public static boolean isBlockUpsideDown(BlockPos pos, Level level)
	{
		if(level != null)
		{
			return level.dimension() == MIRRORED_CITY && pos.getY() >= 150;
		}
		return false;
	}
	
	public static boolean isUpsideDown(Entity entity)
	{
		return entity.level.dimension() == MIRRORED_CITY && entity.getY() >= 150;
	}
}
