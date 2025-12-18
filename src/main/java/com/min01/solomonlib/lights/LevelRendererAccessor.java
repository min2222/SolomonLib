package com.min01.solomonlib.lights;

public interface LevelRendererAccessor
{
	void scheduleChunkRebuild(int x, int y, int z, boolean important);
}
