package com.min01.solomonlib.multipart;

import org.joml.Quaternionf;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public final class QuaternionD
{
    public static final QuaternionD IDENTITY = new QuaternionD(0, 0, 0, 1);
    private final double x;
    private final double y;
    private final double z;
    private final double w;

    public QuaternionD(double x, double y, double z, double w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public QuaternionD(Vec3 axis, double rotationAngle, boolean degrees)
    {
        if(degrees)
        {
            rotationAngle *= 0.017453292F;
        }
        double f = Math.sin(rotationAngle / 2.0);
        this.x = axis.x * f;
        this.y = axis.y * f;
        this.z = axis.z * f;
        this.w = Math.cos(rotationAngle / 2.0);
    }

    public QuaternionD(double pitch, double yaw, double roll, boolean degrees)
    {
        if(degrees)
        {
            pitch *= 0.017453292F;
            yaw *= 0.017453292F;
            roll *= 0.017453292F;
        }
        double f = Math.sin(0.5F * pitch);
        double g = Math.cos(0.5F * pitch);
        double h = Math.sin(0.5F * yaw);
        double i = Math.cos(0.5F * yaw);
        double j = Math.sin(0.5F * roll);
        double k = Math.cos(0.5F * roll);
        this.x = f * i * k + g * h * j;
        this.y = g * h * k - f * i * j;
        this.z = f * h * k + g * i * j;
        this.w = g * i * k - f * h * j;
    }

    public double getX() 
    {
        return this.x;
    }

    public double getY() 
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }

    public double getW()
    {
        return this.w;
    }
    
    public void write(FriendlyByteBuf buf)
    {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeDouble(this.w);
    }

    public static QuaternionD read(FriendlyByteBuf buf) 
    {
        return new QuaternionD(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public QuaternionD hamiltonProduct(QuaternionD other) 
    {
        double f = this.getX();
        double g = this.getY();
        double h = this.getZ();
        double i = this.getW();
        double j = other.getX();
        double k = other.getY();
        double l = other.getZ();
        double m = other.getW();
        return new QuaternionD(i * j + f * m + g * l - h * k, i * k - f * l + g * m + h * j, i * l + f * k - g * j + h * m, i * m - f * j - g * k - h * l);
    }

    public Quaternionf toFloatQuat() 
    {
        return new Quaternionf((float) this.x, (float) this.y, (float) this.z, (float) this.w);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || this.getClass() != o.getClass())
        {
            return false;
        }
        QuaternionD that = (QuaternionD) o;
        if(Double.compare(that.x, this.x) != 0)
        {
            return false;
        }
        if(Double.compare(that.y, this.y) != 0)
        {
            return false;
        }
        if(Double.compare(that.z, this.z) != 0)
        {
            return false;
        }
        return Double.compare(that.w, this.w) == 0;
    }

    @Override
    public int hashCode() 
    {
        int result;
        long temp;
        temp = Double.doubleToLongBits(this.x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.w);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
