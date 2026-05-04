package com.min01.solomonlib.patcher;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraftforge.coremod.api.ASMAPI;

public class RangedAttackPatcher
{
	private static final String RANGED_ATTACK_DESC = "(Lnet/minecraft/world/entity/LivingEntity;F)V";
	private static final String LIVING_DESC = "(Lnet/minecraft/world/entity/LivingEntity;)D";
	private static final String LIVING_D_DESC = "(Lnet/minecraft/world/entity/LivingEntity;D)D";
	private static final String D_LIVING_DESC = "(DLnet/minecraft/world/entity/LivingEntity;)D";
	private static final String DELTA_MOVEMENT_DESC = "(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/phys/Vec3;";
	private static final String GET_X = ASMAPI.mapMethod("m_20185_");
	private static final String GET_Z = ASMAPI.mapMethod("m_20189_");
	private static final String GET_Y = ASMAPI.mapMethod("m_20227_");
	private static final String GET_EYE_Y = ASMAPI.mapMethod("m_20188_");
	private static final String GET_DELTA_MOVEMENT = ASMAPI.mapMethod("m_20184_");
	private static final String PERFORM_RANGED_ATTACK_0 = ASMAPI.mapMethod("m_6504_");
	private static final String PERFORM_RANGED_ATTACK_1 = ASMAPI.mapMethod("m_31448_");
	private static final String PERFORM_RANGED_ATTACK_2 = ASMAPI.mapMethod("m_31457_");

	private enum AimMode
	{
		BODY,
		EYE
	}

	public int patch(ClassNode classNode)
	{
		int total = 0;
		for(MethodNode method : classNode.methods)
		{
			if(!RANGED_ATTACK_DESC.equals(method.desc))
			{
				continue;
			}
			if(!this.isPerformRangedAttack(method.name))
			{
				continue;
			}
			if(!this.hasRangedAimSqrt(method))
			{
				continue;
			}
			total += this.applyPatch(method);
		}
		return total;
	}

	private boolean isPerformRangedAttack(String methodName)
	{
		return PERFORM_RANGED_ATTACK_0.equals(methodName) || PERFORM_RANGED_ATTACK_1.equals(methodName) || PERFORM_RANGED_ATTACK_2.equals(methodName) || "performRangedAttack".equals(methodName);
	}

