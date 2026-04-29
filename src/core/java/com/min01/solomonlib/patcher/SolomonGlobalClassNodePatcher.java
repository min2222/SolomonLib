package com.min01.solomonlib.patcher;

import org.objectweb.asm.tree.ClassNode;

import io.github.mincl.mixinglobal.spi.GlobalClassNodePatcher;

public class SolomonGlobalClassNodePatcher implements GlobalClassNodePatcher
{
	private final SolomonGlobalPatcher delegate = new SolomonGlobalPatcher();

	@Override
	public String id()
	{
		return "solomonlib:global_patcher";
	}

	@Override
	public int patch(ClassNode classNode)
	{
		return this.delegate.patch(classNode);
	}
}
