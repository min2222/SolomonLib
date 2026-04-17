package com.min01.solomonlib.multipart;

import net.minecraft.world.phys.Vec3;

public class Matrix3d 
{
    public static final Matrix3d IDENTITY = new Matrix3d(QuaternionD.IDENTITY);
    public static final Vec3[] IDENTITY_BASIS = IDENTITY.getBasis();
    public double m00;
    public double m01;
    public double m02;
    public double m10;
    public double m11;
    public double m12;
    public double m20;
    public double m21;
    public double m22;

    public Matrix3d(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22) 
    {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
    }

    public Matrix3d(QuaternionD quaternion)
    {
        double x = quaternion.getX();
        double y = quaternion.getY();
        double z = quaternion.getZ();
        double w = quaternion.getW();
        double x2 = 2.0F * x * x;
        double y2 = 2.0F * y * y;
        double z2 = 2.0F * z * z;
        this.m00 = 1.0F - y2 - z2;
        this.m11 = 1.0F - z2 - x2;
        this.m22 = 1.0F - x2 - y2;
        double xy = x * y;
        double yz = y * z;
        double zx = z * x;
        double xw = x * w;
        double yw = y * w;
        double zw = z * w;
        this.m10 = 2.0F * (xy + zw);
        this.m01 = 2.0F * (xy - zw);
        this.m20 = 2.0F * (zx - yw);
        this.m02 = 2.0F * (zx + yw);
        this.m21 = 2.0F * (yz + xw);
        this.m12 = 2.0F * (yz - xw);
    }

    public Matrix3d multiply(Matrix3d other)
    {
        double a00 = this.m00 * other.m00 + this.m01 * other.m10 + this.m02 * other.m20;
        double a01 = this.m00 * other.m01 + this.m01 * other.m11 + this.m02 * other.m21;
        double a02 = this.m00 * other.m02 + this.m01 * other.m12 + this.m02 * other.m22;
        double a10 = this.m10 * other.m00 + this.m11 * other.m10 + this.m12 * other.m20;
        double a11 = this.m10 * other.m01 + this.m11 * other.m11 + this.m12 * other.m21;
        double a12 = this.m10 * other.m02 + this.m11 * other.m12 + this.m12 * other.m22;
        double a20 = this.m20 * other.m00 + this.m21 * other.m10 + this.m22 * other.m20;
        double a21 = this.m20 * other.m01 + this.m21 * other.m11 + this.m22 * other.m21;
        double a22 = this.m20 * other.m02 + this.m21 * other.m12 + this.m22 * other.m22;
        return new Matrix3d(a00, a01, a02, a10, a11, a12, a20, a21, a22);
    }

    public Matrix3d invert() 
    {
        double m00 = this.m00;
        double m01 = this.m10;
        double m02 = this.m20;
        double m10 = this.m01;
        double m11 = this.m11;
        double m12 = this.m21;
        double m20 = this.m02;
        double m21 = this.m12;
        double m22 = this.m22;
        return new Matrix3d(m00, m01, m02, m10, m11, m12, m20, m21, m22);
    }

    public Vec3[] getBasis()
    {
        return new Vec3[]{new Vec3(this.m00, this.m10, this.m20), new Vec3(this.m01, this.m11, this.m21), new Vec3(this.m02, this.m12, this.m22)};
    }

    public Vec3 transform(Vec3 v) 
    {
        return new Vec3(this.m00 * v.x + this.m01 * v.y + this.m02 * v.z, this.m10 * v.x + this.m11 * v.y + this.m12 * v.z, this.m20 * v.x + this.m21 * v.y + this.m22 * v.z);
    }

    public Vec3 transform(double x, double y, double z) 
    {
        return new Vec3(this.m00 * x + this.m01 * y + this.m02 * z, this.m10 * x + this.m11 * y + this.m12 * z, this.m20 * x + this.m21 * y + this.m22 * z);
    }

    public double transformX(double x, double y, double z) 
    {
        return this.m00 * x + this.m01 * y + this.m02 * z;
    }

    public double transformY(double x, double y, double z)
    {
        return this.m10 * x + this.m11 * y + this.m12 * z;
    }

    public double transformZ(double x, double y, double z)
    {
        return this.m20 * x + this.m21 * y + this.m22 * z;
    }
}

