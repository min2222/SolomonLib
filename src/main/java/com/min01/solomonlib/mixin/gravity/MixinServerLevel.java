package com.min01.solomonlib.mixin.gravity;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.solomonlib.gravity.GravityAPI;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(value = ServerLevel.class, priority = -15000)
public abstract class MixinServerLevel extends Level
{   
	protected MixinServerLevel(WritableLevelData p_220352_, ResourceKey<Level> p_220353_, RegistryAccess p_270200_, Holder<DimensionType> p_220354_, Supplier<ProfilerFiller> p_220355_, boolean p_220356_, boolean p_220357_, long p_220358_, int p_220359_)
	{
		super(p_220352_, p_220353_, p_270200_, p_220354_, p_220355_, p_220356_, p_220357_, p_220358_, p_220359_);
	}
	
	@Inject(at = @At("HEAD"), method = "addFreshEntity")
	private void addFreshEntity(Entity p_8837_, CallbackInfoReturnable<Boolean> ci)
	{
		MySecurityManager manager = new MySecurityManager();
		Class<?>[] ctx = manager.getContext();
		for(Class<?> clazz : ctx)
		{
			if(GravityAPI.ENTITY_MAP.containsKey(clazz.hashCode()))
			{
				Entity entity = GravityAPI.ENTITY_MAP.get(clazz.hashCode());
				if(entity != null)
				{
			        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
			        GravityAPI.setBaseGravityDirection(p_8837_, gravityDirection);
				}
			}
			else if(GravityAPI.ENTITY_MAP2.containsKey(clazz.hashCode()))
			{
				Entity entity = GravityAPI.ENTITY_MAP2.get(clazz.hashCode());
				if(entity != null)
				{
			        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
			        GravityAPI.setBaseGravityDirection(p_8837_, gravityDirection);
				}
			}
		}
	}

	@SuppressWarnings("removal")
	private static class MySecurityManager extends SecurityManager
	{
		public Class<?>[] getContext()
		{
			return this.getClassContext();
		}
	}
}
