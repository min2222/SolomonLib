package com.min01.solomonlib.patcher;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraftforge.coremod.api.ASMAPI;

public class RangedAttackPatcher implements ClassNodePatcher
{
	private static final String RANGED_ATTACK_DESC = "(Lnet/minecraft/world/entity/LivingEntity;F)V";

	private static final String GRAVITY_API = "com/min01/solomonlib/gravity/GravityAPI";
	private static final String LIVING_DESC = "(Lnet/minecraft/world/entity/LivingEntity;)D";
	private static final String LIVING_D_DESC = "(Lnet/minecraft/world/entity/LivingEntity;D)D";
	private static final String D_LIVING_DESC = "(DLnet/minecraft/world/entity/LivingEntity;)D";
	private static final String DELTA_MOVEMENT_DESC = "(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/phys/Vec3;";

	private static final String GET_X = ASMAPI.mapMethod("m_20185_");
	private static final String GET_Y = ASMAPI.mapMethod("m_20227_");
	private static final String GET_EYE_Y = ASMAPI.mapMethod("m_20188_");
	private static final String GET_Z = ASMAPI.mapMethod("m_20189_");
	private static final String GET_DELTA_MOVEMENT = ASMAPI.mapMethod("m_20184_");
	private static final String PERFORM_RANGED_ATTACK_0 = ASMAPI.mapMethod("m_6504_");
	private static final String PERFORM_RANGED_ATTACK_1 = ASMAPI.mapMethod("m_31448_");
	private static final String PERFORM_RANGED_ATTACK_2 = ASMAPI.mapMethod("m_31457_");

	private enum Pattern
	{
		BODY, EYE
	}

	@Override
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

			Pattern pattern = this.detectPattern(method);
			if(pattern == null)
			{
				continue;
			}

			total += this.applyPatch(method, pattern);
		}
		return total;
	}

	private boolean isPerformRangedAttack(String methodName)
	{
		return PERFORM_RANGED_ATTACK_0.equals(methodName) || PERFORM_RANGED_ATTACK_1.equals(methodName) || PERFORM_RANGED_ATTACK_2.equals(methodName) || "performRangedAttack".equals(methodName);
	}

	private Pattern detectPattern(MethodNode method)
	{
		boolean hasGetYD = false;
		boolean hasVoidD = false;

		for(var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
		{
			if(!(insn instanceof MethodInsnNode m))
			{
				continue;
			}
			if(m.getOpcode() != Opcodes.INVOKEVIRTUAL)
			{
				continue;
			}

			if("(D)D".equals(m.desc))
			{
				hasGetYD = true;
			}
			if("()D".equals(m.desc))
			{
				hasVoidD = true;
			}
		}

		if(hasGetYD)
		{
			return Pattern.BODY;
		}
		if(hasVoidD)
		{
			return Pattern.EYE;
		}
		return null;
	}

	private int applyPatch(MethodNode method, Pattern pattern)
	{
		boolean pX = false;
		boolean pY = false;
		boolean pZ = false;
		boolean pSqrt = false;
		boolean pDelta = false;
		InsnList list = method.instructions;
		var current = list.getFirst();

		while(current != null)
		{
			var next = current.getNext();

			if(current instanceof MethodInsnNode m)
			{
				if(m.getOpcode() == Opcodes.INVOKEVIRTUAL)
				{
					if(!pX && GET_X.equals(m.name) && "()D".equals(m.desc))
					{
						String helper = pattern == Pattern.BODY ? "rangedBodyTargetX" : "rangedEyeTargetX";
						list.set(current, new MethodInsnNode(Opcodes.INVOKESTATIC, GRAVITY_API, helper, LIVING_DESC, false));
						pX = true;
					}
					else if(!pY && pattern == Pattern.BODY && GET_Y.equals(m.name) && "(D)D".equals(m.desc))
					{
						list.set(current, new MethodInsnNode(Opcodes.INVOKESTATIC, GRAVITY_API, "rangedBodyTargetY", LIVING_D_DESC, false));
						pY = true;
					}
					else if(!pY && pattern == Pattern.EYE && GET_EYE_Y.equals(m.name) && "()D".equals(m.desc))
					{
						list.set(current, new MethodInsnNode(Opcodes.INVOKESTATIC, GRAVITY_API, "rangedEyeTargetY", LIVING_DESC, false));
						pY = true;
					}
					else if(!pZ && GET_Z.equals(m.name) && "()D".equals(m.desc))
					{
						String helper = pattern == Pattern.BODY ? "rangedBodyTargetZ" : "rangedEyeTargetZ";
						list.set(current, new MethodInsnNode(Opcodes.INVOKESTATIC, GRAVITY_API, helper, LIVING_DESC, false));
						pZ = true;
					}
					else if(!pDelta && GET_DELTA_MOVEMENT.equals(m.name) && "()Lnet/minecraft/world/phys/Vec3;".equals(m.desc) && isAload1(prevSignificant(list, current)))
					{
						list.set(current, new MethodInsnNode(Opcodes.INVOKESTATIC, GRAVITY_API, "deltaMovement", DELTA_MOVEMENT_DESC, false));
						pDelta = true;
					}
				}
				else if(!pSqrt && m.getOpcode() == Opcodes.INVOKESTATIC && "java/lang/Math".equals(m.owner) && "sqrt".equals(m.name) && "(D)D".equals(m.desc))
				{
					list.insertBefore(current, new VarInsnNode(Opcodes.ALOAD, 1));
					list.set(current, new MethodInsnNode(Opcodes.INVOKESTATIC, GRAVITY_API, "rangedSqrt", D_LIVING_DESC, false));
					pSqrt = true;
				}
			}

			if(pX && pY && pZ && pSqrt)
			{
				break;
			}
			current = next;
		}
		return (pX ? 1 : 0) + (pY ? 1 : 0) + (pZ ? 1 : 0) + (pSqrt ? 1 : 0) + (pDelta ? 1 : 0);
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
