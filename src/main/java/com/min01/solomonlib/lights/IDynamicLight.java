package com.min01.solomonlib.lights;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 * Copyright © 2024 toni (https://github.com/txnimc/SodiumDynamicLights)
 *
 * This file is derived from SodiumDynamicLights (DynamicLightSource) / LambDynLights.
 * Licensed under the MIT License. The full license text is included in README.md at
 * the repository root.
 */
public interface IDynamicLight 
{
	double getDynamicLightX();

	double getDynamicLightY();

	double getDynamicLightZ();

	Level getDynamicLightLevel();

	default boolean solomonlib$isDynamicLightEnabled()
	{
		return DynamicLights.get().containsLightSource(this);
	}
	
	@ApiStatus.Internal
	default void solomonlib$setDynamicLightEnabled(boolean enabled)
	{
		this.resetDynamicLight();
		if(enabled)
		{
			DynamicLights.get().addLightSource(this);
		}
		else
		{
			DynamicLights.get().removeLightSource(this);
		}
	}

	void resetDynamicLight();

	int getLuminance();

	void dynamicLightTick();

	boolean shouldUpdateDynamicLight();

 	@OnlyIn(Dist.CLIENT)
	boolean updateDynamicLight(@NotNull LevelRenderer renderer);

 	@OnlyIn(Dist.CLIENT)
	void scheduleTrackedChunksRebuild(@NotNull LevelRenderer renderer);
}
