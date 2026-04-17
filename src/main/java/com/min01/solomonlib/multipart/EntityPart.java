package com.min01.solomonlib.multipart;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;

/**
 * Represents a hit box of an entity
 */
public final class EntityPart 
{
    private boolean changed = true;
    private double offX, offY, offZ;
    private double x, y, z;
    private final AABB box;
    private double px, py, pz;
    private QuaternionD rotation;
    private @Nullable EntityPart parent;
    public boolean collide;
    private @Nullable OrientedBox cachedBox;

    EntityPart(@Nullable EntityPart parent, AABB box, boolean center, double offX, double offY, double offZ)
    {
        this.parent = parent;
        this.offX = offX;
        this.offY = offY;
        this.offZ = offZ;
        this.rotation = QuaternionD.IDENTITY;
        if(center)
        {
        	box = box.move(-box.minX - box.getXsize() / 2, -box.minY - box.getYsize() / 2, -box.minZ - box.getZsize() / 2);
        }
        this.box = box;
        this.setX(0.0);
        this.setY(0.0);
        this.setZ(0.0);
    }

    void setParent(@Nullable EntityPart parent) 
    {
        this.parent = parent;
    }
    
    public void setCollide(boolean collide)
    {
        this.collide = collide;
        this.changed = true;
    }

    public void setOffX(double offX)
    {
        this.offX = offX;
        this.changed = true;
    }

    public void setOffY(double offY) 
    {
        this.offY = offY;
        this.changed = true;
    }

    public void setOffZ(double offZ)
    {
        this.offZ = offZ;
        this.changed = true;
    }

    /**
     * @param x X coordinate relative to parent
     */
    public void setX(double x)
    {
        this.x = x + this.offX;
        this.changed = true;
    }

    /**
     * @param y Y coordinate relative to parent
     */
    public void setY(double y)
    {
        this.y = y + this.offY;
        this.changed = true;
    }

    /**
     * @param z Z coordinate relative to parent
     */
    public void setZ(double z) 
    {
        this.z = z + this.offZ;
        this.changed = true;
    }

    /**
     * @param px X coordinate of point this part should be rotated around
     */
    public void setPivotX(double px)
    {
        this.px = px;
        this.changed = true;
    }

    /**
     * @param py X coordinate of point this part should be rotated around
     */
    public void setPivotY(double py)
    {
        this.py = py;
        this.changed = true;
    }

    /**
     * @param pz X coordinate of point this part should be rotated around
     */
    public void setPivotZ(double pz) 
    {
        this.pz = pz;
        this.changed = true;
    }

    public void setRotation(QuaternionD rotation)
    {
        this.rotation = rotation;
        this.changed = true;
    }

    public void rotate(QuaternionD quaternion)
    {
    	this.rotation = this.rotation.hamiltonProduct(quaternion);
        this.changed = true;
    }

    public void rotate(double pitch, double yaw, double roll, boolean degrees)
    {
    	this.rotation = this.rotation.hamiltonProduct(new QuaternionD(pitch, yaw, roll, degrees));
        this.changed = true;
    }

    public void setRotation(double pitch, double yaw, double roll, boolean degrees) 
    {
    	this.rotation = new QuaternionD(pitch, yaw, roll, degrees);
    	this.changed = true;
    }

    void setChanged(boolean changed) 
    {
        this.changed = changed;
    }

    boolean isChanged() 
    {
        return this.changed;
    }

    /**
     * @return Oriented box represented by this EntityPart after all transformations have been applied
     */
    public OrientedBox getBox()
    {
        if(this.cachedBox == null || this.changed)
        {
            OrientedBox orientedBox = new OrientedBox(this.box, this.collide);
            OrientedBox child = this.transformChild(orientedBox);
            child.collide = this.collide;
            this.cachedBox = child;
            this.changed = false;
        }
        return this.cachedBox;
    }

    private OrientedBox transformChild(OrientedBox orientedBox)
    {
        if(this.parent != null)
        {
            orientedBox = this.parent.transformChild(orientedBox);
        }
        return orientedBox.transform(this.x, this.y, this.z, this.px, this.py, this.pz, this.rotation);
    }
    
    public static EntityPart read(FriendlyByteBuf buf) 
    {
        boolean hasParent = buf.readBoolean();
        EntityPart parent = hasParent ? read(buf) : null;
        
        double minX = buf.readDouble();
        double minY = buf.readDouble();
        double minZ = buf.readDouble();
        double maxX = buf.readDouble();
        double maxY = buf.readDouble();
        double maxZ = buf.readDouble();
        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        
        double offX = buf.readDouble();
        double offY = buf.readDouble();
        double offZ = buf.readDouble();
        
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        
        double px = buf.readDouble();
        double py = buf.readDouble();
        double pz = buf.readDouble();
        
        double qx = buf.readDouble();
        double qy = buf.readDouble();
        double qz = buf.readDouble();
        double qw = buf.readDouble();
        QuaternionD rotation = new QuaternionD(qx, qy, qz, qw);
        
        boolean collide = buf.readBoolean();
        boolean isChanged = buf.readBoolean();
        
        EntityPart part = new EntityPart(parent, box, false, offX, offY, offZ);
        part.setX(x - offX);
        part.setY(y - offY);
        part.setZ(z - offZ);
        part.setPivotX(px);
        part.setPivotY(py);
        part.setPivotZ(pz);
        part.setRotation(rotation);
        part.setCollide(collide);
        part.setChanged(isChanged);
        return part;
    }

    public static void write(FriendlyByteBuf buf, EntityPart part) 
    {
        boolean hasParent = part.parent != null;
        buf.writeBoolean(hasParent);
        if(hasParent)
        {
            write(buf, part.parent);
        }
        
        AABB box = part.box;
        buf.writeDouble(box.minX);
        buf.writeDouble(box.minY);
        buf.writeDouble(box.minZ);
        buf.writeDouble(box.maxX);
        buf.writeDouble(box.maxY);
        buf.writeDouble(box.maxZ);
        
        buf.writeDouble(part.offX);
        buf.writeDouble(part.offY);
        buf.writeDouble(part.offZ);
        
        buf.writeDouble(part.x);
        buf.writeDouble(part.y);
        buf.writeDouble(part.z);
        
        buf.writeDouble(part.px);
        buf.writeDouble(part.py);
        buf.writeDouble(part.pz);
        
        QuaternionD rotation = part.rotation;
        buf.writeDouble(rotation.getX());
        buf.writeDouble(rotation.getY());
        buf.writeDouble(rotation.getZ());
        buf.writeDouble(rotation.getW());
        
        buf.writeBoolean(part.collide);
        buf.writeBoolean(part.changed);
    }
}