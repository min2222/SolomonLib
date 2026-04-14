package com.min01.solomonlib.coremod.transformer;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraftforge.coremod.api.ASMAPI;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;

public class FallingBlockGravityTransformer implements ITransformer<ClassNode>
{
	private static final Logger LOGGER = LogManager.getLogger("SolomonLib/Coremod");

	private static final String TARGET_INTERNAL = "net/minecraft/world/entity/item/FallingBlockEntity";
	private static final String VEC3_INTERNAL = "net/minecraft/world/phys/Vec3";
	private static final String BRIDGE = "com/min01/solomonlib/gravity/GravityAPIBridge";
	private static final String BRIDGE_DESC = "(Ljava/lang/Object;DDDLjava/lang/Object;)Ljava/lang/Object;";
	private static final String VEC3_ADD = ASMAPI.mapMethod("m_82520_");
	private static final String TICK_NAME = ASMAPI.mapMethod("m_8119_");

	@Override
	public ClassNode transform(ClassNode classNode, ITransformerVotingContext context)
	{
		if(!TARGET_INTERNAL.equals(classNode.name))
		{
			return classNode;
		}
		for(MethodNode method : classNode.methods)
		{
			if(!"()V".equals(method.desc) || !TICK_NAME.equals(method.name))
			{
				continue;
			}
			if(patchTick(method))
			{
				LOGGER.debug("[SolomonLib/Coremod] FallingBlockGravity OK {}", classNode.name);
			}
			else
			{
				LOGGER.warn("[SolomonLib/Coremod] FallingBlockGravity FAIL {}", classNode.name);
			}
			break;
		}
		return classNode;
	}

	private boolean patchTick(MethodNode method)
	{
		AbstractInsnNode cur = method.instructions.getFirst();
		while(cur != null)
		{
			AbstractInsnNode next = cur.getNext();
			if(cur instanceof MethodInsnNode m
					&& m.getOpcode() == Opcodes.INVOKEVIRTUAL
					&& VEC3_INTERNAL.equals(m.owner)
					&& VEC3_ADD.equals(m.name)
					&& "(DDD)Lnet/minecraft/world/phys/Vec3;".equals(m.desc))
			{
				method.instructions.insertBefore(m, new VarInsnNode(Opcodes.ALOAD, 0));
				MethodInsnNode bridgeCall = new MethodInsnNode(
					Opcodes.INVOKESTATIC, BRIDGE, "addWithGravity", BRIDGE_DESC, false);
				method.instructions.set(m, bridgeCall);
				method.instructions.insertBefore(bridgeCall.getNext(),
					new TypeInsnNode(Opcodes.CHECKCAST, VEC3_INTERNAL));
				return true;
			}
			cur = next;
		}
		return false;
	}

	@Override
	public TransformerVoteResult castVote(ITransformerVotingContext context)
	{
		return TransformerVoteResult.YES;
	}

	@Override
	public Set<Target> targets()
	{
		return Set.of(Target.targetClass(TARGET_INTERNAL));
	}
}
