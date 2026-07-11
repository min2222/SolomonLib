package com.min01.solomonlib.mixin.multipart;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.solomonlib.multipart.OBB;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@Mixin(Level.class)
public class MixinLevel
{
    @Inject(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void getEntities(@Nullable Entity pEntity, AABB pBoundingBox, Predicate<? super Entity> pPredicate, CallbackInfoReturnable<List<Entity>> cir)
    {
    	Level level = (Level) (Object) this;
    	List<Entity> list = cir.getReturnValue();
    	OBB.getOBBEntities(level, pBoundingBox, (t, u) -> 
    	{
	        if(t != pEntity && pPredicate.test(t)) 
	        {
	        	list.add(t);
	        }
    	});
        cir.setReturnValue(list);
    }
}
