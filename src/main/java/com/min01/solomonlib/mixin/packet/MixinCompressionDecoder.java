package com.min01.solomonlib.mixin.packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.network.CompressionDecoder;

@Mixin(value = CompressionDecoder.class, priority = -30000)
public class MixinCompressionDecoder
{
    @ModifyExpressionValue(method = "decode", at = @At(value = "CONSTANT", args = "intValue=8388608"))
	private int decode(int old) 
	{
		return 2_147_483_647;
	}
}
