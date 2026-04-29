package com.min01.solomonlib.patcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraftforge.coremod.api.ASMAPI;

public class EyePositionPatcher implements ClassNodePatcher
{
	private static final String GRAVITY_API = "com/min01/solomonlib/gravity/GravityAPI";
	private static final String ENTITY_DESC = "(Lnet/minecraft/world/entity/Entity;)D";

	private static final String GET_X = ASMAPI.mapMethod("m_20185_");
	private static final String GET_Y = ASMAPI.mapMethod("m_20186_");
	private static final String GET_Z = ASMAPI.mapMethod("m_20189_");
	private static final String FIELD_X = ASMAPI.mapField("f_46013_");
	private static final String FIELD_Y = ASMAPI.mapField("f_46014_");
	private static final String FIELD_Z = ASMAPI.mapField("f_46015_");

	private enum Axis
	{
		X, Y, Z
	}

	private enum Origin
	{
		THIS, OTHER, UNKNOWN
	}

	private record AxisOrigin(Axis axis, Origin origin)
	{
		static final AxisOrigin UNKNOWN = new AxisOrigin(null, Origin.UNKNOWN);

		boolean isKnown()
		{
			return origin != Origin.UNKNOWN;
		}
	}

	@Override
	public int patch(ClassNode classNode)
	{
		int total = 0;
		for(MethodNode method : classNode.methods)
		{
			total += this.scanAndPatch(method, classNode);
		}
		return total;
	}

	private int scanAndPatch(MethodNode method, ClassNode classNode)
	{
		AbstractInsnNode[] insns = method.instructions.toArray();

		Map<Integer, AxisOrigin> varOrigins = this.buildVarOriginMap(insns, classNode);
		AxisOrigin[] produced = this.computeProducedOrigins(insns, varOrigins, classNode);

		int patches = 0;

		for(int i = 0; i < insns.length; i++)
		{
			if(insns[i].getOpcode() != Opcodes.DSUB)
			{
				continue;
			}

			int[] producers = findDSubProducers(produced, i);
			if(producers == null)
			{
				continue;
			}

			AxisOrigin aoA = produced[producers[0]];
			AxisOrigin aoB = produced[producers[1]];

			if(!isSubtractionPattern(aoA, aoB))
			{
				continue;
			}
			if(aoA.axis != aoB.axis)
			{
				continue;
			}

			int targetIdx = (aoA.origin == Origin.OTHER) ? producers[0] : producers[1];
			patches += this.patchSite(method, insns, targetIdx, aoA.axis);
		}

		return patches;
	}

	private int patchSite(MethodNode method, AbstractInsnNode[] insns, int idx, Axis axis)
	{
		AbstractInsnNode target = insns[idx];

		if(target instanceof MethodInsnNode m && m.getOpcode() == Opcodes.INVOKEVIRTUAL && "()D".equals(m.desc) && axisOf(m.name) == axis)
		{
			return this.replaceGetXYZ(method, m, axis);
		}

		if(target.getOpcode() == Opcodes.DLOAD)
		{
			int slot = ((VarInsnNode) target).var;
			int storeIdx = findDStoreSource(insns, slot, idx);
			if(storeIdx >= 0 && insns[storeIdx] instanceof MethodInsnNode ms && ms.getOpcode() == Opcodes.INVOKEVIRTUAL)
			{
				if("()D".equals(ms.desc) && axisOf(ms.name) == axis)
				{
					return this.replaceGetXYZ(method, ms, axis);
				}
			}
		}

		return 0;
	}

	private int replaceGetXYZ(MethodNode method, MethodInsnNode m, Axis axis)
	{
		String helper = switch(axis)
		{
			case X -> "eyeX";
			case Y -> "eyeY";
			case Z -> "eyeZ";
		};

		method.instructions.set(m, new MethodInsnNode(Opcodes.INVOKESTATIC, GRAVITY_API, helper, ENTITY_DESC, false));
		return 1;
	}

	private Map<Integer, AxisOrigin> buildVarOriginMap(AbstractInsnNode[] insns, ClassNode classNode)
	{
		Map<Integer, AxisOrigin> map = new HashMap<>();
		for(int i = 1; i < insns.length; i++)
		{
			if(insns[i].getOpcode() != Opcodes.DSTORE)
			{
				continue;
			}

			AbstractInsnNode producer = prevSignificant(insns, i - 1);
			if(producer == null)
			{
				continue;
			}

			AxisOrigin ao = this.originOf(insns, producer, classNode);
			if(ao.isKnown())
			{
				map.put(((VarInsnNode) insns[i]).var, ao);
			}
		}
		return map;
	}

	private AxisOrigin[] computeProducedOrigins(AbstractInsnNode[] insns, Map<Integer, AxisOrigin> varOrigins, ClassNode classNode)
	{
		AxisOrigin[] produced = new AxisOrigin[insns.length];
		Arrays.fill(produced, AxisOrigin.UNKNOWN);

		for(int i = 0; i < insns.length; i++)
		{
			AxisOrigin ao = this.originOf(insns, insns[i], classNode);
			if(ao.isKnown())
			{
				produced[i] = ao;
				continue;
			}
			if(insns[i].getOpcode() == Opcodes.DLOAD)
			{
				int slot = ((VarInsnNode) insns[i]).var;
				produced[i] = varOrigins.getOrDefault(slot, AxisOrigin.UNKNOWN);
			}
		}
		return produced;
	}

