package com.min01.solomonlib.misc;

import com.min01.solomonlib.SolomonLib;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class SolomonTags
{
	public static final TagKey<Block> NON_CLIMBABLE = createBlock("non_climbable");
	
	private static TagKey<Block> createBlock(String name) 
	{
		return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(SolomonLib.MODID, name));
	}
}
