package com.min01.solomonlib.patcher;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import io.github.mincl.mixinglobal.spi.GlobalClassNodePatcher;

public class SolomonGlobalClassNodePatcher implements GlobalClassNodePatcher
{
	private static final Logger LOGGER = LogManager.getLogger("SolomonLib/GlobalPatcher");
	private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("solomonlib.globalpatcher.debug", "false"));
	private static final AtomicBoolean FIRST_CALL_LOGGED = new AtomicBoolean(false);
	private static final Set<String> PATCHED_CLASSES = ConcurrentHashMap.newKeySet();
	private static final ThreadLocal<Boolean> IN_PATCH = ThreadLocal.withInitial(() -> Boolean.FALSE);

	private static final String[] SKIP_PREFIXES = {
		"com/min01/solomonlib/",
		"io/github/mincl/mixinglobal/",
		"org/spongepowered/",
		"com/llamalad7/mixinextras/",
		"com/bawnorton/mixinsquared/",
		"cpw/mods/",
		"net/minecraftforge/",
		"java/",
		"javax/",
		"jdk/",
		"sun/"
	};

	private final EyePositionPatcher eyePositionPatcher = new EyePositionPatcher();
	private final RangedAttackPatcher rangedAttackPatcher = new RangedAttackPatcher();
	private final ProjectileLivingConstructorPatcher projectileLivingConstructorPatcher = new ProjectileLivingConstructorPatcher();
	private final GravityStrengthPatcher gravityStrengthPatcher = new GravityStrengthPatcher();
	private final FallingBlockGravityPatcher fallingBlockGravityPatcher = new FallingBlockGravityPatcher();

	@Override
	public String id()
	{
		return "solomonlib:global_patcher";
	}

	@Override
	public int patch(ClassNode classNode)
	{
		if(FIRST_CALL_LOGGED.compareAndSet(false, true))
		{
			LOGGER.info("[SolomonLib/GlobalPatcher] patcher active debug={}", DEBUG);
		}
		if(classNode == null || classNode.name == null || shouldSkip(classNode.name))
		{
			return 0;
		}
		if(IN_PATCH.get())
		{
			return 0;
		}
		// Mixin/ModLauncher can revisit the same class; avoid duplicate bytecode edits.
		if(!PATCHED_CLASSES.add(classNode.name))
		{
			return 0;
		}

		IN_PATCH.set(Boolean.TRUE);
		try
		{
			int eyePatched = this.eyePositionPatcher.patch(classNode);
			int rangedPatched = this.rangedAttackPatcher.patch(classNode);
			int projectileCtorPatched = this.projectileLivingConstructorPatcher.patch(classNode);
			int gravityPatched = this.gravityStrengthPatcher.patch(classNode);
			int fallingPatched = this.fallingBlockGravityPatcher.patch(classNode);
			int total = eyePatched + rangedPatched + projectileCtorPatched + gravityPatched + fallingPatched;

			if(DEBUG && total > 0)
			{
				LOGGER.info(
					"[SolomonLib/GlobalPatcher] class={} eye={} ranged={} projectileCtor={} gravity={} falling={} total={}",
					classNode.name,
					eyePatched,
					rangedPatched,
					projectileCtorPatched,
					gravityPatched,
					fallingPatched,
					total);
			}
			if(total > 0)
			{
				stripLocalVariableTables(classNode);
			}
			return total;
		}
		finally
		{
			IN_PATCH.set(Boolean.FALSE);
		}
	}

	private static boolean shouldSkip(String internalName)
	{
		for(String prefix : SKIP_PREFIXES)
		{
			if(internalName.startsWith(prefix))
			{
				return true;
			}
		}
		return false;
	}

	private static void stripLocalVariableTables(ClassNode classNode)
	{
		for(MethodNode method : classNode.methods)
		{
			method.localVariables = null;
		}
	}
}
