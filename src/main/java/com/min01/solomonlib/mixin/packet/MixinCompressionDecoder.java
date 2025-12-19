package com.min01.solomonlib.mixin.packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.network.CompressionDecoder;

@Mixin(value = CompressionDecoder.class, priority = -10000)
public class MixinCompressionDecoder
{
	@ModifyConstant(method = "decode", constant = @Constant(intValue = CompressionDecoder.MAXIMUM_UNCOMPRESSED_LENGTH))
	private int decode(int old) 
	{
		return 2_147_483_647;
	}
}
