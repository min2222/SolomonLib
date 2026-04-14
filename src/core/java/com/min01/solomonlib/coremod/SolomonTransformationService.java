package com.min01.solomonlib.coremod;

import java.util.List;
import java.util.Set;

import com.min01.solomonlib.coremod.transformer.EyePositionTransformer;
import com.min01.solomonlib.coremod.transformer.FallingBlockGravityTransformer;
import com.min01.solomonlib.coremod.transformer.GravityStrengthTransformer;
import com.min01.solomonlib.coremod.transformer.RangedAttackTransformer;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;

public class SolomonTransformationService implements ITransformationService
{
    @Override
    public String name()
    {
        return "solomonlib";
    }

    @Override
    public void initialize(IEnvironment environment)
    { 
    	
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException
    {
    	
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<ITransformer> transformers()
    {
        return List.of(
            new EyePositionTransformer(),
            new RangedAttackTransformer(),
            new GravityStrengthTransformer(),
            new FallingBlockGravityTransformer());
    }
}
