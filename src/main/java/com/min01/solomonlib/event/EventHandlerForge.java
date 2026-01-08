package com.min01.solomonlib.event;

import com.min01.solomonlib.SolomonLib;
import com.min01.solomonlib.config.SolomonConfig;
import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.GravityCapabilityImpl;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = SolomonLib.MODID)
public class EventHandlerForge
{
	@SubscribeEvent
	public static void onEntityJoinLevel(EntityJoinLevelEvent event)
	{
		Entity entity = event.getEntity();
		GravityAPI.ENTITY_MAP.put(entity.getClass().hashCode(), entity);
		GravityAPI.ENTITY_MAP2.put(entity.getClass().getSuperclass().hashCode(), entity);
	}
	
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event)
    {
    	Player player = event.getEntity();
    	if(event.isWasDeath() && !SolomonConfig.resetGravityOnRespawn.get())
    	{
        	Player original = event.getOriginal();
        	original.revive();
        	GravityAPI.setBaseGravityDirection(player, GravityAPI.getBaseGravityDirection(original));
    	}
		for(Entity entity : GravityAPI.getAllEntities(player.level))
		{
			if(!entity.level.isClientSide)
			{
    			if(GravityAPI.getBaseGravityDirection(entity) == Direction.DOWN)
    			{
    				continue;
    			}
    			GravityCapabilityImpl cap = GravityAPI.getGravityComponent(entity);
    			cap.initialized = false;
				cap.deserializeNBT(cap.serializeNBT());
			}
		}
    }
}
