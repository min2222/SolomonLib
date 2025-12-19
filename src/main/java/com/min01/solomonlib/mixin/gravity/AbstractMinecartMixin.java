package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.min01.solomonlib.gravity.GravityAPI;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends Entity
{
    public AbstractMinecartMixin(EntityType<?> type, Level world) 
    {
        super(type, world);
    }
    
    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"), index = 1)
    private double multiplyGravity(double x)
    {
        return x * GravityAPI.getGravityStrength(this);
    }
}
