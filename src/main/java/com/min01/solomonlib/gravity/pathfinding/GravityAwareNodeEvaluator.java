package com.min01.solomonlib.gravity.pathfinding;

import java.util.EnumSet;

import com.min01.solomonlib.gravity.zone.GravityZoneManager;
import com.min01.solomonlib.spider.AdvancedWalkNodeEvaluator;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;

public class GravityAwareNodeEvaluator extends AdvancedWalkNodeEvaluator
{
	@Override
	public void prepare(PathNavigationRegion region, Mob mob)
	{
		Direction gravDir = GravityZoneManager.getDirection(mob.level, mob.blockPosition());
		this.pathableFacings = EnumSet.of(gravDir);
		this.advancedPathFindingEntity = new GravityEntityAdapter(mob);
		super.prepare(region, mob);
	}
}
