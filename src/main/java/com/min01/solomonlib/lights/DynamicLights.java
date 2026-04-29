package com.min01.solomonlib.lights;

/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 * Copyright © 2024 toni (https://github.com/txnimc/SodiumDynamicLights)
 *
 * Portions of this file are derived from SodiumDynamicLights / LambDynLights and are
 * used under the MIT License. The full license text is included in README.md at the
 * repository root.
 */

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.min01.solomonlib.util.SolomonClientUtil;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		boolean enabled = lightSource.solomonlib$isDynamicLightEnabled();
		int luminance = lightSource.getLuminance();
		if(!enabled && luminance > 0)
		{
			lightSource.solomonlib$setDynamicLightEnabled(true);
		}
		else if(enabled && luminance < 1)
		{
			lightSource.solomonlib$setDynamicLightEnabled(false);
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
		{
			return;
		}
		if(this.containsLightSource(lightSource))
		{
			return;
		}
		this.lightSourcesLock.writeLock().lock();
		this.dynamicLightSources.add(lightSource);
		this.lightSourcesLock.writeLock().unlock();
	}

	public int getLightSourcesCount()
	{
		this.lightSourcesLock.readLock().lock();
		try
		{
			return this.dynamicLightSources.size();
		}
		finally
		{
			this.lightSourcesLock.readLock().unlock();
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void removeLightSource(@NotNull IDynamicLight lightSource)
	{
		this.lightSourcesLock.writeLock().lock();
		try
		{
			var iterator = this.dynamicLightSources.iterator();
			while(iterator.hasNext())
			{
				IDynamicLight it = iterator.next();
				if(it.equals(lightSource))
				{
					iterator.remove();
					lightSource.scheduleTrackedChunksRebuild(SolomonClientUtil.MC.levelRenderer);
					break;
				}
			}
		}
		finally
		{
			this.lightSourcesLock.writeLock().unlock();
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void removeLightSources(@NotNull Predicate<IDynamicLight> filter)
	{
		this.lightSourcesLock.writeLock().lock();
		try
		{
			var iterator = this.dynamicLightSources.iterator();
			while(iterator.hasNext())
			{
				IDynamicLight it = iterator.next();
				if(filter.test(it))
				{
					iterator.remove();
					if(it.getLuminance() > 0)
					{
						it.resetDynamicLight();
					}
					it.scheduleTrackedChunksRebuild(SolomonClientUtil.MC.levelRenderer);
				}
			}
		}
		finally
		{
			this.lightSourcesLock.writeLock().unlock();
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void removeEntitiesLightSource()
	{
		this.removeLightSources(lightSource -> lightSource instanceof Entity && !(lightSource instanceof Player));
	}

	@OnlyIn(Dist.CLIENT)
	public void removeCreeperLightSources()
	{
		this.removeLightSources(lightSource -> lightSource instanceof Creeper);
	}

	@OnlyIn(Dist.CLIENT)
	public void removeTntLightSources()
	{
		this.removeLightSources(lightSource -> lightSource instanceof PrimedTnt);
	}

	@OnlyIn(Dist.CLIENT)
	public void removeBlockEntitiesLightSource()
	{
		this.removeLightSources(lightSource -> lightSource instanceof BlockEntity);
	}

	@OnlyIn(Dist.CLIENT)
	public void clearLightSources()
	{
		this.lightSourcesLock.writeLock().lock();
		try
		{
			var iterator = this.dynamicLightSources.iterator();
			while(iterator.hasNext())
			{
				IDynamicLight it = iterator.next();
				iterator.remove();
				if(it.getLuminance() > 0)
				{
					it.resetDynamicLight();
				}
				it.scheduleTrackedChunksRebuild(SolomonClientUtil.MC.levelRenderer);
			}
		}
		finally
		{
			this.lightSourcesLock.writeLock().unlock();
		}
	}
}
