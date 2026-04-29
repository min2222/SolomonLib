package com.min01.solomonlib.patcher;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;

public class SolomonGlobalPatcher implements ClassNodePatcher
{
	private final List<ClassNodePatcher> patchers;

	public SolomonGlobalPatcher()
	{
		this.patchers = List.of(
			new EyePositionPatcher(),
			new RangedAttackPatcher(),
			new GravityStrengthPatcher(),
			new FallingBlockGravityPatcher()
		);
	}

	@Override
	public int patch(ClassNode classNode)
	{
		int total = 0;
		for(ClassNodePatcher patcher : this.patchers)
		{
			total += patcher.patch(classNode);
		}
		return total;
	}
}
