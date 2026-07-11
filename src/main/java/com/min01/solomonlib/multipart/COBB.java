package com.min01.solomonlib.multipart;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Iterators;
import com.min01.kaijuc.KaijuC;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class COBB extends AABB implements Iterable<OBB> 
{
    public final Collection<OBB> obbs;

    public COBB(AABB aabb, Collection<OBB> obbs)
    {
        this(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, obbs);
    }

    public COBB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Collection<OBB> obbs) 
    {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.obbs = obbs;
    }

    @Override
    public AABB inflate(double x, double y, double z)
    {
    	AABB aabb = super.inflate(x, y, z);
        List<OBB> obbs = new ObjectArrayList<>(this.obbs.size());
        for(OBB obb : this.obbs)
        {
        	if(!obb.enabled)
        	{
        		continue;
        	}
        	obbs.add(obb.inflate(x, y, z));
        }
        return new COBB(aabb, obbs);
    }

    @Override
    public boolean contains(double x, double y, double z) 
    {
        for(OBB obb : this.obbs)
        {
        	if(!obb.enabled)
        	{
        		continue;
        	}
        	if(obb.contains(x, y, z))
        	{
        		return true;
        	}
        }
        return false;
    }

    @Override
    public AABB move(BlockPos blockPos) 
    {
    	//redirected;
        return this.move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public AABB move(double x, double y, double z) 
    {
    	AABB aabb = super.move(x, y, z);
        List<OBB> obbs = new ObjectArrayList<>(this.obbs.size());
        for(OBB obb : this.obbs)
        {
        	if(!obb.enabled)
        	{
        		continue;
        	}
        	obbs.add(obb.move(x, y, z));
        }
        return new COBB(aabb, obbs);
    }

    @Override
    public Optional<Vec3> clip(Vec3 pFrom, Vec3 pTo)
    {
        return KaijuC.clip(this.obbs, pFrom, pTo, KaijuC.FILTER_ENABLED);
    }

    @Override
    public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
    	//redirected;
        return this.intersects(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }
    
    @Override
    public boolean intersects(AABB pOther) 
    {
    	return KaijuC.intersects(this.obbs, pOther, KaijuC.FILTER_ENABLED);
    }
    
    @Override
    public double distanceToSqr(Vec3 pVec) 
    {
    	double min = Double.MAX_VALUE;
        for(OBB obb : this.obbs) 
        {
            double dist = obb.distanceToSqr(pVec);
            min = Math.min(min, dist);
        }
        return min;
    }
    
    @Override
    public Iterator<OBB> iterator() 
    {
        return Iterators.unmodifiableIterator(this.obbs.iterator());
    }
}
