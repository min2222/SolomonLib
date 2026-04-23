package com.min01.solomonlib.coremod.transformer;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
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
import net.minecraftforge.coremod.api.ASMAPI;

public class RangedAttackTransformer implements ITransformer<ClassNode>
{
	private static final Logger LOGGER = LogManager.getLogger("SolomonLib/Coremod");

	private static final String RANGED_ATTACK_DESC = "(Lnet/minecraft/world/entity/LivingEntity;F)V";

	private static final String BRIDGE = "com/min01/solomonlib/gravity/GravityAPIBridge";
	private static final String OBJ_DESC = "(Ljava/lang/Object;)D";
	private static final String OBJ_D_DESC = "(Ljava/lang/Object;D)D";
	private static final String D_OBJ_DESC = "(DLjava/lang/Object;)D";

	private static final String GET_X = ASMAPI.mapMethod("m_20185_");
	private static final String GET_Y = ASMAPI.mapMethod("m_20227_");
	private static final String GET_EYE_Y = ASMAPI.mapMethod("m_20188_");
	private static final String GET_Z = ASMAPI.mapMethod("m_20189_");
	private static final String GET_DELTA_MOVEMENT = ASMAPI.mapMethod("m_20184_");
	private static final String PERFORM_RANGED_ATTACK_0 = ASMAPI.mapMethod("m_6504_");
	private static final String PERFORM_RANGED_ATTACK_1 = ASMAPI.mapMethod("m_31448_");
	private static final String PERFORM_RANGED_ATTACK_2 = ASMAPI.mapMethod("m_31457_");

	private final Set<Target> targets;

	public RangedAttackTransformer(Set<String> classNames)
	{
	    this.targets = classNames.stream().map(Target::targetClass).collect(Collectors.toSet());
	}
	
	private enum Pattern
	{
		BODY, EYE
	}

	@Override
	public ClassNode transform(ClassNode classNode, ITransformerVotingContext context)
	{
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

			this.applyPatch(method, pattern, classNode.name);
		}
		return classNode;
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

	private void applyPatch(MethodNode method, Pattern pattern, String owner)
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
						list.set(current, new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, helper, OBJ_DESC, false));
						pX = true;
					}
					else if(!pY && pattern == Pattern.BODY && GET_Y.equals(m.name) && "(D)D".equals(m.desc))
					{
						list.set(current, new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "rangedBodyTargetY", OBJ_D_DESC, false));
						pY = true;
					}
					else if(!pY && pattern == Pattern.EYE
							&& GET_EYE_Y.equals(m.name) && "()D".equals(m.desc))
					{
						list.set(current, new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "rangedEyeTargetY", OBJ_DESC, false));
						pY = true;
					}
					else if(!pZ && GET_Z.equals(m.name) && "()D".equals(m.desc))
					{
						String helper = pattern == Pattern.BODY ? "rangedBodyTargetZ" : "rangedEyeTargetZ";
						list.set(current, new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, helper, OBJ_DESC, false));
						pZ = true;
					}
					else if(!pDelta && GET_DELTA_MOVEMENT.equals(m.name) && "()Lnet/minecraft/world/phys/Vec3;".equals(m.desc) && isAload1(prevSignificant(list, current)))
					{
						list.set(current, new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "deltaMovement", "(Ljava/lang/Object;)Ljava/lang/Object;", false));
						pDelta = true;
					}
				}
				else if(!pSqrt && m.getOpcode() == Opcodes.INVOKESTATIC && "java/lang/Math".equals(m.owner) && "sqrt".equals(m.name) && "(D)D".equals(m.desc))
				{
					list.insertBefore(current, new VarInsnNode(Opcodes.ALOAD, 1));
					list.set(current, new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "rangedSqrt", D_OBJ_DESC, false));
					pSqrt = true;
				}
			}

			if(pX && pY && pZ && pSqrt)
			{
				break;
			}
			current = next;
		}

		boolean ok = pX && pY && pZ && pSqrt;
		LOGGER.log(ok ? Level.INFO : Level.WARN, "[SolomonLib/Coremod] RangedAttack({}) {} {} (x={},y={},z={},sqrt={},delta={})", pattern, ok ? "OK" : "FAIL", owner, pX, pY, pZ, pSqrt, pDelta);
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
