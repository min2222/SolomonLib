package com.min01.solomonlib.mixin.gravity;


import com.min01.solomonlib.gravity.GravityAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.Vec3;

@Mixin(ThrowableProjectile.class)
public abstract class ThrowableProjectileMixin {
    
    @Shadow
    protected abstract float getGravity();

    /*@Override
    public Direction gravitychanger$getAppliedGravityDirection() {
        return GravityChangerAPI.getGravityDirection((ThrownEntity)(Object)this);
    }*/
    
    @ModifyVariable(
        method = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;tick()V",
        at = @At(
            value = "STORE"
        )
        , ordinal = 0
    )
    public Vec3 tick(Vec3 modify) {
        //if(this instanceof RotatableEntityAccessor) {
        modify = new Vec3(modify.x, modify.y + this.getGravity(), modify.z);
        modify = RotationUtil.vecWorldToPlayer(modify, GravityAPI.getGravityDirection((ThrowableProjectile) (Object) this));
        modify = new Vec3(modify.x, modify.y - this.getGravity(), modify.z);
        modify = RotationUtil.vecPlayerToWorld(modify, GravityAPI.getGravityDirection((ThrowableProjectile) (Object) this));
        // }
        return modify;
    }
    
    /*@WrapOperation(
        method = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;)V",
            ordinal = 0
        )
    )
    private static ThrowableProjectile modifyargs_init_init_0(ThrowableProjectile instance, EntityType<? extends ThrowableProjectile> type, double x, double y, double z, Level world, Operation<ThrowableProjectile> original, @Local LivingEntity owner) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(owner);
        if (gravityDirection == Direction.DOWN) return original.call(instance, type, x, y, z, world);
        
        Vec3 pos = owner.getEyePosition().subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.10000000149011612D, 0.0D, gravityDirection));
        return original.call(instance, type, pos.x, pos.y, pos.z, world);
    }*/
    
    @ModifyReturnValue(method = "getGravity", at = @At("RETURN"))
    private float multiplyGravity(float original) {
        return original * (float) GravityAPI.getGravityStrength(((Entity) (Object) this));
    }
}
