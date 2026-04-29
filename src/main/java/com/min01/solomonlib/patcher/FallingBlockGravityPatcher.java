package com.min01.solomonlib.patcher;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraftforge.coremod.api.ASMAPI;

public class FallingBlockGravityPatcher implements ClassNodePatcher
{
	private static final String TARGET_INTERNAL = "net/minecraft/world/entity/item/FallingBlockEntity";
	private static final String VEC3_INTERNAL = "net/minecraft/world/phys/Vec3";
	private static final String GRAVITY_API = "com/min01/solomonlib/gravity/GravityAPI";
	private static final String ADD_WITH_GRAVITY_DESC = "(Lnet/minecraft/world/phys/Vec3;DDDLnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/Vec3;";

	private static final class Names
	{
		static final String VEC3_ADD = ASMAPI.mapMethod("m_82520_");
		static final String TICK_NAME = ASMAPI.mapMethod("m_8119_");
	}

	@Override
	public int patch(ClassNode classNode)
	{
		if(!TARGET_INTERNAL.equals(classNode.name))
		{
			return 0;
		}
		for(MethodNode method : classNode.methods)
		{
			if(!"()V".equals(method.desc) || !Names.TICK_NAME.equals(method.name))
			{
				continue;
			}
			return this.patchTick(method) ? 1 : 0;
		}
		return 0;
	}

	private boolean patchTick(MethodNode method)
	{
		AbstractInsnNode cur = method.instructions.getFirst();
		while(cur != null)
		{
			AbstractInsnNode next = cur.getNext();
			if(cur instanceof MethodInsnNode m && m.getOpcode() == Opcodes.INVOKEVIRTUAL && VEC3_INTERNAL.equals(m.owner) && Names.VEC3_ADD.equals(m.name) && "(DDD)Lnet/minecraft/world/phys/Vec3;".equals(m.desc))
			{
				method.instructions.insertBefore(m, new VarInsnNode(Opcodes.ALOAD, 0));
				MethodInsnNode bridgeCall = new MethodInsnNode(Opcodes.INVOKESTATIC, GRAVITY_API, "addWithGravity", ADD_WITH_GRAVITY_DESC, false);
				method.instructions.set(m, bridgeCall);
				method.instructions.insertBefore(bridgeCall.getNext(), new TypeInsnNode(Opcodes.CHECKCAST, VEC3_INTERNAL));
				return true;
			}
			cur = next;
		}
		return false;
	}
}
