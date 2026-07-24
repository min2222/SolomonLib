package com.min01.solomonlib.mixin.multipart;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.kaijuc.KaijuC;
import com.min01.solomonlib.multipart.EntityPartBuilder;
import com.min01.solomonlib.multipart.IMultipart;
import com.min01.solomonlib.multipart.OBB;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public class MixinEntity
{
    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void getBoundingBox(CallbackInfoReturnable<AABB> cir)
    {
    	//change entity hitbox to compound obb;
    	Entity entity = (Entity) (Object) this;
        if(entity instanceof IMultipart multipart)
        {
        	AABB aabb = cir.getReturnValue();
			EntityPartBuilder builder = multipart.getPartBuilder();
            cir.setReturnValue(builder.build(aabb));
        }
    }

    @ModifyVariable(method = "collide", at = @At("HEAD"), argsOnly = true)
    private Vec3 collide(Vec3 pVec) 
    {
    	//collision logic;
    	Entity entity = (Entity) (Object) this;
        AABB aabb = entity.getBoundingBox();
        List<OBB> list = OBB.getOBBEntityCollisions(entity.level, aabb.expandTowards(pVec));
        if(!list.isEmpty()) 
        {
            AABB sweep = new AABB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
            return KaijuC.collide(list, sweep, pVec, KaijuC.FILTER_COLLIDE);
        }
    	return pVec;
    }
}
