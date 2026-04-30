package com.min01.solomonlib.patcher;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraftforge.coremod.api.ASMAPI;

public class ProjectileLivingConstructorPatcher
{
	private static final String ABSTRACT_ARROW = "net/minecraft/world/entity/projectile/AbstractArrow";
	private static final String THROWABLE_PROJECTILE = "net/minecraft/world/entity/projectile/ThrowableProjectile";
	private static final String INIT_DESC = "(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V";
	private static final String LIVING_D = "(Lnet/minecraft/world/entity/LivingEntity;)D";
	private static final String GET_X = ASMAPI.mapMethod("m_20185_");
	private static final String GET_Z = ASMAPI.mapMethod("m_20189_");
	private static final String GET_EYE_Y = ASMAPI.mapMethod("m_20188_");

	public int patch(ClassNode classNode)
	{
		if(classNode == null || classNode.name == null)
		{
			return 0;
		}
		if(!ABSTRACT_ARROW.equals(classNode.name) && !THROWABLE_PROJECTILE.equals(classNode.name))
		{
			return 0;
		}
		int total = 0;
		for(MethodNode method : classNode.methods)
		{
			if(!"<init>".equals(method.name) || !INIT_DESC.equals(method.desc))
			{
				continue;
			}
			total += this.patchInit(method);
		}
		return total;
	}

	private int patchInit(MethodNode method)
	{
		InsnList list = method.instructions;
		int total = 0;
		total += this.replaceEyeYMinusOffset(list);
		total += this.replaceShooterGetter(list, GET_X);
		total += this.replaceShooterGetter(list, GET_Z);
		return total;
	}

	private int replaceEyeYMinusOffset(InsnList list)
	{
		for(AbstractInsnNode cur = list.getFirst(); cur != null; cur = cur.getNext())
		{
			if(cur.getOpcode() != Opcodes.ALOAD || !(cur instanceof VarInsnNode av) || av.var != 2)
			{
				continue;
			}
			AbstractInsnNode n1 = nextSignificant(cur.getNext());
			if(!(n1 instanceof MethodInsnNode m) || m.getOpcode() != Opcodes.INVOKEVIRTUAL || !GET_EYE_Y.equals(m.name) || !"()D".equals(m.desc))
			{
				continue;
			}
			AbstractInsnNode n2 = nextSignificant(n1.getNext());
			AbstractInsnNode constInsn = n2;
			AbstractInsnNode afterConst = n2;
			if(n2 != null && n2.getOpcode() == Opcodes.LDC && n2 instanceof LdcInsnNode ldcN2 && ldcN2.cst instanceof Float)
			{
				AbstractInsnNode n3 = nextSignificant(n2.getNext());
				if(n3 != null && n3.getOpcode() == Opcodes.F2D)
				{
					constInsn = n2;
					afterConst = n3;
				}
			}
			if(!isApproxEyeYOffsetConstant(constInsn))
			{
				continue;
			}
			AbstractInsnNode dsub = nextSignificant(afterConst.getNext());
			if(dsub == null || dsub.getOpcode() != Opcodes.DSUB)
			{
				continue;
			}
			InsnList rep = new InsnList();
			rep.add(new VarInsnNode(Opcodes.ALOAD, 2));
			rep.add(new MethodInsnNode(Opcodes.INVOKESTATIC, PatcherConstants.GRAVITY_API, "projectileSpawnY", LIVING_D, false));
			list.insertBefore(cur, rep);
			AbstractInsnNode node = cur;
			while(node != dsub)
			{
				AbstractInsnNode nx = node.getNext();
				list.remove(node);
				node = nx;
			}
			list.remove(dsub);
			return 1;
		}
		return 0;
	}

	private int replaceShooterGetter(InsnList list, String mappedName)
	{
		for(AbstractInsnNode cur = list.getFirst(); cur != null; cur = cur.getNext())
		{
			if(cur.getOpcode() != Opcodes.ALOAD || !(cur instanceof VarInsnNode av) || av.var != 2)
			{
				continue;
			}
			AbstractInsnNode n1 = nextSignificant(cur.getNext());
			if(!(n1 instanceof MethodInsnNode m) || m.getOpcode() != Opcodes.INVOKEVIRTUAL || !mappedName.equals(m.name) || !"()D".equals(m.desc))
			{
				continue;
			}
			String helper = GET_X.equals(mappedName) ? "projectileSpawnX" : "projectileSpawnZ";
			list.set(n1, new MethodInsnNode(Opcodes.INVOKESTATIC, PatcherConstants.GRAVITY_API, helper, LIVING_D, false));
			return 1;
		}
		return 0;
	}

	private static boolean isApproxEyeYOffsetConstant(AbstractInsnNode insn)
	{
		if(insn == null || insn.getOpcode() != Opcodes.LDC || !(insn instanceof LdcInsnNode ldc))
		{
			return false;
		}
		if(ldc.cst instanceof Double d)
		{
			return Math.abs(d - 0.1D) < 1.0E-4D || Math.abs(d - 0.10000000149011612D) < 1.0E-4D;
		}
		if(ldc.cst instanceof Float f)
		{
			return Math.abs(f - 0.1F) < 1.0E-3F || Math.abs(f - 0.100000001F) < 1.0E-3F;
		}
		return false;
	}

	private static AbstractInsnNode nextSignificant(AbstractInsnNode from)
	{
		AbstractInsnNode n = from;
		while(n != null)
		{
			int t = n.getType();
			if(t != AbstractInsnNode.LABEL && t != AbstractInsnNode.LINE && t != AbstractInsnNode.FRAME)
			{
				return n;
			}
			n = n.getNext();
		}
		return null;
	}
}
