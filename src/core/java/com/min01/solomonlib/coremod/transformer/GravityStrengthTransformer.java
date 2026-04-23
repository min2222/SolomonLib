package com.min01.solomonlib.coremod.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;

public class GravityStrengthTransformer implements ITransformer<ClassNode>
{
	private static final Logger LOGGER = LogManager.getLogger("SolomonLib/Coremod");

	private static final String BRIDGE = "com/min01/solomonlib/gravity/GravityAPIBridge";
	
	private final Set<Target> targets;

	public GravityStrengthTransformer(Set<String> classNames)
	{
	    this.targets = classNames.stream().map(Target::targetClass).collect(Collectors.toSet());
	}

	@Override
	public ClassNode transform(ClassNode classNode, ITransformerVotingContext context)
	{
		for(MethodNode method : classNode.methods)
		{
			if(!"getGravity".equals(method.name) || !"()F".equals(method.desc))
			{
				continue;
			}
			this.patchGetGravity(method, classNode.name);
			break;
		}
		return classNode;
	}

	private void patchGetGravity(MethodNode method, String owner)
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
			LOGGER.warn("[SolomonLib/Coremod] GravityStrength A3 FAIL {} — no FRETURN found", owner);
			return;
		}

		InsnList list = method.instructions;
		for(AbstractInsnNode fret : freturns)
		{
			InsnList inject = new InsnList();
			inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "scaleF", "(FLjava/lang/Object;)F", false));
			list.insertBefore(fret, inject);
		}

		LOGGER.info("[SolomonLib/Coremod] GravityStrength A3 OK {} — {} FRETURN(s) patched", owner, freturns.size());
	}

	@Override
	public TransformerVoteResult castVote(ITransformerVotingContext context)
	{
		return TransformerVoteResult.YES;
	}

	@Override
	public Set<Target> targets()
	{
	    return this.targets;
	}
}
