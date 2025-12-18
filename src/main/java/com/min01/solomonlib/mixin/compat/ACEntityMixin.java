package com.min01.solomonlib.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.bawnorton.mixinsquared.TargetHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

//https://github.com/Tfarcenim/GravityChanger/issues/2
@Mixin(value = Entity.class, priority = 1500)
public class ACEntityMixin
{
	@TargetHandler(mixin = "com.github.alexmodguy.alexscaves.mixin.EntityMixin", name = "ac_collide")
	@Inject(method = "@MixinSquared:Handler", at = @At(value = "HEAD"), cancellable = true)
	private void inject_adjustMovementForCollisions(Vec3 vec3, CallbackInfoReturnable<Vec3> cir, CallbackInfo ci)
	{
		//TODO find which mixin is cause problem instead of cancel entirely
		//com.min01.gravityapi.mixin.EntityMixin;
		ci.cancel();
	}
}
