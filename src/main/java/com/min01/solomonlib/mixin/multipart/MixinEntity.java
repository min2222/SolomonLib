package com.min01.solomonlib.mixin.multipart;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.min01.solomonlib.multipart.CompoundOrientedBox;
import com.min01.solomonlib.multipart.IMultipart;
import com.min01.solomonlib.multipart.OrientedBox;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(value = Entity.class, priority = -20000)
public class MixinEntity
{
    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void getBoundingBox(CallbackInfoReturnable<AABB> cir)
    {
        if((Entity)(Object)this instanceof IMultipart multipart)
        {
            cir.setReturnValue(multipart.getCompoundBoundingBox(cir.getReturnValue()));
        }
    }
    
    @ModifyVariable(method = "collide", at = @At("HEAD"), argsOnly = true)
    private Vec3 collide(Vec3 originalMovement) 
    {
        Entity entity = (Entity)(Object)this;
        float hw = entity.getBbWidth() / 2.0f;
        AABB aabb = new AABB(entity.getX() - hw, entity.getY(), entity.getZ() - hw, entity.getX() + hw, entity.getY() + entity.getBbHeight(), entity.getZ() + hw);
        List<OrientedBox> obbList = this.getOBBEntityCollisions(entity.level, entity, aabb.expandTowards(originalMovement));
        if(obbList.isEmpty())
        {
            return originalMovement;
        }
        return this.collideWithOrientedBoxes(originalMovement, aabb, obbList);
    }

    private Vec3 collideWithOrientedBoxes(Vec3 pDeltaMovement, AABB pEntityBB, List<OrientedBox> obbs)
    {
        if(obbs.isEmpty()) 
        {
            return pDeltaMovement;
        }

        Vec3 totalMtv = Vec3.ZERO;
        for(OrientedBox obb : obbs)
        {
            Vec3 mtv = obb.getDepenetrationVector(pEntityBB);
            if(mtv.lengthSqr() > 1.0E-7)
            {
                totalMtv = totalMtv.add(mtv);
            }
        }
        if(totalMtv.lengthSqr() > 1.0E-7)
        {
            pEntityBB = pEntityBB.move(totalMtv);
            pDeltaMovement = pDeltaMovement.add(totalMtv);
        }

        double d0 = pDeltaMovement.x;
        double d1 = pDeltaMovement.y;
        double d2 = pDeltaMovement.z;
        
        if(d1 != 0.0D) 
        {
        	d1 = OrientedBox.collide(Direction.Axis.Y, pEntityBB, obbs, d1);
        	if(d1 != 0.0D) 
        	{
        		pEntityBB = pEntityBB.move(0.0D, d1, 0.0D);
        	}
        }

        boolean flag = Math.abs(d0) < Math.abs(d2);
        if(flag && d2 != 0.0D) 
        {
        	d2 = OrientedBox.collide(Direction.Axis.Z, pEntityBB, obbs, d2);
        	if(d2 != 0.0D)
        	{
        		pEntityBB = pEntityBB.move(0.0D, 0.0D, d2);
        	}
        }

        if(d0 != 0.0D) 
        {
        	d0 = OrientedBox.collide(Direction.Axis.X, pEntityBB, obbs, d0);
        	if(!flag && d0 != 0.0D)
        	{
        		pEntityBB = pEntityBB.move(d0, 0.0D, 0.0D);
        	}
        }

        if(!flag && d2 != 0.0D) 
        {
        	d2 = OrientedBox.collide(Direction.Axis.Z, pEntityBB, obbs, d2);
        }

        return new Vec3(d0, d1, d2);
    }
    
    private List<OrientedBox> getOBBEntityCollisions(Level level, @Nullable Entity pEntity, AABB pCollisionBox) 
    {
    	if(pCollisionBox.getSize() < 1.0E-7D)
        {
        	return List.of();
        } 
        else 
        {
        	Predicate<Entity> predicate = EntitySelector.NO_SPECTATORS.and(t -> t instanceof IMultipart multipart && !multipart.getCollidePart().isEmpty());
        	if(pEntity != null)
        	{
        		predicate = predicate.and(t -> !pEntity.isPassengerOfSameVehicle(t));
        	}
        	List<Entity> list = level.getEntities(pEntity, pCollisionBox, predicate);
        	if(list.isEmpty())
        	{
        		return List.of();
        	}
        	else
        	{
        		ImmutableList.Builder<OrientedBox> builder = ImmutableList.builderWithExpectedSize(list.size() * 8);
        		for(Entity entity : list) 
        		{
        			if(entity.getBoundingBox() instanceof CompoundOrientedBox compoundBox)
        			{
        				compoundBox.boxes.stream().filter(t -> t.collide).forEach(builder::add);
        			}
        		}
        		return builder.build();
        	}
        }
    }
}
