package com.min01.solomonlib.event;

import com.min01.solomonlib.SolomonLib;
import com.min01.solomonlib.util.SolomonUtil;

import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.Type;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = SolomonLib.MODID)
public class EventHandlerForge
{
	@SubscribeEvent
	public static void onEntityJoinLevel(EntityJoinLevelEvent event)
	{
		
	}
    
	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent event)
	{
		if(event.phase == Phase.END && event.type == Type.LEVEL)
		{
			SolomonUtil.removeUpsideDownBlocks(event.level);
		}
	}
}
