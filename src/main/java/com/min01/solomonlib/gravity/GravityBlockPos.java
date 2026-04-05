package com.min01.solomonlib.gravity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class GravityBlockPos extends BlockPos
{
    private final Direction gravityDirection;

    public GravityBlockPos(Vec3i vec3i, Direction gravityDirection)
    {
        super(vec3i);
        this.gravityDirection = gravityDirection;
    }

    public GravityBlockPos(int x, int y, int z, Direction gravityDirection)
    {
        super(x, y, z);
        this.gravityDirection = gravityDirection;
    }

    public GravityBlockPos(BlockPos pos, Direction gravityDirection)
    {
        super(pos);
        this.gravityDirection = gravityDirection;
    }

    public Direction getGravityDirection()
    {
        return this.gravityDirection;
    }

    @Override
    public BlockPos relative(Direction localDirection)
    {
        Direction worldDirection = RotationUtil.dirPlayerToWorld(localDirection, this.gravityDirection);
        return this.gravityRelative(worldDirection);
    }

    @Override
    public BlockPos relative(Direction localDirection, int pDistance)
    {
        if(pDistance == 0)
        {
            return this;
        }
        Direction worldDirection = RotationUtil.dirPlayerToWorld(localDirection, this.gravityDirection);
        return this.gravityRelative(worldDirection, pDistance);
    }

    public GravityBlockPos gravityRelative(Direction worldDirection)
    {
        return new GravityBlockPos(this.getX() + worldDirection.getStepX(), this.getY() + worldDirection.getStepY(), this.getZ() + worldDirection.getStepZ(), this.gravityDirection);
    }

    public GravityBlockPos gravityRelative(Direction worldDirection, int distance)
    {
        return new GravityBlockPos(this.getX() + worldDirection.getStepX() * distance, this.getY() + worldDirection.getStepY() * distance, this.getZ() + worldDirection.getStepZ() * distance, this.gravityDirection);
    }
}
