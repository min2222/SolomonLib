package com.min01.solomonlib;

import com.min01.solomonlib.config.SolomonConfig;
import com.min01.solomonlib.item.SolomonItems;
import com.min01.solomonlib.network.SolomonNetwork;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SolomonLib.MODID)
public class SolomonLib
{
	public static final String MODID = "solomonlib";
	
	public SolomonLib(FMLJavaModLoadingContext ctx) 
	{
		IEventBus bus = ctx.getModEventBus();

		SolomonItems.ITEMS.register(bus);
		
		SolomonNetwork.registerMessages();
		//MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, SolomonCapabilities::attachEntityCapability);
		ctx.registerConfig(Type.COMMON, SolomonConfig.CONFIG_SPEC, "solomonlib.toml");
	}
}