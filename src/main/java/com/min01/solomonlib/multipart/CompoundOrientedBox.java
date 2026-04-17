package com.min01.solomonlib.multipart;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Iterators;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

//https://github.com/CERBON-MODS/CERBONs-API/blob/1.20.1/src/main/java/com/cerbon/cerbons_api/api/multipart_entities/util/CompoundOrientedBox.java
public class CompoundOrientedBox extends AABB implements Iterable<OrientedBox> 
{
    public final Collection<OrientedBox> boxes;

    public CompoundOrientedBox(AABB bounds, Collection<OrientedBox> boxes)
    {
        this(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ, boxes);
    }

    public CompoundOrientedBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Collection<OrientedBox> boxes) 
    {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.boxes = boxes;
    }

    @Override
    public AABB inflate(double x, double y, double z)
    {
    	AABB aabb = super.inflate(x, y, z);
        List<OrientedBox> orientedBoxes = new ObjectArrayList<>(this.boxes.size());
        for(OrientedBox box : this.boxes)
        {
            orientedBoxes.add(box.expand(x, y, z));
        }
        return new CompoundOrientedBox(aabb, orientedBoxes);
    }

    @Override
    public AABB move(double x, double y, double z) 
    {
    	AABB aabb = super.move(x, y, z);
        List<OrientedBox> orientedBoxes = new ObjectArrayList<>(this.boxes.size());
        for(OrientedBox box : this.boxes)
        {
            orientedBoxes.add(box.offset(x, y, z));
        }
        return new CompoundOrientedBox(aabb, orientedBoxes);
    }

    @Override
    public AABB move(BlockPos blockPos) 
    {
        return this.move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public Optional<Vec3> clip(final @NotNull Vec3 min, final @NotNull Vec3 max)
    {
        double t = Double.MAX_VALUE;
        for(OrientedBox box : this.boxes) 
        {
            double tmp = box.raycast(min, max);
            if(tmp != -1)
            {
                t = Math.min(t, tmp);
            }
        }
        if(t != Double.MAX_VALUE) 
        {
            double d = max.x - min.x;
            double e = max.y - min.y;
            double f = max.z - min.z;
            return Optional.of(min.add(t * d, t * e, t * f));
        }
        return Optional.empty();
    }
    
    @Override
    public Iterator<OrientedBox> iterator() 
    {
        return Iterators.unmodifiableIterator(this.boxes.iterator());
    }

    @Override
    public boolean intersects(double minX, double minY,  double minZ,  double maxX, double maxY, double maxZ)
    {
        return this.intersects(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }
    
    @Override
    public boolean intersects(AABB box) 
    {
        for(OrientedBox orientedBox : this.boxes)
        {
            if(orientedBox.intersects(box))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(double x, double y, double z) 
    {
        for(OrientedBox box : this.boxes) 
        {
            if(box.contains(x, y, z))
            {
                return true;
            }
        }
        return false;
    }

    public CompoundOrientedBox withBounds(AABB bounds) 
    {
        return new CompoundOrientedBox(bounds, new ObjectArrayList<>(this.boxes));
    }
}
