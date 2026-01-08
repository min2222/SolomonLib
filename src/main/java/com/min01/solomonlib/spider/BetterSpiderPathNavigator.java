package com.min01.solomonlib.spider;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public class BetterSpiderPathNavigator<T extends Mob & IClimberEntity> extends AdvancedClimberPathNavigation<T> 
{
	private boolean useVanillaBehaviour;
	private BlockPos targetPosition;

	public BetterSpiderPathNavigator(T entity, Level worldIn, boolean useVanillaBehaviour) 
	{
		super(entity, worldIn, false, true, true);
		this.useVanillaBehaviour = useVanillaBehaviour;
	}

	@Override
	public Path createPath(BlockPos pos, int checkpointRange) 
	{
		this.targetPosition = pos;
		return super.createPath(pos, checkpointRange);
	}

	@Override
	public Path createPath(Entity entityIn, int checkpointRange) 
	{
		this.targetPosition = entityIn.blockPosition();
		return super.createPath(entityIn, checkpointRange);
	}

	@Override
	public boolean moveTo(Entity entityIn, double speedIn)
	{
		Path path = this.createPath(entityIn, 0);
		if(path != null)
		{
			return this.moveTo(path, speedIn);
		} 
		else 
		{
			this.targetPosition = entityIn.blockPosition();
			this.speedModifier = speedIn;
			return true;
		}
	}

	@Override
	public void tick() 
	{
		if(!this.isDone())
		{
			super.tick();
		} 
		else 
		{
			if(this.targetPosition != null && this.useVanillaBehaviour) 
			{
				// FORGE: Fix MC-94054
				if(!this.targetPosition.closerThan(this.mob.blockPosition(), Math.max((double) this.mob.getBbWidth(), 1.0D)) && (!(this.mob.getY() > (double) this.targetPosition.getY()) || !(BlockPos.containing(this.targetPosition.getX(), this.mob.getY(), this.targetPosition.getZ())).closerThan(this.mob.blockPosition(), Math.max((double) this.mob.getBbWidth(), 1.0D)))) 
				{
					this.mob.getMoveControl().setWantedPosition((double) this.targetPosition.getX(), (double) this.targetPosition.getY(), (double) this.targetPosition.getZ(), this.speedModifier);
				}
				else 
				{
					this.targetPosition = null;
				}
			}

		}
	}
}
