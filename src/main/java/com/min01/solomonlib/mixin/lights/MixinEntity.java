package com.min01.solomonlib.mixin.lights;

/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 * Copyright © 2024 toni (https://github.com/txnimc/SodiumDynamicLights)
 *
 * Portions of this file are derived from SodiumDynamicLights / LambDynLights and are
 * used under the MIT License. The full license text is included in README.md at the
 * repository root.
 */

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.solomonlib.lights.DynamicLights;
import com.min01.solomonlib.lights.IDynamicLight;
import com.min01.solomonlib.misc.IDynamicLightEntity;
import com.min01.solomonlib.misc.IDynamicLightItem;
import com.min01.solomonlib.util.SolomonClientUtil;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Mixin(value = Entity.class, priority = -15000)
public abstract class MixinEntity implements IDynamicLight
{
	@Shadow
	public abstract double getEyeY();

	@Shadow
	public abstract boolean isRemoved();

	@Shadow
	public abstract ChunkPos chunkPosition();

	@Unique
	protected int luminance;

	@Unique
	private LongOpenHashSet trackedLitChunkPos = new LongOpenHashSet();

	@Unique
	private int solomon$lastDynamicLuminance;

	@Unique
	private double solomon$prevLightX;

	@Unique
	private double solomon$prevLightY;

	@Unique
	private double solomon$prevLightZ;

	@Unique
	private Level solomon$dynamicLightLastLevel;

	@Inject(method = "tick", at = @At("TAIL"))
	private void solomon$tickTail(CallbackInfo ci)
	{
		Entity self = Entity.class.cast(this);
		if(!self.level().isClientSide())
		{
			return;
		}
		if(self.isRemoved())
		{
			this.setDynamicSolomonLightEnabled(false);
		}
		else
		{
			this.dynamicLightTick();
			DynamicLights.updateTracking(this);
		}
	}

	@Inject(method = "remove", at = @At("TAIL"))
	private void solomon$remove(CallbackInfo ci)
	{
		Entity self = Entity.class.cast(this);
		if(self.level().isClientSide())
		{
			this.setDynamicSolomonLightEnabled(false);
		}
	}

	@Override
	public double getDynamicLightX()
	{
		return this.getDynamicLightPos().x;
	}

	@Override
	public double getDynamicLightY()
	{
		return this.getDynamicLightPos().y;
	}

	@Override
	public double getDynamicLightZ()
	{
		return this.getDynamicLightPos().z;
	}

	@Unique
	public Vec3 getDynamicLightPos()
	{
		Entity entity = Entity.class.cast(this);
		if(entity instanceof IDynamicLightEntity lightEntity)
		{
			return lightEntity.getDynamicLightPos();
		}
		if(entity instanceof LivingEntity living)
		{
			for(InteractionHand hand : InteractionHand.values())
			{
				ItemStack stack = living.getItemInHand(hand);
				if(stack.getItem() instanceof IDynamicLightItem)
				{
					IDynamicLightItem lightItem = (IDynamicLightItem) stack.getItem();
					return lightItem.getDynamicLightPos(entity, stack);
				}
			}
		}
		return new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
	}

	@Override
	public Level getDynamicLightLevel()
	{
		return Entity.class.cast(this).level();
	}

	@Override
	public void resetDynamicLight()
	{
		this.solomon$lastDynamicLuminance = 0;
	}

	@Override
	public boolean shouldUpdateDynamicLight()
	{
		Entity entity = Entity.class.cast(this);
		boolean any = false;
		if(entity instanceof IDynamicLightEntity lightEntity)
		{
			any |= lightEntity.shouldUpdateDynamicLight();
		}
		if(entity instanceof LivingEntity living)
		{
			for(InteractionHand hand : InteractionHand.values())
			{
				ItemStack stack = living.getItemInHand(hand);
				if(stack.getItem() instanceof IDynamicLightItem lightItem)
				{
					any |= lightItem.shouldUpdateDynamicLight(entity, stack);
				}
			}
		}
		return any;
	}

	@Override
	public void dynamicLightTick()
	{
		Entity self = Entity.class.cast(this);
		this.luminance = 0;

		if(self instanceof IDynamicLightEntity lightEntity)
		{
			this.luminance = Math.max(this.luminance, lightEntity.getDynamicLightLuminance());
		}

		if(self instanceof LivingEntity living)
		{
			for(InteractionHand hand : InteractionHand.values())
			{
				ItemStack stack = living.getItemInHand(hand);
				if(stack.getItem() instanceof IDynamicLightItem lightItem)
				{
					int fromItem = lightItem.getDynamicLightLuminance(living, stack);
					this.luminance = Math.max(this.luminance, fromItem);
				}
			}
		}

		if(self instanceof Player player)
		{
			Level lastLevel = this.solomon$dynamicLightLastLevel;
			if(lastLevel != null && lastLevel != player.level())
			{
				this.luminance = 0;
			}
			this.solomon$dynamicLightLastLevel = player.level();
			if(player.isSpectator())
			{
				this.luminance = 0;
			}
		}
	}

	@Override
	public int getLuminance()
	{
		return this.luminance;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean updateDynamicLight(@NotNull LevelRenderer renderer)
	{
		Entity self = Entity.class.cast(this);
		double deltaX = this.getDynamicLightX() - this.solomon$prevLightX;
		double deltaY = this.getDynamicLightY() - this.solomon$prevLightY;
		double deltaZ = this.getDynamicLightZ() - this.solomon$prevLightZ;

		int lum = this.getLuminance();

		if(Math.abs(deltaX) > 0.1D || Math.abs(deltaY) > 0.1D || Math.abs(deltaZ) > 0.1D || lum != this.solomon$lastDynamicLuminance)
		{
			this.solomon$prevLightX = this.getDynamicLightX();
			this.solomon$prevLightY = this.getDynamicLightY();
			this.solomon$prevLightZ = this.getDynamicLightZ();
			this.solomon$lastDynamicLuminance = lum;

			LongOpenHashSet newPos = new LongOpenHashSet();

			if(lum > 0)
			{
				ChunkPos entityChunkPos = self.chunkPosition();
				BlockPos.MutableBlockPos chunkPos = new BlockPos.MutableBlockPos(entityChunkPos.x, SectionPos.blockToSectionCoord(this.getDynamicLightY()), entityChunkPos.z);

				DynamicLights.scheduleChunkRebuild(renderer, chunkPos);
				DynamicLights.updateTrackedChunks(chunkPos, this.trackedLitChunkPos, newPos);

				Direction directionX = (self.getOnPos().getX() & 15) >= 8 ? Direction.EAST : Direction.WEST;
				Direction directionY = ((int) Mth.floor(this.getDynamicLightY()) & 15) >= 8 ? Direction.UP : Direction.DOWN;
				Direction directionZ = (self.getOnPos().getZ() & 15) >= 8 ? Direction.SOUTH : Direction.NORTH;

				for(int i = 0; i < 7; i++)
				{
					if(i % 4 == 0)
					{
						chunkPos.move(directionX);
					}
					else if(i % 4 == 1)
					{
						chunkPos.move(directionZ);
					}
					else if(i % 4 == 2)
					{
						chunkPos.move(directionX.getOpposite());
					}
					else
					{
						chunkPos.move(directionZ.getOpposite());
						chunkPos.move(directionY);
					}
					DynamicLights.scheduleChunkRebuild(renderer, chunkPos);
					DynamicLights.updateTrackedChunks(chunkPos, this.trackedLitChunkPos, newPos);
				}
			}

			this.scheduleTrackedChunksRebuild(renderer);
			this.trackedLitChunkPos = newPos;
			return this.shouldUpdateDynamicLight();
		}
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void scheduleTrackedChunksRebuild(@NotNull LevelRenderer renderer)
	{
		Entity self = Entity.class.cast(this);
		if(SolomonClientUtil.MC.level == self.level())
		{
			for(long pos : this.trackedLitChunkPos)
			{
				DynamicLights.scheduleChunkRebuild(renderer, pos);
			}
		}
	}
}
