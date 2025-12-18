package com.min01.solomonlib.gravity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class GravityPathNavigation extends GroundPathNavigation
{
	public GravityPathNavigation(Mob p_26424_, Level p_26425_) 
	{
		super(p_26424_, p_26425_);
	}

	@Override
	protected PathFinder createPathFinder(int p_26428_) 
	{
		this.nodeEvaluator = new GravityNodeEvaluator();
		this.nodeEvaluator.setCanPassDoors(true);
		return new PathFinder(this.nodeEvaluator, p_26428_);
	}
	
	@Override
	protected Vec3 getTempMobPos() 
	{
		return this.getSurfacePos();
	}
	
	private Vec3 getSurfacePos() 
	{
		if(this.mob.isInWater() && this.canFloat()) 
		{
			Direction direction = GravityAPI.getGravityDirection(this.mob);
			BlockPos pos = this.mob.blockPosition();
			BlockState blockstate = this.level.getBlockState(pos);
			int j = 0;
			while(blockstate.is(Blocks.WATER)) 
			{
				pos = pos.relative(direction.getOpposite());
				blockstate = this.level.getBlockState(pos);
				++j;
				if(j > 16)
				{
					return this.mob.position();
				}
			}
			return Vec3.atCenterOf(pos);
		}
		else 
		{
			return this.mob.position();
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Path createPath(BlockPos p_26475_, int p_26476_) 
	{
		Direction direction = GravityAPI.getGravityDirection(this.mob);
		if(this.level.getBlockState(p_26475_).isAir())
		{
			BlockPos blockpos;
			int i = 0;
			int i2 = 0;
			for(blockpos = p_26475_.relative(direction); i < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos).isAir(); blockpos = blockpos.relative(direction))
			{
				i++;
			}
			if(i < this.level.getMaxBuildHeight())
			{
				return super.createPath(blockpos.relative(direction.getOpposite()), p_26476_);
			}
			while(i2 < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos).isAir())
			{
				i2++;
				blockpos = blockpos.relative(direction.getOpposite());
			}
			p_26475_ = blockpos;
		}
		if(!this.level.getBlockState(p_26475_).isSolid())
		{
			return super.createPath(p_26475_, p_26476_);
		} 
		else 
		{
			BlockPos blockpos1;
			int i = 0;
			for(blockpos1 = p_26475_.relative(direction.getOpposite()); i < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos1).isSolid(); blockpos1 = blockpos1.relative(direction.getOpposite())) 
			{
				i++;
			}
			return super.createPath(blockpos1, p_26476_);
		}
	}
}