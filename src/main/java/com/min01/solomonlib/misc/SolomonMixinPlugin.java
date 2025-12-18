package com.min01.solomonlib.misc;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.bawnorton.mixinsquared.MixinSquaredBootstrap;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;

import net.minecraftforge.fml.loading.LoadingModList;

public class SolomonMixinPlugin implements IMixinConfigPlugin
{
    @Override
    public void onLoad(String mixinPackage)
    {
        MixinExtrasBootstrap.init();
        MixinSquaredBootstrap.init();
    }

	@Override
	public String getRefMapperConfig()
	{
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) 
	{
		if(mixinClassName.equals("com.min01.solomonlib.mixin.compat.ACEntityMixin") && LoadingModList.get().getModFileById("alexscaves") == null)
		{
			return false;
		}
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{
		
	}

	@Override
	public List<String> getMixins()
	{
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) 
	{
		
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
		
	}
}
