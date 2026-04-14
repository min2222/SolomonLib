package com.min01.solomonlib.lights;

/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 * Copyright © 2024 toni (https://github.com/txnimc/SodiumDynamicLights)
 *
 * This file is derived from SodiumDynamicLights / LambDynLights.
 * Licensed under the MIT License. The full license text is included in README.md at
 * the repository root.
 */

public interface LevelRendererAccessor
{
	void scheduleChunkRebuild(int x, int y, int z, boolean important);
}
