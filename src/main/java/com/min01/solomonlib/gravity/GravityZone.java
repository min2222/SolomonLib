package com.min01.solomonlib.gravity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public final class GravityZone
{
    public static final GravityZone DEFAULT = new GravityZone(Direction.DOWN, 1.0);

    private final Direction gravityDirection;
    private final double gravityStrength;

    public GravityZone(Direction gravityDirection, double gravityStrength)
    {
        this.gravityDirection = gravityDirection;
        this.gravityStrength  = gravityStrength;
    }

    public static GravityZone of(Direction direction)
    {
        if(direction == Direction.DOWN)
        {
            return DEFAULT;
        }
        return new GravityZone(direction, 1.0);
    }

    public static GravityZone of(Direction direction, double strength)
    {
        if(direction == Direction.DOWN && strength == 1.0)
        {
            return DEFAULT;
        }
        return new GravityZone(direction, strength);
    }

    public Direction getDirection()
    {
        return this.gravityDirection;
    }

    public double getStrength()
    {
        return this.gravityStrength;
    }

    public boolean isDefault()
    {
        return this.gravityDirection == Direction.DOWN && this.gravityStrength == 1.0;
    }

    public CompoundTag toNbt()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("direction", this.gravityDirection.getName());
        tag.putDouble("strength",  this.gravityStrength);
        return tag;
    }

    public static GravityZone fromNbt(CompoundTag tag)
    {
        Direction dir = Direction.byName(tag.getString("direction"));
        if(dir == null)
        {
            dir = Direction.DOWN;
        }
        double strength = tag.contains("strength") ? tag.getDouble("strength") : 1.0;
        return of(dir, strength);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof GravityZone other))
        {
            return false;
        }
        return this.gravityDirection == other.gravityDirection && Double.compare(this.gravityStrength, other.gravityStrength) == 0;
    }

    @Override
    public int hashCode()
    {
        return 31 * this.gravityDirection.ordinal() + Double.hashCode(this.gravityStrength);
    }

    @Override
    public String toString()
    {
        return "GravityZone{dir=" + this.gravityDirection + ", strength=" + this.gravityStrength + "}";
    }
}
