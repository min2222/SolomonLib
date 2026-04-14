package com.min01.solomonlib;

import java.util.concurrent.atomic.AtomicBoolean;

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
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class SolomonBootstrap
{
	private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
	private static final AtomicBoolean MOD_BUS_REGISTERED = new AtomicBoolean(false);
	private static final AtomicBoolean FORGE_BUS_REGISTERED = new AtomicBoolean(false);
	
	private SolomonBootstrap()
	{
	}
	
	public static void initialize(FMLJavaModLoadingContext ctx)
	{
		if(!INITIALIZED.compareAndSet(false, true))
		{
			return;
		}
		
		registerModBus(ctx.getModEventBus());
		registerForgeBus();
		SolomonNetwork.registerMessages();
		ctx.registerConfig(Type.COMMON, SolomonConfig.CONFIG_SPEC, "solomonlib.toml");
	}
	
	private static void registerModBus(IEventBus bus)
	{
		if(!MOD_BUS_REGISTERED.compareAndSet(false, true))
		{
			return;
		}
		
		SolomonItems.ITEMS.register(bus);
		SolomonBlocks.BLOCKS.register(bus);
		SolomonBlocks.BLOCK_ENTITIES.register(bus);
		SolomonMobEffects.EFFECTS.register(bus);
		SolomonMobEffects.POTIONS.register(bus);
		SolomonCreativeTabs.CREATIVE_MODE_TAB.register(bus);
	}
	
	private static void registerForgeBus()
	{
		if(!FORGE_BUS_REGISTERED.compareAndSet(false, true))
		{
			return;
		}
		
		MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, SolomonCapabilities::onAttachEntityCapabilities);
	}
}