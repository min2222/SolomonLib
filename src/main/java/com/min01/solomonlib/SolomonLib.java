package com.min01.solomonlib;

import com.min01.solomonlib.block.SolomonBlocks;
import com.min01.solomonlib.capabilities.SolomonCapabilities;
import com.min01.solomonlib.config.SolomonConfig;
import com.min01.solomonlib.effect.SolomonMobEffects;
import com.min01.solomonlib.item.SolomonItems;
import com.min01.solomonlib.misc.SolomonCreativeTabs;
import com.min01.solomonlib.network.SolomonNetwork;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SolomonLib.MODID)
public class SolomonLib
{
	public static final String MODID = "solomonlib";
	
	public SolomonLib() 
	{
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext ctx = ModLoadingContext.get();

		SolomonItems.ITEMS.register(bus);
		SolomonBlocks.BLOCKS.register(bus);
		SolomonBlocks.BLOCK_ENTITIES.register(bus);
		SolomonMobEffects.EFFECTS.register(bus);
		SolomonMobEffects.POTIONS.register(bus);
		SolomonCreativeTabs.CREATIVE_MODE_TAB.register(bus);
		
		SolomonNetwork.registerMessages();
		MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, SolomonCapabilities::attachEntityCapability);
		ctx.registerConfig(Type.COMMON, SolomonConfig.CONFIG_SPEC, "solomonlib.toml");
	}
}