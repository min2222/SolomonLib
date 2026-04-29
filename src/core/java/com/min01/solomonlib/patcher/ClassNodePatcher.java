package com.min01.solomonlib.patcher;

import org.objectweb.asm.tree.ClassNode;

public interface ClassNodePatcher
{
	int patch(ClassNode classNode);
}
