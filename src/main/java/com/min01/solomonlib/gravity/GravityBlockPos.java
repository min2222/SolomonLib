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
	public BlockPos relative(Direction p_121946_, int p_121949_) 
	{
	    return switch (this.direction) 
	    {
	        case UP -> switch (p_121946_) 
	        {
	            case UP -> this.gravityRelative(Direction.DOWN, p_121949_);
	            case DOWN -> this.gravityRelative(Direction.UP, p_121949_);
	            case WEST -> this.gravityRelative(Direction.WEST, p_121949_);
	            case EAST -> this.gravityRelative(Direction.EAST, p_121949_);
	            default -> this.gravityRelative(p_121946_, p_121949_);
	        };
	        case NORTH -> switch (p_121946_)
	        {
	            case UP -> this.gravityRelative(Direction.SOUTH, p_121949_);
	            case DOWN -> this.gravityRelative(Direction.NORTH, p_121949_);
	            case NORTH -> this.gravityRelative(Direction.UP, p_121949_);
	            case SOUTH -> this.gravityRelative(Direction.DOWN, p_121949_);
	            default -> this.gravityRelative(p_121946_, p_121949_);
	        };
	        case SOUTH -> switch (p_121946_)
	        {
	            case UP -> this.gravityRelative(Direction.NORTH, p_121949_);
	            case DOWN -> this.gravityRelative(Direction.SOUTH, p_121949_);
	            case NORTH -> this.gravityRelative(Direction.DOWN, p_121949_);
	            case SOUTH -> this.gravityRelative(Direction.UP, p_121949_);
	            default -> this.gravityRelative(p_121946_, p_121949_);
	        };
	        case WEST -> switch (p_121946_) 
	        {
	            case UP -> this.gravityRelative(Direction.EAST, p_121949_);
	            case DOWN -> this.gravityRelative(Direction.WEST, p_121949_);
	            case WEST -> this.gravityRelative(Direction.DOWN, p_121949_);
	            case EAST -> this.gravityRelative(Direction.UP, p_121949_);
	            default -> this.gravityRelative(p_121946_, p_121949_);
	        };
	        case EAST -> switch (p_121946_) 
	        {
	            case UP -> this.gravityRelative(Direction.WEST, p_121949_);
	            case DOWN -> this.gravityRelative(Direction.EAST, p_121949_);
	            case WEST -> this.gravityRelative(Direction.UP, p_121949_);
	            case EAST -> this.gravityRelative(Direction.DOWN, p_121949_);
	            default -> this.gravityRelative(p_121946_, p_121949_);
	        };
	        default -> this.gravityRelative(p_121946_, p_121949_);
	    };
	}

	@Override
	public BlockPos relative(Direction p_121946_)
	{
	    return switch (this.direction)
	    {
	        case UP -> switch (p_121946_) 
	        {
	            case UP -> this.gravityRelative(Direction.DOWN);
	            case DOWN -> this.gravityRelative(Direction.UP);
	            case WEST -> this.gravityRelative(Direction.WEST);
	            case EAST -> this.gravityRelative(Direction.EAST);
	            default -> this.gravityRelative(p_121946_);
	        };
	        case NORTH -> switch (p_121946_) 
	        {
	            case UP -> this.gravityRelative(Direction.SOUTH);
	            case DOWN -> this.gravityRelative(Direction.NORTH);
	            case NORTH -> this.gravityRelative(Direction.UP);
	            case SOUTH -> this.gravityRelative(Direction.DOWN);
	            default -> this.gravityRelative(p_121946_);
	        };
	        case SOUTH -> switch (p_121946_)
	        {
	            case UP -> this.gravityRelative(Direction.NORTH);
	            case DOWN -> this.gravityRelative(Direction.SOUTH);
	            case NORTH -> this.gravityRelative(Direction.DOWN);
	            case SOUTH -> this.gravityRelative(Direction.UP);
	            default -> this.gravityRelative(p_121946_);
	        };
	        case WEST -> switch (p_121946_)
	        {
	            case UP -> this.gravityRelative(Direction.EAST);
	            case DOWN -> this.gravityRelative(Direction.WEST);
	            case WEST -> this.gravityRelative(Direction.DOWN);
	            case EAST -> this.gravityRelative(Direction.UP);
	            default -> this.gravityRelative(p_121946_);
	        };
	        case EAST -> switch (p_121946_)
	        {
	            case UP -> this.gravityRelative(Direction.WEST);
	            case DOWN -> this.gravityRelative(Direction.EAST);
	            case WEST -> this.gravityRelative(Direction.UP);
	            case EAST -> this.gravityRelative(Direction.DOWN);
	            default -> this.gravityRelative(p_121946_);
	        };
	        default -> this.gravityRelative(p_121946_);
	    };
	}
	
	public BlockPos gravityRelative(Direction p_121946_) 
	{
		return new GravityBlockPos(this.getX() + p_121946_.getStepX(), this.getY() + p_121946_.getStepY(), this.getZ() + p_121946_.getStepZ(), this.direction);
	}

	public BlockPos gravityRelative(Direction p_121948_, int p_121949_) 
	{
		return p_121949_ == 0 ? this : new GravityBlockPos(this.getX() + p_121948_.getStepX() * p_121949_, this.getY() + p_121948_.getStepY() * p_121949_, this.getZ() + p_121948_.getStepZ() * p_121949_, this.direction);
	}
}
