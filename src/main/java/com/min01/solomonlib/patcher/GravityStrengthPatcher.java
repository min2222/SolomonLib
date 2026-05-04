package com.min01.solomonlib.patcher;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class GravityStrengthPatcher
{
	private static final String SCALE_F_DESC = "(FLnet/minecraft/world/entity/Entity;)F";

	public int patch(ClassNode classNode)
	{
		for(MethodNode method : classNode.methods)
		{
			if(!"getGravity".equals(method.name) || !"()F".equals(method.desc))
			{
				continue;
			}
			if((method.access & Opcodes.ACC_STATIC) != 0)
			{
				continue;
			}
			return this.patchGetGravity(method);
		}
		return 0;
	}

	private int patchGetGravity(MethodNode method)
	{
		List<AbstractInsnNode> freturns = new ArrayList<>();
		for(AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
		{
			if(insn.getOpcode() == Opcodes.FRETURN)
			{
				freturns.add(insn);
			}
		}

		if(freturns.isEmpty())
		{
			return 0;
		}

		InsnList list = method.instructions;
		for(AbstractInsnNode fret : freturns)
		{
			LabelNode lblSkip = new LabelNode();
			LabelNode lblEnd = new LabelNode();
			InsnList inject = new InsnList();
			inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			inject.add(new TypeInsnNode(Opcodes.INSTANCEOF, PatcherConstants.ENTITY_INTERNAL));
			inject.add(new JumpInsnNode(Opcodes.IFEQ, lblSkip));
			inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, PatcherConstants.GRAVITY_API, "scaleF", SCALE_F_DESC, false));
			inject.add(new JumpInsnNode(Opcodes.GOTO, lblEnd));
			inject.add(lblSkip);
			inject.add(lblEnd);
			list.insertBefore(fret, inject);
		}
		return freturns.size();
	}
}
