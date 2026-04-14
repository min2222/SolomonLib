package com.min01.solomonlib.mixin.lights;

/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 * Copyright © 2024 toni (https://github.com/txnimc/SodiumDynamicLights)
 *
 * Portions of this file are derived from SodiumDynamicLights / LambDynLights and are
 * used under the MIT License. The full license text is included in README.md at the
 * repository root.
 */

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.solomonlib.lights.DynamicLights;
import com.min01.solomonlib.lights.IDynamicLight;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

@Mixin(value = EntityRenderer.class, priority = -10000)
public class MixinEntityRenderer<T extends Entity>
{
	@Inject(method = "getBlockLightLevel", at = @At("RETURN"), cancellable = true)
	private void getBlockLightLevel(T entity, BlockPos pos, CallbackInfoReturnable<Integer> cir)
	{
		int vanilla = cir.getReturnValueI();
		int entityLuminance = ((IDynamicLight) entity).getLuminance();
		int posLuminance = (int) DynamicLights.get().getDynamicLightLevel(pos);
		cir.setReturnValue(Math.max(Math.max(vanilla, entityLuminance), posLuminance));
	}
}
