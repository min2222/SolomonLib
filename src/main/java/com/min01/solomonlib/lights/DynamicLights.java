package com.min01.solomonlib.lights;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.min01.solomonlib.util.SolomonClientUtil;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of SodiumDynamicLights.
 *
 * Licensed under the MIT License. For more information,
 * see the LICENSE file.
 */

//https://github.com/txnimc/SodiumDynamicLights/blob/main/src/main/java/toni/sodiumdynamiclights/SodiumDynamicLights.java
public class DynamicLights
{
	public static final DynamicLights INSTANCE = new DynamicLights();
	private final Set<IDynamicLight> dynamicLightSources = new HashSet<>();
	private final ReentrantReadWriteLock lightSourcesLock = new ReentrantReadWriteLock();
	private static final double MAX_RADIUS = 7.75;
	private static final double MAX_RADIUS_SQUARED = MAX_RADIUS * MAX_RADIUS;
	private long lastUpdate = System.currentTimeMillis();
	public int lastUpdateCount = 0;
	
	public static DynamicLights get()
	{
		return INSTANCE;
	}
	
	public void updateAll(@NotNull LevelRenderer renderer) 
	{
		long now = System.currentTimeMillis();
		if(now >= this.lastUpdate + 50) 
		{
			this.lastUpdate = now;
			this.lastUpdateCount = 0;

			this.lightSourcesLock.readLock().lock();
			for(var lightSource : this.dynamicLightSources) 
			{
				if(lightSource.updateDynamicLight(renderer)) 
				{
					this.lastUpdateCount++;
				}
			}
			this.lightSourcesLock.readLock().unlock();
		}
	}
	
	public static void updateTrackedChunks(@NotNull BlockPos chunkPos, @Nullable LongOpenHashSet old, @Nullable LongOpenHashSet newPos)
	{
		if(old != null || newPos != null) 
		{
			long pos = chunkPos.asLong();
			if(old != null)
			{
				old.remove(pos);
			}
			if(newPos != null)
			{
				newPos.add(pos);
			}
		}
	}

	public static void updateTracking(@NotNull IDynamicLight lightSource)
	{
		boolean enabled = lightSource.isDynamicLightEnabled();
		int luminance = lightSource.getLuminance();
		if(!enabled && luminance > 0)
		{
			lightSource.setBTADynamicLightEnabled(true);
		} 
		else if(enabled && luminance < 1)
		{
			lightSource.setBTADynamicLightEnabled(false);
		}
	}
	
	public static void scheduleChunkRebuild(@NotNull LevelRenderer renderer, @NotNull BlockPos chunkPos)
	{
		scheduleChunkRebuild(renderer, chunkPos.getX(), chunkPos.getY(), chunkPos.getZ());
	}
	
	public static void scheduleChunkRebuild(@NotNull LevelRenderer renderer, long chunkPos) 
	{
		scheduleChunkRebuild(renderer, BlockPos.getX(chunkPos), BlockPos.getY(chunkPos), BlockPos.getZ(chunkPos));
	}

 	@OnlyIn(Dist.CLIENT)
	public static void scheduleChunkRebuild(@NotNull LevelRenderer renderer, int x, int y, int z)
	{
		if(SolomonClientUtil.MC.level != null)
		{
			((LevelRendererAccessor) renderer).scheduleChunkRebuild(x, y, z, false);
		}
	}
	
	public int getLightmapWithDynamicLight(@NotNull BlockPos pos, int lightmap) 
	{
		return this.getLightmapWithDynamicLight(this.getDynamicLightLevel(pos), lightmap);
	}
	
	public int getLightmapWithDynamicLight(double dynamicLightLevel, int lightmap) 
	{
		if(dynamicLightLevel > 0) 
		{
			int blockLevel = LightTexture.block(lightmap);
			if(dynamicLightLevel > blockLevel)
			{
				int luminance = (int) (dynamicLightLevel * 16.0);
				lightmap &= 0xfff00000;
				lightmap |= luminance & 0x000fffff;
			}
		}
		return lightmap;
	}
	
	public double getDynamicLightLevel(@NotNull BlockPos pos) 
	{
		double result = 0;
		this.lightSourcesLock.readLock().lock();
		for(var lightSource : this.dynamicLightSources) 
		{
			result = maxDynamicLightLevel(pos, lightSource, result);
		}
		this.lightSourcesLock.readLock().unlock();

		return Mth.clamp(result, 0, 15);
	}
	
	public static double maxDynamicLightLevel(@NotNull BlockPos pos, @NotNull IDynamicLight lightSource, double currentLightLevel)
	{
		int luminance = lightSource.getLuminance();
		if(luminance > 0)
		{
			double dx = pos.getX() - lightSource.getDynamicLightX() + 0.5;
			double dy = pos.getY() - lightSource.getDynamicLightY() + 0.5;
			double dz = pos.getZ() - lightSource.getDynamicLightZ() + 0.5;
			double distanceSquared = dx * dx + dy * dy + dz * dz;
			if(distanceSquared <= MAX_RADIUS_SQUARED) 
			{
				double multiplier = 1.0 - Math.sqrt(distanceSquared) / MAX_RADIUS;
				double lightLevel = multiplier * (double) luminance;
				if(lightLevel > currentLightLevel)
				{
					return lightLevel;
				}
			}
		}
		return currentLightLevel;
	}
	
	public boolean containsLightSource(@NotNull IDynamicLight lightSource) 
	{
		if(!lightSource.getDynamicLightLevel().isClientSide())
		{
			return false;
		}

		boolean result;
		this.lightSourcesLock.readLock().lock();
		result = this.dynamicLightSources.contains(lightSource);
		this.lightSourcesLock.readLock().unlock();
		return result;
	}
	
	public void addLightSource(@NotNull IDynamicLight lightSource)
	{
		if(!lightSource.getDynamicLightLevel().isClientSide())
			return;
		if(this.containsLightSource(lightSource))
			return;
		this.lightSourcesLock.writeLock().lock();
		this.dynamicLightSources.add(lightSource);
		this.lightSourcesLock.writeLock().unlock();
	}

 	@OnlyIn(Dist.CLIENT)
	public void removeLightSource(@NotNull IDynamicLight lightSource) 
	{
		this.lightSourcesLock.writeLock().lock();
		var dynamicLightSources = this.dynamicLightSources.iterator();
		IDynamicLight it;
		while(dynamicLightSources.hasNext()) 
		{
			it = dynamicLightSources.next();
			if(it.equals(lightSource)) 
			{
				dynamicLightSources.remove();
				lightSource.scheduleTrackedChunksRebuild(SolomonClientUtil.MC.levelRenderer);
				break;
			}
		}
		this.lightSourcesLock.writeLock().unlock();
	}

 	@OnlyIn(Dist.CLIENT)
	public void clearLightSources() 
	{
		this.lightSourcesLock.writeLock().lock();
		var dynamicLightSources = this.dynamicLightSources.iterator();
		IDynamicLight it;
		while(dynamicLightSources.hasNext()) 
		{
			it = dynamicLightSources.next();
			dynamicLightSources.remove();
			if(it.getLuminance() > 0)
			{
				it.resetDynamicLight();
			}
			it.scheduleTrackedChunksRebuild(SolomonClientUtil.MC.levelRenderer);
		}
		this.lightSourcesLock.writeLock().unlock();
	}
}
