package com.min01.solomonlib.misc;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.bawnorton.mixinsquared.MixinSquaredBootstrap;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;

import io.github.mincl.mixinglobal.bootstrap.MixinGlobalBootstrap;

public class SolomonMixinPlugin implements IMixinConfigPlugin
{
    @Override
    public void onLoad(String mixinPackage)
    {
        MixinExtrasBootstrap.init();
        MixinSquaredBootstrap.init();
        MixinGlobalBootstrap.init();
    }

	@Override
	public String getRefMapperConfig()
	{
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) 
	{
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