	private AxisOrigin originOf(AbstractInsnNode[] insns, AbstractInsnNode insn, ClassNode classNode)
	{
		int idx = indexOf(insns, insn);

		if(insn instanceof MethodInsnNode m && m.getOpcode() == Opcodes.INVOKEVIRTUAL && "()D".equals(m.desc))
		{
			Axis axis = axisOf(m.name);
			if(axis != null)
			{
				Origin origin = isAload0(prevSignificant(insns, idx - 1)) ? Origin.THIS : Origin.OTHER;
				return new AxisOrigin(axis, origin);
			}

			if(isAload0(prevSignificant(insns, idx - 1)))
			{
				String lower = m.name.toLowerCase();
				if(lower.contains("x") || lower.contains("y") || lower.contains("z"))
				{
					Axis resolved = resolveAxisFromMethod(m.name, m.desc, classNode);
					if(resolved != null)
					{
						return new AxisOrigin(resolved, Origin.THIS);
					}
				}
			}
		}

		if(insn instanceof FieldInsnNode f && f.getOpcode() == Opcodes.GETFIELD && "D".equals(f.desc))
		{
			Axis axis = axisOfField(f.name);
			if(axis == null)
			{
				return AxisOrigin.UNKNOWN;
			}
			Origin origin = isAload0(prevSignificant(insns, idx - 1)) ? Origin.THIS : Origin.OTHER;
			return new AxisOrigin(axis, origin);
		}

		return AxisOrigin.UNKNOWN;
	}

	private Axis resolveAxisFromMethod(String name, String desc, ClassNode classNode)
	{
		return resolveAxisFromMethod(name, desc, classNode, 0);
	}

	private Axis resolveAxisFromMethod(String name, String desc, ClassNode classNode, int depth)
	{
		if(depth > 4)
		{
			return null;
		}

		for(MethodNode mn : classNode.methods)
		{
			if(!name.equals(mn.name) || !desc.equals(mn.desc))
			{
				continue;
			}

			AbstractInsnNode[] inner = mn.instructions.toArray();
			for(int i = 1; i < inner.length; i++)
			{
				if(!(inner[i] instanceof MethodInsnNode m))
				{
					continue;
				}
				if(m.getOpcode() != Opcodes.INVOKEVIRTUAL)
				{
					continue;
				}

				AbstractInsnNode recv = prevSignificant(inner, i - 1);
				if(!isAload0(recv))
				{
					continue;
				}

				if("()D".equals(m.desc))
				{
					Axis axis = axisOf(m.name);
					if(axis != null)
					{
						return axis;
					}
				}

				if("()D".equals(m.desc) || m.desc.endsWith(")D"))
				{
					String lower = m.name.toLowerCase();
					if(lower.contains("x") || lower.contains("y") || lower.contains("z"))
					{
						Axis axis = resolveAxisFromMethod(m.name, m.desc, classNode, depth + 1);
						if(axis != null)
						{
							return axis;
						}
					}
				}
			}
			break;
		}
		return null;
	}

	private static int[] findDSubProducers(AxisOrigin[] produced, int dsubIdx)
	{
		int found = 0;
		int[] result = new int[2];

		for(int j = dsubIdx - 1; j >= 0 && found < 2; j--)
		{
			if(produced[j].isKnown())
			{
				result[found++] = j;
			}
		}

		if(found < 2)
		{
			return null;
		}
		return new int[]{result[1], result[0]};
	}

	private static int findDStoreSource(AbstractInsnNode[] insns, int slot, int beforeIdx)
	{
		for(int j = beforeIdx - 1; j >= 1; j--)
		{
			if(insns[j].getOpcode() == Opcodes.DSTORE && ((VarInsnNode) insns[j]).var == slot)
			{
				AbstractInsnNode src = prevSignificant(insns, j - 1);
				return (src != null) ? indexOf(insns, src) : -1;
			}
		}
		return -1;
	}

	private static boolean isSubtractionPattern(AxisOrigin a, AxisOrigin b)
	{
		return (a.origin == Origin.THIS && b.origin == Origin.OTHER) || (a.origin == Origin.OTHER && b.origin == Origin.THIS);
	}

	private static boolean isAload0(AbstractInsnNode n)
	{
		return n != null && n.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) n).var == 0;
	}

	private Axis axisOf(String methodName)
	{
		if(GET_X.equals(methodName))
		{
			return Axis.X;
		}
		if(GET_Y.equals(methodName))
		{
			return Axis.Y;
		}
		if(GET_Z.equals(methodName))
		{
			return Axis.Z;
		}
		return null;
	}

	private Axis axisOfField(String fieldName)
	{
		if(FIELD_X.equals(fieldName))
		{
			return Axis.X;
		}
		if(FIELD_Y.equals(fieldName))
		{
			return Axis.Y;
		}
		if(FIELD_Z.equals(fieldName))
		{
			return Axis.Z;
		}
		return null;
	}

	private static AbstractInsnNode prevSignificant(AbstractInsnNode[] insns, int from)
	{
		for(int i = from; i >= 0; i--)
		{
			int t = insns[i].getType();
			if(t != AbstractInsnNode.LABEL && t != AbstractInsnNode.LINE)
			{
				return insns[i];
			}
		}
		return null;
	}

	private static int indexOf(AbstractInsnNode[] insns, AbstractInsnNode target)
	{
		for(int i = 0; i < insns.length; i++)
		{
			if(insns[i] == target)
			{
				return i;
			}
		}
		return -1;
	}
}
