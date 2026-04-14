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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.solomonlib.lights.DynamicLights;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@Mixin(value = Minecraft.class, priority = -10000)
public class MixinMinecraft
{
	@Inject(method = "updateLevelInEngines", at = @At("HEAD"))
	private void updateLevelInEngines(ClientLevel level, CallbackInfo ci) 
	{
		DynamicLights.get().clearLightSources();
	}
}
