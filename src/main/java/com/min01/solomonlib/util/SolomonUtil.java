package com.min01.solomonlib.util;

import java.util.ArrayList;
import java.util.List;

import com.min01.solomonlib.world.SolomonSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class SolomonUtil 
{
	public static final String BTA_MODID = "beyondtheabyss";
	
 	public static final ResourceKey<Level> MIRRORED_CITY = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(BTA_MODID, "mirrored_city"));
	public static final List<BlockPos> GRAVITY_BLOCKS = new ArrayList<>();
	
	public static void setBlockUpsideDown(Level level, BlockPos pos)
	{
		SolomonSavedData data = SolomonSavedData.get(level);
		if(data != null)
		{
			data.setBlockUpsideDown(pos);
		}
		else
		{
			GRAVITY_BLOCKS.add(pos);
		}
	}
	
	public static void removeUpsideDownBlocks(Level level)
	{
		SolomonSavedData data = SolomonSavedData.get(level);
		if(data != null)
		{
			data.getUpsideDownBlocks().removeIf(t -> level.getBlockState(t).isAir());
		}
		else
		{
			GRAVITY_BLOCKS.removeIf(t -> level.getBlockState(t).isAir());
		}
	}
	
	public static void removeUpsideDownBlock(Level level, BlockPos pos)
	{
		SolomonSavedData data = SolomonSavedData.get(level);
		if(data != null)
		{
			data.getUpsideDownBlocks().removeIf(t -> t.equals(pos));
		}
		else
		{
			GRAVITY_BLOCKS.removeIf(t -> t.equals(pos));
		}
	}
	
	public static boolean isBlockUpsideDown(Level level, BlockPos pos)
	{
		SolomonSavedData data = SolomonSavedData.get(level);
		if(data != null)
		{
			return data.isBlockUpsideDown(level, pos);
		}
		return GRAVITY_BLOCKS.contains(pos);
	}
}
