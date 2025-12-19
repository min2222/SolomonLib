package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(AreaEffectCloud.class)
public abstract class AreaEffectCloudMixin extends Entity
{
    @Shadow
    public abstract boolean isWaiting();
    
    @Shadow
    public abstract float getRadius();

    public AreaEffectCloudMixin(EntityType<?> type, Level world)
    {
        super(type, world);
    }
    
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void modify_move_multiply_0(Level instance, ParticleOptions particle, double x, double y, double z, double dx, double dy, double dz, Operation<Void> original)
    {
        boolean bl = this.isWaiting();
        float f = this.getRadius();
        
        float g;
        if(bl)
        {
            g = 0.2F;
        }
        else 
        {
            g = f;
        }
        
        float h = this.random.nextFloat() * 6.2831855F;
        float k = Mth.sqrt(this.random.nextFloat()) * g;
        
        double d = this.getX();
        double e = this.getY();
        double l = this.getZ();
        Vec3 modify = RotationUtil.vecWorldToPlayer(d, e, l, GravityAPI.getGravityDirection(this));
        d = modify.x + (double) (Mth.cos(h) * k);
        e = modify.y;
        l = modify.z + (double) (Mth.sin(h) * k);
        modify = RotationUtil.vecPlayerToWorld(d, e, l, GravityAPI.getGravityDirection(this));

        original.call(instance, particle, modify.x, modify.y, modify.z, dx, dy, dz);
    }
}
