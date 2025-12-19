package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.min01.solomonlib.gravity.GravityAPI;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownPotion;

@Mixin(ThrownPotion.class)
public class ThrownPotionMixin {
    @ModifyReturnValue(method = "getGravity", at = @At("RETURN"))
    private float multiplyGravity(float original) {
        return original * (float) GravityAPI.getGravityStrength(((Entity) (Object) this));
    }
}
