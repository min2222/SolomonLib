package com.min01.solomonlib.mixin.lights;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Mixin(value = Entity.class, priority = -15000)
public abstract class MixinEntity implements IDynamicLight
{
	@Unique
	protected int luminance = 0;
	
	@Unique
	private int lastLuminance = 0;
	
	@Unique
	private double prevX;
	
	@Unique
	private double prevY;
	
	@Unique
	private double prevZ;
	
	@Unique
	private LongOpenHashSet trackedLitChunkPos = new LongOpenHashSet();

	@Inject(method = "tick", at = @At("TAIL"))
	private void tickTail(CallbackInfo ci) 
	{
		if(Entity.class.cast(this).level.isClientSide)
		{
			if(Entity.class.cast(this).isRemoved()) 
			{
				this.setBTADynamicLightEnabled(false);
			}
			else
			{
				if(this.shouldUpdateDynamicLight())
				{
					this.dynamicLightTick();
					DynamicLights.updateTracking(this);
				}
				else
				{
					this.setBTADynamicLightEnabled(false);
				}
			}
		}
	}

	@Inject(method = "remove", at = @At("TAIL"))
	private void remove(CallbackInfo ci) 
	{
		if(Entity.class.cast(this).level.isClientSide)
		{
			this.setBTADynamicLightEnabled(false);
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
				if(living.getItemInHand(hand).getItem() instanceof IDynamicLightItem lightItem)
				{
					return lightItem.getDynamicLightPos();
				}
			}
		}
		return Vec3.ZERO;
	}

	@Override
	public Level getDynamicLightLevel() 
	{
		return Entity.class.cast(this).level;
	}

	@Override
	public void resetDynamicLight() 
	{
		this.lastLuminance = 0;
		this.luminance = 0;
	}

	@Override
	public boolean shouldUpdateDynamicLight()
	{
		Entity entity = Entity.class.cast(this);
		if(entity instanceof IDynamicLightEntity lightEntity)
		{
			return lightEntity.shouldUpdateDynamicLight();
		}
		if(entity instanceof LivingEntity living)
		{
			for(InteractionHand hand : InteractionHand.values())
			{
				if(living.getItemInHand(hand).getItem() instanceof IDynamicLightItem lightItem)
				{
					return lightItem.shouldUpdateDynamicLight();
				}
			}
		}
		return false;
	}

	@Override
	public void dynamicLightTick() 
	{
		this.luminance = 0;
		int luminance = 15;
		if(luminance > this.luminance)
		{
			this.luminance = luminance;
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
		if(!this.shouldUpdateDynamicLight())
		{
			return false;
		}
		double deltaX = Entity.class.cast(this).getX() - this.prevX;
		double deltaY = Entity.class.cast(this).getY() - this.prevY;
		double deltaZ = Entity.class.cast(this).getZ() - this.prevZ;

		int luminance = this.getLuminance();

		if(Math.abs(deltaX) > 0.1D || Math.abs(deltaY) > 0.1D || Math.abs(deltaZ) > 0.1D || luminance != this.lastLuminance) 
		{
			this.prevX = Entity.class.cast(this).getX();
			this.prevY = Entity.class.cast(this).getY();
			this.prevZ = Entity.class.cast(this).getZ();
			this.lastLuminance = luminance;

			LongOpenHashSet newPos = new LongOpenHashSet();

			if(luminance > 0) 
			{
				ChunkPos entityChunkPos = Entity.class.cast(this).chunkPosition();
				BlockPos.MutableBlockPos chunkPos = new BlockPos.MutableBlockPos(entityChunkPos.x, SectionPos.blockToSectionCoord(Entity.class.cast(this).getEyeY()), entityChunkPos.z);

				DynamicLights.scheduleChunkRebuild(renderer, chunkPos);
				DynamicLights.updateTrackedChunks(chunkPos, this.trackedLitChunkPos, newPos);

				Direction directionX = (Entity.class.cast(this).getOnPos().getX() & 15) >= 8 ? Direction.EAST : Direction.WEST;
				Direction directionY = ((int) Mth.floor(Entity.class.cast(this).getEyeY()) & 15) >= 8 ? Direction.UP : Direction.DOWN;
				Direction directionZ = (Entity.class.cast(this).getOnPos().getZ() & 15) >= 8 ? Direction.SOUTH : Direction.NORTH;

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
			return true;
		}
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void scheduleTrackedChunksRebuild(@NotNull LevelRenderer renderer)
	{
		if(SolomonClientUtil.MC.level == Entity.class.cast(this).level)
		{
			for(long pos : this.trackedLitChunkPos)
			{
				DynamicLights.scheduleChunkRebuild(renderer, pos);
			}
		}
	}
}