	private boolean hasRangedAimSqrt(MethodNode method)
	{
		for(AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
		{
			if(insn instanceof MethodInsnNode m && m.getOpcode() == Opcodes.INVOKESTATIC && "java/lang/Math".equals(m.owner) && "sqrt".equals(m.name) && "(D)D".equals(m.desc))
			{
				return true;
			}
		}
		return false;
	}

	private AimMode detectAimMode(MethodNode method)
	{
		for(AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
		{
			if(!(insn instanceof MethodInsnNode m))
			{
				continue;
			}
			int op = m.getOpcode();
			if(op != Opcodes.INVOKEVIRTUAL && op != Opcodes.INVOKEINTERFACE)
			{
				continue;
			}
			if(GET_Y.equals(m.name) && "(D)D".equals(m.desc))
			{
				return AimMode.BODY;
			}
		}
		return AimMode.EYE;
	}

	private int applyPatch(MethodNode method)
	{
		AimMode mode = this.detectAimMode(method);
		int total = 0;
		int ordX = 0;
		int ordZ = 0;
		int ordYScaled = 0;
		int ordEyeY = 0;
		InsnList list = method.instructions;
		for(AbstractInsnNode cur = list.getFirst(); cur != null; cur = cur.getNext())
		{
			if(!(cur instanceof MethodInsnNode m))
			{
				continue;
			}
			int op = m.getOpcode();
			if(op != Opcodes.INVOKEVIRTUAL && op != Opcodes.INVOKEINTERFACE)
			{
				continue;
			}
			if(GET_X.equals(m.name) && "()D".equals(m.desc))
			{
				if(ordX++ != 0)
				{
					continue;
				}
				String helper = mode == AimMode.BODY ? "rangedBodyTargetX" : "rangedEyeTargetX";
				this.replaceVirtualWithGuardedStatic(list, m, new MethodInsnNode(Opcodes.INVOKESTATIC, PatcherConstants.GRAVITY_API, helper, LIVING_DESC, false));
				total++;
				continue;
			}
			if(GET_Z.equals(m.name) && "()D".equals(m.desc))
			{
				if(ordZ++ != 0)
				{
					continue;
				}
				String helper = mode == AimMode.BODY ? "rangedBodyTargetZ" : "rangedEyeTargetZ";
				this.replaceVirtualWithGuardedStatic(list, m, new MethodInsnNode(Opcodes.INVOKESTATIC, PatcherConstants.GRAVITY_API, helper, LIVING_DESC, false));
				total++;
				continue;
			}
			if(mode == AimMode.BODY && GET_Y.equals(m.name) && "(D)D".equals(m.desc))
			{
				if(ordYScaled++ != 0)
				{
					continue;
				}
				list.set(cur, new MethodInsnNode(Opcodes.INVOKESTATIC, PatcherConstants.GRAVITY_API, "rangedBodyTargetY", LIVING_D_DESC, false));
				total++;
				continue;
			}
			if(mode == AimMode.EYE && GET_EYE_Y.equals(m.name) && "()D".equals(m.desc))
			{
				if(ordEyeY++ != 0)
				{
					continue;
				}
				this.replaceVirtualWithGuardedStatic(list, m, new MethodInsnNode(Opcodes.INVOKESTATIC, PatcherConstants.GRAVITY_API, "rangedEyeTargetY", LIVING_DESC, false));
				total++;
			}
		}

		total += this.patchSqrtAndDeltaOnce(method);
		return total;
	}

	private int patchSqrtAndDeltaOnce(MethodNode method)
	{
		int total = 0;
		InsnList list = method.instructions;
		for(AbstractInsnNode cur = list.getFirst(); cur != null; cur = cur.getNext())
		{
			if(!(cur instanceof MethodInsnNode m))
			{
				continue;
			}
			if(m.getOpcode() == Opcodes.INVOKEVIRTUAL && GET_DELTA_MOVEMENT.equals(m.name) && "()Lnet/minecraft/world/phys/Vec3;".equals(m.desc) && isAload1(prevSignificant(list, cur)))
			{
				this.replaceVirtualWithGuardedStatic(list, m, new MethodInsnNode(Opcodes.INVOKESTATIC, PatcherConstants.GRAVITY_API, "deltaMovement", DELTA_MOVEMENT_DESC, false));
				total++;
			}
			else if(m.getOpcode() == Opcodes.INVOKESTATIC && "java/lang/Math".equals(m.owner) && "sqrt".equals(m.name) && "(D)D".equals(m.desc))
			{
				LabelNode lblMath = new LabelNode();
				LabelNode lblAfter = new LabelNode();
				InsnList sqrtGuard = new InsnList();
				sqrtGuard.add(new VarInsnNode(Opcodes.ALOAD, 1));
				sqrtGuard.add(new InsnNode(Opcodes.DUP));
				sqrtGuard.add(new TypeInsnNode(Opcodes.INSTANCEOF, PatcherConstants.ENTITY_INTERNAL));
				sqrtGuard.add(new JumpInsnNode(Opcodes.IFEQ, lblMath));
				sqrtGuard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, PatcherConstants.GRAVITY_API, "rangedSqrt", D_LIVING_DESC, false));
				sqrtGuard.add(new JumpInsnNode(Opcodes.GOTO, lblAfter));
				sqrtGuard.add(lblMath);
				sqrtGuard.add(new InsnNode(Opcodes.POP));
				sqrtGuard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Math", "sqrt", "(D)D", false));
				sqrtGuard.add(lblAfter);
				list.insertBefore(m, sqrtGuard);
				list.remove(m);
				total++;
			}
		}
		return total;
	}

	private void replaceVirtualWithGuardedStatic(InsnList list, MethodInsnNode original, MethodInsnNode staticReplacement)
	{
		LabelNode lblOrig = new LabelNode();
		LabelNode lblEnd = new LabelNode();
		InsnList inject = new InsnList();
		inject.add(new InsnNode(Opcodes.DUP));
		inject.add(new TypeInsnNode(Opcodes.INSTANCEOF, PatcherConstants.ENTITY_INTERNAL));
		inject.add(new JumpInsnNode(Opcodes.IFEQ, lblOrig));
		inject.add(staticReplacement);
		inject.add(new JumpInsnNode(Opcodes.GOTO, lblEnd));
		inject.add(lblOrig);
		inject.add(new MethodInsnNode(original.getOpcode(), original.owner, original.name, original.desc, original.itf));
		inject.add(lblEnd);
		list.insertBefore(original, inject);
		list.remove(original);
	}

	private static boolean isAload1(AbstractInsnNode insn)
	{
		return insn instanceof VarInsnNode v && v.getOpcode() == Opcodes.ALOAD && v.var == 1;
	}

	private static AbstractInsnNode prevSignificant(InsnList list, AbstractInsnNode from)
	{
		AbstractInsnNode node = from.getPrevious();
		while(node != null)
		{
			int t = node.getType();
			if(t != AbstractInsnNode.LABEL && t != AbstractInsnNode.LINE)
			{
				return node;
			}
			node = node.getPrevious();
		}
		return null;
	}
}
