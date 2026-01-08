package com.min01.solomonlib.util;

import java.util.function.Consumer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;

public class SolomonUtil 
{
	public static final String BTA_MODID = "beyondtheabyss";
	
 	public static final ResourceKey<Level> MIRRORED_CITY = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(BTA_MODID, "mirrored_city"));
	
	public static void getClientLevel(Consumer<Level> consumer)
	{
		LogicalSidedProvider.CLIENTWORLD.get(LogicalSide.CLIENT).filter(ClientLevel.class::isInstance).ifPresent(level -> 
		{
			consumer.accept(level);
		});
	}
	
	public static boolean isBlockUpsideDown(Level level, BlockPos pos)
	{
		return level.dimension() == MIRRORED_CITY && pos.getY() >= 150;
	}
}
