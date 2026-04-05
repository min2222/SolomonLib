package com.min01.solomonlib.gravity.pathfinding;

import com.min01.solomonlib.gravity.zone.GravityZoneManager;
import com.min01.solomonlib.spider.IAdvancedPathFindingEntity;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;

public class GravityEntityAdapter implements IAdvancedPathFindingEntity
{
	private final Mob mob;

	public GravityEntityAdapter(Mob mob)
	{
		this.mob = mob;
	}

	@Override
	public Direction getGroundSide()
	{
		return GravityZoneManager.getDirection(this.mob.level, this.mob.blockPosition());
	}

	@Override
	public void onPathingObstructed(Direction facing) 
	{
		
	}
}
