package com.min01.solomonlib.gravity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class GravityBlockPos extends BlockPos
{
	private final Direction direction;
	
	public GravityBlockPos(Vec3i vec3i, Direction direction)
	{
		super(vec3i);
		this.direction = direction;
	}
	
	public GravityBlockPos(int x, int y, int z, Direction direction)
	{
		super(x, y, z);
		this.direction = direction;
	}
	
	@Override
	public BlockPos relative(Direction pDirection, int pDistance) 
	{
	    return switch (this.direction) 
	    {
	        case UP -> switch (pDirection) 
	        {
	            case UP -> this.gravityRelative(Direction.DOWN, pDistance);
	            case DOWN -> this.gravityRelative(Direction.UP, pDistance);
	            case WEST -> this.gravityRelative(Direction.WEST, pDistance);
	            case EAST -> this.gravityRelative(Direction.EAST, pDistance);
	            default -> this.gravityRelative(pDirection, pDistance);
	        };
	        case NORTH -> switch (pDirection)
	        {
	            case UP -> this.gravityRelative(Direction.SOUTH, pDistance);
	            case DOWN -> this.gravityRelative(Direction.NORTH, pDistance);
	            case NORTH -> this.gravityRelative(Direction.UP, pDistance);
	            case SOUTH -> this.gravityRelative(Direction.DOWN, pDistance);
	            default -> this.gravityRelative(pDirection, pDistance);
	        };
	        case SOUTH -> switch (pDirection)
	        {
	            case UP -> this.gravityRelative(Direction.NORTH, pDistance);
	            case DOWN -> this.gravityRelative(Direction.SOUTH, pDistance);
	            case NORTH -> this.gravityRelative(Direction.DOWN, pDistance);
	            case SOUTH -> this.gravityRelative(Direction.UP, pDistance);
	            default -> this.gravityRelative(pDirection, pDistance);
	        };
	        case WEST -> switch (pDirection) 
	        {
	            case UP -> this.gravityRelative(Direction.EAST, pDistance);
	            case DOWN -> this.gravityRelative(Direction.WEST, pDistance);
	            case WEST -> this.gravityRelative(Direction.DOWN, pDistance);
	            case EAST -> this.gravityRelative(Direction.UP, pDistance);
	            default -> this.gravityRelative(pDirection, pDistance);
	        };
	        case EAST -> switch (pDirection) 
	        {
	            case UP -> this.gravityRelative(Direction.WEST, pDistance);
	            case DOWN -> this.gravityRelative(Direction.EAST, pDistance);
	            case WEST -> this.gravityRelative(Direction.UP, pDistance);
	            case EAST -> this.gravityRelative(Direction.DOWN, pDistance);
	            default -> this.gravityRelative(pDirection, pDistance);
	        };
	        default -> this.gravityRelative(pDirection, pDistance);
	    };
	}

	@Override
	public BlockPos relative(Direction pDirection)
	{
	    return switch (this.direction)
	    {
	        case UP -> switch (pDirection) 
	        {
	            case UP -> this.gravityRelative(Direction.DOWN);
	            case DOWN -> this.gravityRelative(Direction.UP);
	            case WEST -> this.gravityRelative(Direction.WEST);
	            case EAST -> this.gravityRelative(Direction.EAST);
	            default -> this.gravityRelative(pDirection);
	        };
	        case NORTH -> switch (pDirection) 
	        {
	            case UP -> this.gravityRelative(Direction.SOUTH);
	            case DOWN -> this.gravityRelative(Direction.NORTH);
	            case NORTH -> this.gravityRelative(Direction.UP);
	            case SOUTH -> this.gravityRelative(Direction.DOWN);
	            default -> this.gravityRelative(pDirection);
	        };
	        case SOUTH -> switch (pDirection)
	        {
	            case UP -> this.gravityRelative(Direction.NORTH);
	            case DOWN -> this.gravityRelative(Direction.SOUTH);
	            case NORTH -> this.gravityRelative(Direction.DOWN);
	            case SOUTH -> this.gravityRelative(Direction.UP);
	            default -> this.gravityRelative(pDirection);
	        };
	        case WEST -> switch (pDirection)
	        {
	            case UP -> this.gravityRelative(Direction.EAST);
	            case DOWN -> this.gravityRelative(Direction.WEST);
	            case WEST -> this.gravityRelative(Direction.DOWN);
	            case EAST -> this.gravityRelative(Direction.UP);
	            default -> this.gravityRelative(pDirection);
	        };
	        case EAST -> switch (pDirection)
	        {
	            case UP -> this.gravityRelative(Direction.WEST);
	            case DOWN -> this.gravityRelative(Direction.EAST);
	            case WEST -> this.gravityRelative(Direction.UP);
	            case EAST -> this.gravityRelative(Direction.DOWN);
	            default -> this.gravityRelative(pDirection);
	        };
	        default -> this.gravityRelative(pDirection);
	    };
	}
	
	public BlockPos gravityRelative(Direction pDirection) 
	{
		return new GravityBlockPos(this.getX() + pDirection.getStepX(), this.getY() + pDirection.getStepY(), this.getZ() + pDirection.getStepZ(), this.direction);
	}

	public BlockPos gravityRelative(Direction pDirection, int pDistance) 
	{
		return pDistance == 0 ? this : new GravityBlockPos(this.getX() + pDirection.getStepX() * pDistance, this.getY() + pDirection.getStepY() * pDistance, this.getZ() + pDirection.getStepZ() * pDistance, this.direction);
	}
}
