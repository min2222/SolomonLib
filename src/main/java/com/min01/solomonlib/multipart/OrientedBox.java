package com.min01.solomonlib.multipart;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OrientedBox
{
    private final Vec3 center;
    private final Vec3 halfExtents;
    private final QuaternionD rotation;
    private AABB extents;
    private Matrix3d matrix;
    private Matrix3d inverse;
    private Vec3[] vertices;
    private Vec3[] basis;
    public boolean collide;

    public OrientedBox(AABB box, boolean collide) 
    {
    	this.center = box.getCenter();
    	this.halfExtents = new Vec3(box.getXsize() / 2, box.getYsize() / 2, box.getZsize() / 2);
    	this.rotation = QuaternionD.IDENTITY;
    	this.collide = collide;
    }

    public OrientedBox(Vec3 center, Vec3 halfExtents, QuaternionD rotation)
    {
        this.center = center;
        this.halfExtents = halfExtents;
        this.rotation = rotation;
    }

    public OrientedBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, QuaternionD rotation)
    {
    	this.center = new Vec3((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
        this.halfExtents = new Vec3((maxX - minX) / 2, (maxY - minY) / 2, (maxZ - minZ) / 2);
        this.rotation = rotation;
    }

    private OrientedBox(Vec3 center, Vec3 halfExtents, QuaternionD rotation, Matrix3d matrix, Matrix3d inverse, Vec3[] basis)
    {
        this.center = center;
        this.halfExtents = halfExtents;
        this.rotation = rotation;
        this.matrix = matrix;
        this.inverse = inverse;
        this.basis = basis;
    }

    public Matrix3d getMatrix() 
    {
        if(this.matrix == null)
        {
        	this.matrix = new Matrix3d(this.rotation);
        }
        return this.matrix;
    }

    public Matrix3d getInverse() 
    {
        if(this.inverse == null) 
        {
        	this.inverse = this.getMatrix().invert();
        }
        return this.inverse;
    }

    public AABB getExtents() 
    {
        if(this.extents == null) 
        {
        	this.extents = new AABB(this.halfExtents.multiply(-1, -1, -1), this.halfExtents);
        }
        return this.extents;
    }

    public Vec3[] getBasis()
    {
        if(this.basis == null) 
        {
        	this.basis = this.getMatrix().getBasis();
        }
        return this.basis;
    }

    public OrientedBox rotate(QuaternionD quaternion)
    {
        if(QuaternionD.IDENTITY.equals(quaternion))
        {
            return this;
        }
        return new OrientedBox(this.center, this.halfExtents, this.rotation.hamiltonProduct(quaternion));
    }

    public OrientedBox translate(double x, double y, double z) 
    {
        if(x == 0 && y == 0 && z == 0) 
        {
            return this;
        }
        Matrix3d matrix = this.getMatrix();
        double transX = matrix.transformX(x, y, z);
        double transY = matrix.transformY(x, y, z);
        double transZ = matrix.transformZ(x, y, z);
        return new OrientedBox(this.center.add(transX, transY, transZ), this.halfExtents, this.rotation, matrix, this.inverse, this.basis);
    }

    public OrientedBox transform(double x, double y, double z, double pivotX, double pivotY, double pivotZ, QuaternionD quaternion) 
    {
        Vec3 vec = this.getMatrix().transform(x - pivotX, y - pivotY, z - pivotZ);
        boolean bl = quaternion.equals(QuaternionD.IDENTITY);
        return new OrientedBox(this.center.add(vec), this.halfExtents, this.rotation.hamiltonProduct(quaternion), bl ? this.matrix : null, bl ? this.inverse : null, bl ? this.basis : null).translate(pivotX, pivotY, pivotZ);
    }

    public QuaternionD getRotation() 
    {
        return this.rotation;
    }

    public Vec3 getCenter() 
    {
        return this.center;
    }

    public Vec3 getHalfExtents() 
    {
        return this.halfExtents;
    }

    public OrientedBox offset(double x, double y, double z)
    {
        return new OrientedBox(this.center.add(x, y, z), this.halfExtents, this.rotation, this.matrix, this.inverse, this.basis);
    }

    public void computeVertices() 
    {
        AABB box = this.getExtents();
        Vec3[] vertices = getVertices(box);
        this.vertices = new Vec3[8];
        Matrix3d matrix = this.getMatrix();
        for(int i = 0; i < vertices.length; i++)
        {
            this.vertices[i] = matrix.transform(vertices[i]).add(this.center);
        }
    }

    public static Vec3[] getVertices(AABB box) 
    {
        Vec3[] vertices = new Vec3[8];
        int index = 0;
        Direction.AxisDirection[] axisDirections = Direction.AxisDirection.values();
        for(Direction.AxisDirection x : axisDirections)
        {
            for(Direction.AxisDirection y : axisDirections) 
            {
                for(Direction.AxisDirection z : axisDirections) 
                {
                    vertices[index++] = new Vec3(getPoint(box, x, Direction.Axis.X), getPoint(box, y, Direction.Axis.Y), getPoint(box, z, Direction.Axis.Z));
                }
            }
        }
        return vertices;
    }

    private static double getPoint(AABB box, Direction.AxisDirection direction, Direction.Axis axis)
    {
        return direction == Direction.AxisDirection.NEGATIVE ? box.min(axis) : box.max(axis);
    }
    
    public static double collide(Direction.Axis pMovementAxis, AABB pCollisionBox, Iterable<OrientedBox> pPossibleHits, double pDesiredOffset)
    {
        for(OrientedBox obb : pPossibleHits) 
        {
        	if(Math.abs(pDesiredOffset) < 1.0E-7D) 
        	{
        		return 0.0D;
        	}
        	pDesiredOffset = obb.collide(pMovementAxis, pCollisionBox, pDesiredOffset);
        }
        return pDesiredOffset;
    }
    
    public double collide(Direction.Axis axis, AABB aabb, double desiredMove) 
    {
        if(Math.abs(desiredMove) < 1.0E-7D) 
        {
            return 0.0D;
        }
        
        if(this.intersects(aabb)) 
        {
            return desiredMove;
        }

        double sign = Math.signum(desiredMove);
        Vec3 axisVec = switch (axis) 
        {
            case X -> new Vec3(sign, 0, 0);
            case Y -> new Vec3(0, sign, 0);
            case Z -> new Vec3(0, 0, sign);
        };

        AABB finalMovedAABB = aabb.move(axisVec.scale(Math.abs(desiredMove)));
        if(!this.intersects(finalMovedAABB)) 
        {
            return desiredMove;
        }

        double low = 0.0;
        double high = Math.abs(desiredMove);

        for(int i = 0; i < 10; i++)
        {
            double mid = (low + high) / 2.0;
            if (mid == low || mid == high) 
            {
                break;
            }

            AABB testAABB = aabb.move(axisVec.scale(mid));
            if(this.intersects(testAABB)) 
            {
                high = mid;
            } 
            else
            {
                low = mid;
            }
        }

        return low * sign;
    }
    
    public Vec3 getDepenetrationVector(AABB other) 
    {
        if(!this.intersects(other)) 
        {
            return Vec3.ZERO;
        }

        if (this.vertices == null) this.computeVertices();
        Vec3[] obbVerts = this.vertices;
        Vec3[] aabbVerts = getVertices(other);

        double minOverlap = Double.MAX_VALUE;
        Vec3 pushAxis = Vec3.ZERO;

        Vec3[] obbBasis  = this.getBasis();
        Vec3[] aabbBasis = Matrix3d.IDENTITY_BASIS;

        int totalAxes = obbBasis.length + aabbBasis.length;
        Vec3[] axes = new Vec3[totalAxes + 9];
        int idx = 0;
        for (Vec3 a : obbBasis)  axes[idx++] = a;
        for (Vec3 a : aabbBasis) axes[idx++] = a;

        for(Vec3 a : obbBasis)
        {
            for(Vec3 b : aabbBasis) 
            {
                axes[idx++] = cross(a, b);
            }
        }

        for(Vec3 axis : axes)
        {
            if(axis.lengthSqr() < 1.0E-9) 
            	continue;

            double min1 = Double.MAX_VALUE, max1 = -Double.MAX_VALUE;
            for(Vec3 v : obbVerts)
            {
                double proj = v.dot(axis);
                min1 = Math.min(min1, proj);
                max1 = Math.max(max1, proj);
            }

            double min2 = Double.MAX_VALUE, max2 = -Double.MAX_VALUE;
            for(Vec3 v : aabbVerts) 
            {
                double proj = v.dot(axis);
                min2 = Math.min(min2, proj);
                max2 = Math.max(max2, proj);
            }

            double overlap = Math.min(max1, max2) - Math.max(min1, min2);

            if(overlap <= 0)
            {
                return Vec3.ZERO;
            }

            if(overlap < minOverlap) 
            {
                minOverlap = overlap;
                pushAxis = axis;
            }
        }

        Vec3 centerDiff = other.getCenter().subtract(this.center);
        if(centerDiff.dot(pushAxis) < 0)
        {
            pushAxis = pushAxis.scale(-1);
        }

        return pushAxis.normalize().scale(minOverlap + 1.0E-4);
    }
    
    public boolean intersects(AABB other)
    {
        return this.intersects(getVertices(other));
    }

    public boolean intersects(Vec3[] otherVertices)
    {
        if(this.vertices == null)
        {
            this.computeVertices();
        }
        Vec3[] vertices1 = this.vertices;
        Vec3[] normals1 = this.getBasis();
        for(Vec3 normal : normals1)
        {
            if(!sat(normal, vertices1, otherVertices))
            {
                return false;
            }
        }
        Vec3[] normals2 = Matrix3d.IDENTITY_BASIS;
        for(Vec3 normal : normals2) 
        {
            if(!sat(normal, vertices1, otherVertices))
            {
                return false;
            }
        }
        for(int i = 0; i < normals1.length; i++)
        {
            for(int j = 0; j < normals2.length; j++) 
            {
                Vec3 normal = cross(normals1[i], normals2[j]);
                if (normal.lengthSqr() < 1.0E-9) {
                    continue;
                }
                if(!sat(normal, vertices1, otherVertices))
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean sat(Vec3 normal, Vec3[] vertices1, Vec3[] vertices2)
    {
        double min1 = Double.MAX_VALUE;
        double max1 = -Double.MAX_VALUE;
        for(Vec3 d : vertices1)
        {
        	if(d != null)
        	{
                double v = d.dot(normal);
                min1 = Math.min(min1, v);
                max1 = Math.max(max1, v);
        	}
        }
        double min2 = Double.MAX_VALUE;
        double max2 = -Double.MAX_VALUE;
        for(Vec3 vec3d : vertices2)
        {
            double v = vec3d.dot(normal);
            min2 = Math.min(min2, v);
            max2 = Math.max(max2, v);
        }
        return min1 <= min2 && min2 <= max1 || min2 <= min1 && min1 <= max2;
    }

    public static Vec3 cross(Vec3 first, Vec3 second)
    {
        return new Vec3(first.y * second.z - first.z * second.y, first.z * second.x - first.x * second.z, first.x * second.y - first.y * second.x);
    }

    public double raycast(Vec3 start, Vec3 end)
    {
        Matrix3d inverse = this.getInverse();
        Vec3 d = inverse.transform(start.x - this.center.x, start.y - this.center.y, start.z - this.center.z);
        Vec3 e = inverse.transform(end.x - this.center.x, end.y - this.center.y, end.z - this.center.z);
        return this.raycast0(d, e);
    }

    private double raycast0(Vec3 start, Vec3 end) 
    {
        double d = end.x - start.x;
        double e = end.y - start.y;
        double f = end.z - start.z;
        double[] t = new double[]{1};
        Direction direction = traceCollisionSide(this.getExtents(), start, t, d, e, f);
        if(direction != null)
        {
            return t[0];
        }
        return -1;
    }

    @Nullable
    private static Direction traceCollisionSide(AABB box, Vec3 intersectingVector, double[] traceDistanceResult, double xDelta, double yDelta, double zDelta) 
    {
        Direction approachDirection = null;
        if(xDelta > 1.0E-7D)
        {
            approachDirection = traceCollisionSide(traceDistanceResult, approachDirection, xDelta, yDelta, zDelta, box.minX, box.minY, box.maxY, box.minZ, box.maxZ, Direction.WEST, intersectingVector.x, intersectingVector.y, intersectingVector.z);
        }
        else if(xDelta < -1.0E-7D)
        {
            approachDirection = traceCollisionSide(traceDistanceResult, approachDirection, xDelta, yDelta, zDelta, box.maxX, box.minY, box.maxY, box.minZ, box.maxZ, Direction.EAST, intersectingVector.x, intersectingVector.y, intersectingVector.z);
        }
        if(yDelta > 1.0E-7D)
        {
            approachDirection = traceCollisionSide(traceDistanceResult, approachDirection, yDelta, zDelta, xDelta, box.minY, box.minZ, box.maxZ, box.minX, box.maxX, Direction.DOWN, intersectingVector.y, intersectingVector.z, intersectingVector.x);
        }
        else if(yDelta < -1.0E-7D)
        {
            approachDirection = traceCollisionSide(traceDistanceResult, approachDirection, yDelta, zDelta, xDelta, box.maxY, box.minZ, box.maxZ, box.minX, box.maxX, Direction.UP, intersectingVector.y, intersectingVector.z, intersectingVector.x);
        }
        if(zDelta > 1.0E-7D)
        {
            approachDirection = traceCollisionSide(traceDistanceResult, approachDirection, zDelta, xDelta, yDelta, box.minZ, box.minX, box.maxX, box.minY, box.maxY, Direction.NORTH, intersectingVector.z, intersectingVector.x, intersectingVector.y);
        }
        else if(zDelta < -1.0E-7D)
        {
            approachDirection = traceCollisionSide(traceDistanceResult, approachDirection, zDelta, xDelta, yDelta, box.maxZ, box.minX, box.maxX, box.minY, box.maxY, Direction.SOUTH, intersectingVector.z, intersectingVector.x, intersectingVector.y);
        }
        return approachDirection;
    }

    @Nullable
    private static Direction traceCollisionSide(double[] traceDistanceResult, Direction approachDirection, double xDelta, double yDelta, double zDelta, double begin, double minX, double maxX, double minZ, double maxZ, Direction resultDirection, double startX, double startY, double startZ)
    {
        double d = (begin - startX) / xDelta;
        double e = startY + d * yDelta;
        double f = startZ + d * zDelta;
        if(0.0D < d && d < traceDistanceResult[0] && minX - 1.0E-7D < e && e < maxX + 1.0E-7D && minZ - 1.0E-7D < f && f < maxZ + 1.0E-7D)
        {
            traceDistanceResult[0] = d;
            return resultDirection;
        } 
        else
        {
            return approachDirection;
        }
    }

    public boolean contains(double x, double y, double z) 
    {
        x -= this.center.x;
        y -= this.center.y;
        z -= this.center.z;
        double transX = this.getMatrix().transformX(x, y, z);
        double transY = this.getMatrix().transformY(x, y, z);
        double transZ = this.getMatrix().transformZ(x, y, z);
        return this.getExtents().contains(transX, transY, transZ);
    }

    public double getMax(Direction.Axis axis) 
    {
        Matrix3d matrix = this.getMatrix();
        return switch(axis) 
        {
            case X -> Math.abs(matrix.m00) * this.halfExtents.x + Math.abs(matrix.m01) * this.halfExtents.y + Math.abs(matrix.m02) * this.halfExtents.z + this.center.x;
            case Y -> Math.abs(matrix.m10) * this.halfExtents.x + Math.abs(matrix.m11) * this.halfExtents.y + Math.abs(matrix.m12) * this.halfExtents.z + this.center.y;
            case Z -> Math.abs(matrix.m20) * this.halfExtents.x + Math.abs(matrix.m21) * this.halfExtents.y + Math.abs(matrix.m22) * this.halfExtents.z + this.center.z;
        };
    }

    public double getMin(Direction.Axis axis)
    {
        Matrix3d matrix = this.getMatrix();
        return switch(axis) 
        {
            case X -> -(Math.abs(matrix.m00) * this.halfExtents.x + Math.abs(matrix.m01) * this.halfExtents.y + Math.abs(matrix.m02) * this.halfExtents.z) + this.center.x;
            case Y -> -(Math.abs(matrix.m10) * this.halfExtents.x + Math.abs(matrix.m11) * this.halfExtents.y + Math.abs(matrix.m12) * this.halfExtents.z) + this.center.y;
            case Z -> -(Math.abs(matrix.m20) * this.halfExtents.x + Math.abs(matrix.m21) * this.halfExtents.y + Math.abs(matrix.m22) * this.halfExtents.z) + this.center.z;
        };
    }

    public OrientedBox expand(double x, double y, double z)
    {
        if(x == 0 && y == 0 && z == 0)
        {
            return this;
        }
        return new OrientedBox(this.center, this.halfExtents.add(x / 2, y / 2, z / 2), this.rotation, this.matrix, this.inverse, this.basis);
    }
}
