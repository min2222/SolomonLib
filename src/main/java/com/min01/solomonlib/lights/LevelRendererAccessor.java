package com.min01.solomonlib.lights;

/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of SodiumDynamicLights.
 *
 * Licensed under the MIT License. For more information,
 * see the LICENSE file.
 */

public interface LevelRendererAccessor
{
	void scheduleChunkRebuild(int x, int y, int z, boolean important);
}
