package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.solomonlib.util.SolomonClientUtil;
import com.min01.solomonlib.util.SolomonUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = Block.class, priority = -10000)
public class MixinBlock 
{
	@Inject(method = "shouldRenderFace", at = @At("RETURN"), cancellable = true)
	private static void shouldRenderFace(BlockState p_152445_, BlockGetter p_152446_, BlockPos p_152447_, Direction p_152448_, BlockPos p_152449_, CallbackInfoReturnable<Boolean> cir)
	{
		if(SolomonUtil.isBlockUpsideDown(SolomonClientUtil.MC.level, p_152449_))
		{
			cir.setReturnValue(true);
		}
	}
}
