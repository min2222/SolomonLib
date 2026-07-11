package com.min01.kaijuc;

import java.util.Optional;

import com.min01.solomonlib.multipart.OBB;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class KaijuC 
{
    public static final int STRIDE = 12;
    public static final int FILTER_ALL = 0;
    public static final int FILTER_ENABLED = 1;
    public static final int FILTER_COLLIDE = 2;
    
    public double[] buffer = new double[0];
    public int count;
    public boolean dirty = true;
    
    public static boolean filter(OBB o, int filter) 
    {
        return switch(filter) {
            case FILTER_ALL -> true;
            case FILTER_ENABLED -> o.enabled;
            case FILTER_COLLIDE -> o.enabled && o.collide;
            default -> true;
        };
    }

    public static int count(Iterable<OBB> obbs, int filter) 
    {
        int n = 0;
        for(OBB o : obbs)
        {
        	if(filter(o, filter))
        	{
        		n++;
        	}
        }
        return n;
    }
    
    public static int pack(Iterable<OBB> obbs, int filter, double[] buf) 
    {
        int i = 0;
        for(OBB o : obbs) 
        {
            if(!filter(o, filter))
            {
            	continue;
            }
            writeOBB(buf, i++, o);
        }
        return i;
    }
    
    public static boolean intersects(OBB obb, AABB other)
    {
        double[] buf = new double[STRIDE];
        writeOBB(buf, 0, obb);
        double[] aabb = { other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ };
        return KaijuCNative.intersects(buf, 1, aabb);
    }
    
    public static double raycast(OBB obb, Vec3 from, Vec3 to)
    {
        double[] buf = new double[STRIDE];
        writeOBB(buf, 0, obb);
        double[] ray = { from.x, from.y, from.z, to.x, to.y, to.z };
        double t = KaijuCNative.raycast(buf, 1, ray);
        return t;
    }
    
    public static Optional<Vec3> clip(Iterable<OBB> obbs, Vec3 from, Vec3 to, int filter) 
    {
        int n = count(obbs, filter);
        if(n == 0)
        {
        	return Optional.empty();
        }
        double[] buf = new double[n * STRIDE];
        int c = pack(obbs, filter, buf);
        double[] ray = { from.x, from.y, from.z, to.x, to.y, to.z };
        double t = KaijuCNative.raycast(buf, c, ray);
        return t < 0.0 ? Optional.empty() : Optional.of(from.add(to.subtract(from).scale(t)));
    }
    
    public static boolean intersects(Iterable<OBB> obbs, AABB other, int filter) 
    {
        int n = count(obbs, filter);
        if(n == 0)
        {
        	return false;
        }
        double[] buf = new double[n * STRIDE];
        int c = pack(obbs, filter, buf);
        double[] aabb = { other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ };
        return KaijuCNative.intersects(buf, c, aabb);
    }
    
    public static Vec3 collide(Iterable<OBB> obbs, AABB box, Vec3 delta, int filter) 
    {
        int n = count(obbs, filter);
        if(n == 0)
        {
        	return delta;
        }
        double[] buf = new double[n * STRIDE];
        int c = pack(obbs, filter, buf);
        double[] aabb = { box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ };
        double[] d = { delta.x, delta.y, delta.z };
        double[] out = KaijuCNative.collide(buf, c, aabb, d);
        return new Vec3(out[0], out[1], out[2]);
    }
    
    public static void writeOBB(double[] buf, int index, OBB obb)
    {
        int i = index * STRIDE;
        buf[i] = obb.center.x;
        buf[i + 1] = obb.center.y;
        buf[i + 2] = obb.center.z;
        buf[i + 3] = obb.halfExtents.x;
        buf[i + 4] = obb.halfExtents.y;
        buf[i + 5] = obb.halfExtents.z;
        buf[i + 6] = obb.rotation.x;
        buf[i + 7] = obb.rotation.y;
        buf[i + 8] = obb.rotation.z;
        buf[i + 9] = obb.rotation.w;
        
        int flags = (obb.enabled ? 1 : 0) | (obb.collide ? 2 : 0);
        buf[i + 10] = flags;
    }
}
