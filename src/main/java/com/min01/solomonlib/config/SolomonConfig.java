package com.min01.solomonlib.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class SolomonConfig 
{
	public static final SolomonConfig CONFIG;
	public static final ForgeConfigSpec CONFIG_SPEC;

	public static ForgeConfigSpec.BooleanValue PATH_FINDER_DEBUG_PREVIEW;
	
    public static ConfigValue<Integer> rotationTime;
    public static ForgeConfigSpec.BooleanValue worldVelocity;
    
    public static ForgeConfigSpec.DoubleValue gravityStrengthMultiplier;

	public static ForgeConfigSpec.BooleanValue resetGravityOnRespawn;
	public static ForgeConfigSpec.BooleanValue voidDamageAboveWorld;
	public static ForgeConfigSpec.BooleanValue voidDamageOnHorizontalFallTooFar;
	public static ForgeConfigSpec.BooleanValue autoJumpOnGravityPlateInnerCorner;
	public static ForgeConfigSpec.BooleanValue adjustPositionAfterChangingGravity;
    
    static 
    {
    	Pair<SolomonConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(SolomonConfig::new);
    	CONFIG = pair.getLeft();
    	CONFIG_SPEC = pair.getRight();
    }
	
    public SolomonConfig(ForgeConfigSpec.Builder config) 
    {
    	config.push("Gravity Settings");
    	rotationTime = config.comment("animation rotation time").defineInRange("rotationTime", 500, 0, Integer.MAX_VALUE);
    	gravityStrengthMultiplier = config.comment("gravity strength multiplier").defineInRange("gravityStrengthMultiplier", 1.0F, 0.0F, Float.MAX_VALUE);
    	worldVelocity = config.comment("world velocity").define("worldVelocity", false);
    	resetGravityOnRespawn = config.comment("reset gravity on respawn").define("resetGravityOnRespawn", true);
    	voidDamageAboveWorld = config.comment("void damage when above world").define("voidDamageAboveWorld", true);
    	voidDamageOnHorizontalFallTooFar = config.comment("void damage when horizontally fall too far").define("voidDamageOnHorizontalFallTooFar", true);
    	autoJumpOnGravityPlateInnerCorner = config.comment("auto jump on gravity plate inner corner").define("autoJumpOnGravityPlateInnerCorner", true);
    	adjustPositionAfterChangingGravity = config.comment("adjust position after gravity change").define("adjustPositionAfterChangingGravity", true);
        config.pop();
        
    	config.push("Spider Settings");
    	PATH_FINDER_DEBUG_PREVIEW = config.comment("Whether the path finder debug preview should be enabled.").define("path_finder_debug_preview", false);
        config.pop();
    }
}
