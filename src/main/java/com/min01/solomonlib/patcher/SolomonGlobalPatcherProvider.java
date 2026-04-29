package com.min01.solomonlib.patcher;

import java.util.List;

import io.github.mincl.mixinglobal.spi.GlobalClassNodePatcher;
import io.github.mincl.mixinglobal.spi.GlobalClassNodePatcherProvider;

public class SolomonGlobalPatcherProvider implements GlobalClassNodePatcherProvider
{
	@Override
	public List<GlobalClassNodePatcher> patchers()
	{
		return List.of(new SolomonGlobalClassNodePatcher());
	}
}
