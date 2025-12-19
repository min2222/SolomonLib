package com.min01.solomonlib.mixin.gravity;

import com.min01.solomonlib.gravity.GravityAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.LlamaSpit;

@Mixin(LlamaSpit.class)
public class LlamaSpitMixin {
    @ModifyArg(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"
        ),
        index = 1
    )
    private double multiplyGravity(double x) {
        return x * GravityAPI.getGravityStrength(((Entity) (Object) this));
    }
}
