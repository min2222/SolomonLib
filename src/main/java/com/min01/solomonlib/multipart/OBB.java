package com.min01.solomonlib.multipart;

import java.util.List;
import java.util.function.BiConsumer;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import com.google.common.collect.ImmutableList;
import com.min01.solomonlib.multipart.EntityPartBuilder.OBBData;
import com.min01.solomonlib.util.SolomonUtil;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OBB
{
    //whether OBB is enabled or not, disabled obb doesn't perform any calculation and not visible;
    public boolean enabled = true;
    
    //whether OBB have collision or only raycasting for hit detection;
    public boolean collide;
    
    public Vec3 center;
    public Vec3 halfExtents;

    public final Quaterniond rotation;
    public final String path;
    
    public OBB(Vec3 center, Vec3 halfExtents, Quaterniond rotation, boolean enabled, boolean collide, String path)
    {
    	this.center = center;
    	this.halfExtents = halfExtents;
    	this.rotation = rotation;
    	this.enabled = enabled;
    	this.collide = collide;
    	this.path = path;
    }
    
    public OBB(OBBData data)
    {
    	this.center = data.center();
    	this.halfExtents = data.halfExtents();
    	this.rotation = data.rotation();
    	this.enabled = data.enabled();
    	this.collide = data.collide();
    	this.path = data.path();
    }
    
    public void update(OBBData data)
    {
    	this.center = data.center();
    	this.halfExtents = data.halfExtents();
    	this.rotation.set(data.rotation());
    	this.enabled = data.enabled();
    	this.collide = data.collide();
    }
    
    public OBB inflate(double x, double y, double z)
    {
        return new OBB(this.center, this.halfExtents.add(x, y, z), this.rotation, this.enabled, this.collide, this.path);
    }
    
    public double distanceToSqr(Vec3 pVec) 
    {
    	Vec3 relative = pVec.subtract(this.center);
    	Vector3d relativeP = new Vector3d(relative.x, relative.y, relative.z);

        Quaterniond inverse = new Quaterniond(this.rotation);
        inverse.conjugate(); 
        
        Vector3d local = inverse.transform(relativeP);

        double x = Math.max(Math.abs(local.x) - this.halfExtents.x, 0.0D);
        double y = Math.max(Math.abs(local.y) - this.halfExtents.y, 0.0D);
        double z = Math.max(Math.abs(local.z) - this.halfExtents.z, 0.0D);

        return Mth.lengthSquared(x, y, z); 
    }
    
    public OBB move(double x, double y, double z)
    {
        return new OBB(this.center.add(x, y, z), this.halfExtents, this.rotation, this.enabled, this.collide, this.path);
    }
    
    public boolean contains(double x, double y, double z) 
    {
        double dx = x - this.center.x;
        double dy = y - this.center.y;
        double dz = z - this.center.z;
        Vector3d vector = new Vector3d();
        this.rotation.transform(dx, dy, dz, vector);
        return this.getExtents().contains(vector.x, vector.y, vector.z);
    }
    
    public AABB getExtents() 
    {
        return new AABB(this.halfExtents.scale(-1.0F), this.halfExtents);
    }
    
    public static void getOBBEntities(Level level, AABB pBoundingBox, BiConsumer<Entity, COBB> consumer)
    {
    	for(Entity entity : SolomonUtil.getAllEntities(level))
    	{
			if(!(entity.getBoundingBox() instanceof COBB cobb) || !cobb.intersects(pBoundingBox))
			{
    			continue;
			}
			consumer.accept(entity, cobb);
    	}
    }
    
    public static List<OBB> getOBBEntityCollisions(Level level, AABB pCollisionBox) 
    {
    	if(pCollisionBox.getSize() < 1.0E-7D)
        {
        	return List.of();
        } 
        else 
        {
    		ImmutableList.Builder<OBB> builder = ImmutableList.builder();
    		getOBBEntities(level, pCollisionBox, (t, u) -> 
    		{
				for(OBB obb : u.obbs) 
				{
		            if(obb.collide && obb.enabled)
		            {
		                builder.add(obb);
		            }
		        }
    		});
    		return builder.build();
        }
    }
}
