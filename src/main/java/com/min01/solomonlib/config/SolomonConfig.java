package com.min01.solomonlib.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class SolomonConfig 
{
	public static final SolomonConfig CONFIG;
	public static final ForgeConfigSpec CONFIG_SPEC;

	public static ForgeConfigSpec.BooleanValue PATH_FINDER_DEBUG_PREVIEW;
    
    static 
    {
    	Pair<SolomonConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(SolomonConfig::new);
    	CONFIG = pair.getLeft();
    	CONFIG_SPEC = pair.getRight();
    }
	
    public SolomonConfig(ForgeConfigSpec.Builder config) 
    {
    	config.push("Settings");
    	PATH_FINDER_DEBUG_PREVIEW = config.comment("Whether the path finder debug preview should be enabled.").define("path_finder_debug_preview", false);
        config.pop();
    }
}
