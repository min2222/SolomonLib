package com.min01.solomonlib.patcher;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.mincl.mixinglobal.spi.GlobalClassNodePatcher;
import io.github.mincl.mixinglobal.spi.GlobalClassNodePatcherProvider;

public class SolomonGlobalPatcherProvider implements GlobalClassNodePatcherProvider
{
	private static final Logger LOGGER = LogManager.getLogger("SolomonLib/GlobalPatcher");

	@Override
	public List<GlobalClassNodePatcher> patchers()
	{
		LOGGER.info("[SolomonLib/GlobalPatcher] provider loaded");
		return List.of(new SolomonGlobalClassNodePatcher());
	}
}
