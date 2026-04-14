package com.min01.solomonlib.event;

import com.min01.solomonlib.SolomonLib;
import com.min01.solomonlib.config.SolomonConfig;
import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.GravityCapabilityImpl;
import com.min01.solomonlib.gravity.GravityZoneManager;
import com.min01.solomonlib.gravity.GravityZoneSavedData;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = SolomonLib.MODID)
public class EventHandlerForge
{
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
    
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event)
    {
        if(!(event.getLevel() instanceof ServerLevel level))
        {
            return;
        }
        GravityZoneSavedData.get(level);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event)
    {
        boolean isClient = event.getLevel().isClientSide();
        GravityZoneManager.onLevelUnload(((Level) event.getLevel()).dimension(), isClient);
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event)
    {
        ServerLevel level = event.getLevel();
        ChunkPos chunkPos = event.getPos();
        ServerPlayer player = event.getPlayer();
        GravityZoneManager.syncChunkToPlayer(level, chunkPos, player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(!(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }
        if(!(player.level instanceof ServerLevel level))
        {
            return;
        }
        GravityZoneManager.syncAllToPlayer(level, player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if(!(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }
        if(!(player.level instanceof ServerLevel level))
        {
            return;
        }
        GravityZoneManager.syncAllToPlayer(level, player);
    }
}
