package com.min01.solomonlib.mixin.gravity;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Entity 
{
    public AbstractArrowMixin(EntityType<?> type, Level world) 
    {
        super(type, world);
    }
    
    @ModifyVariable(method = "Lnet/minecraft/world/entity/projectile/AbstractArrow;tick()V", at = @At(value = "STORE"), ordinal = 0)
    public Vec3 tick(Vec3 modify) 
    {
        modify = new Vec3(modify.x, modify.y + 0.05, modify.z);
        modify = RotationUtil.vecWorldToPlayer(modify, GravityAPI.getGravityDirection(this));
        modify = new Vec3(modify.x, modify.y - 0.05, modify.z);
        modify = RotationUtil.vecPlayerToWorld(modify, GravityAPI.getGravityDirection(this));
        return modify;
    }
    
    @ModifyConstant(method = "Lnet/minecraft/world/entity/projectile/AbstractArrow;tick()V", constant = @Constant(doubleValue = 0.05000000074505806))
    private double multiplyGravity(double constant) 
    {
        return constant * GravityAPI.getGravityStrength(this);
    }
}
