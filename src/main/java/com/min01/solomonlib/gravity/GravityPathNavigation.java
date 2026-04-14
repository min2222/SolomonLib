package com.min01.solomonlib.gravity;

import com.min01.solomonlib.spider.AdvancedPathFinder;
import com.min01.solomonlib.spider.DirectionalPathPoint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GravityPathNavigation extends GroundPathNavigation
{
	public GravityPathNavigation(Mob mob, Level level)
	{
		super(mob, level);
	}

	@Override
	protected PathFinder createPathFinder(int maxVisitedNodes)
	{
		GravityAwareNodeEvaluator evaluator = new GravityAwareNodeEvaluator();
		evaluator.setCanPassDoors(true);
		this.nodeEvaluator = evaluator;
		return new AdvancedPathFinder(evaluator, maxVisitedNodes);
	}

	@Override
	protected Vec3 getTempMobPos()
	{
		Direction gravDir = GravityZoneManager.getDirection(this.mob.level, this.mob.blockPosition());
		return gravityFeetPos(this.mob, gravDir);
	}

	private static Vec3 gravityFeetPos(Mob mob, Direction gravDir)
	{
		AABB bb = mob.getBoundingBox();
		double cx = (bb.minX + bb.maxX) / 2.0;
		double cy = (bb.minY + bb.maxY) / 2.0;
		double cz = (bb.minZ + bb.maxZ) / 2.0;
		return switch(gravDir)
		{
			case DOWN -> new Vec3(cx, bb.minY, cz);
			case UP -> new Vec3(cx, bb.maxY, cz);
			case NORTH -> new Vec3(cx, cy, bb.minZ);
			case SOUTH -> new Vec3(cx, cy, bb.maxZ);
			case WEST -> new Vec3(bb.minX, cy, cz);
			case EAST -> new Vec3(bb.maxX, cy, cz);
		};
	}

	@Override
	public boolean isStableDestination(BlockPos pos)
	{
		Direction gravDir = GravityZoneManager.getDirection(this.mob.level, pos);
		BlockPos surface = pos.relative(gravDir);
		return this.level.getBlockState(surface).isSolidRender(this.level, surface);
	}

	@Override
	public void tick()
	{
		++this.tick;

		if(this.hasDelayedRecomputation)
		{
			this.recomputePath();
		}

		if(!this.isDone())
		{
			if(this.canUpdatePath())
			{
				this.followThePath();
			}

			DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);

			if(!this.isDone())
			{
				Node target = this.path.getNode(this.path.getNextNodeIndex());

				Direction gravDir;
				if(target instanceof DirectionalPathPoint dpp && dpp.getPathSide() != null)
				{
					gravDir = dpp.getPathSide();
				}
				else
				{
					gravDir = GravityZoneManager.getDirection(mob.level, mob.blockPosition());
				}

				Vec3 targetPos = getExactPathingTarget(this.level, target.asBlockPos(), gravDir);
				this.mob.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, this.speedModifier);
			}
		}
	}

	@Override
	protected void followThePath()
	{
		Vec3 pos = this.getTempMobPos();
		this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;

		net.minecraft.core.Vec3i nodePos = this.path.getNextNodePos();
		Direction gravDir = GravityZoneManager.getDirection(this.mob.level, mob.blockPosition());

		double dx = Math.abs(this.mob.getX() - (nodePos.getX() + 0.5));
		double dy = Math.abs(this.mob.getY() - nodePos.getY());
		double dz = Math.abs(this.mob.getZ() - (nodePos.getZ() + 0.5));

		boolean reached = switch(gravDir.getAxis())
		{
			case X -> dx < 1.0 && dy < this.maxDistanceToWaypoint && dz < this.maxDistanceToWaypoint;
			case Y -> dx < this.maxDistanceToWaypoint && dy < 1.0 && dz < this.maxDistanceToWaypoint;
			case Z -> dx < this.maxDistanceToWaypoint && dy < this.maxDistanceToWaypoint && dz < 1.0;
		};

		if(reached)
		{
			this.path.advance();
		}

		this.doStuckDetection(pos);
	}

	private Vec3 getExactPathingTarget(BlockGetter blockAccess, BlockPos pos, Direction dir)
	{
		BlockPos offsetPos = pos.relative(dir);
		VoxelShape shape = blockAccess.getBlockState(offsetPos).getCollisionShape(blockAccess, offsetPos);

		Direction.Axis axis = dir.getAxis();
		int sign = dir.getStepX() + dir.getStepY() + dir.getStepZ();
		double offset = shape.isEmpty() ? sign : (sign > 0 ? shape.min(axis) - 1 : shape.max(axis));

		double marginXZ = 1.0 - (this.mob.getBbWidth() % 1.0);
		double marginY = 1.0 - (this.mob.getBbHeight() % 1.0);
		double pathOffsetXZ = (int) (this.mob.getBbWidth()  + 1.0F) * 0.5;
		double pathOffsetY = (int) (this.mob.getBbHeight() + 1.0F) * 0.5 - this.mob.getBbHeight() * 0.5F;

		double x = offsetPos.getX() + pathOffsetXZ + dir.getStepX() * marginXZ;
		double y = offsetPos.getY() + pathOffsetY + (dir == Direction.DOWN ? -pathOffsetY : 0.0) + (dir == Direction.UP   ? -pathOffsetY + marginY : 0.0);
		double z = offsetPos.getZ() + pathOffsetXZ + dir.getStepZ() * marginXZ;

		return switch(axis)
		{
			case X -> new Vec3(x + offset, y, z);
			case Y -> new Vec3(x, y + offset, z);
			case Z -> new Vec3(x, y, z + offset);
		};
	}
}
