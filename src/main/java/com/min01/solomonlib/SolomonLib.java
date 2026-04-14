package com.min01.solomonlib;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SolomonLib.MODID)
public class SolomonLib
{
	public static final String MODID = "solomonlib";
	
	public SolomonLib(FMLJavaModLoadingContext ctx) 
	{
		SolomonBootstrap.initialize(ctx);
	}
}