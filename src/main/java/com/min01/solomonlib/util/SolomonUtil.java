package com.min01.solomonlib.util;

import com.min01.solomonlib.gravity.GravityZoneManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class SolomonUtil
{
    public static Direction getBlockGravityDirection(Level level, BlockPos pos)
    {
        return GravityZoneManager.getDirection(level, pos);
    }

    public static Direction getEntityZoneDirection(Entity entity)
    {
        return GravityZoneManager.getDirection(entity.level, entity.blockPosition());
    }

    public static boolean isBlockUpsideDown(Level level, BlockPos pos)
    {
        return GravityZoneManager.getDirection(level, pos) == Direction.UP;
    }
}
