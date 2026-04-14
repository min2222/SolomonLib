package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.solomonlib.gravity.GravityZoneManager;
import com.min01.solomonlib.util.SolomonClientUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = Block.class, priority = -10000)
public class MixinBlock
{
    @Inject(method = "shouldRenderFace", at = @At("RETURN"), cancellable = true)
    private static void shouldRenderFace(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pFace, BlockPos pAdjacentPos, CallbackInfoReturnable<Boolean> cir)
    {
        if(SolomonClientUtil.MC.level == null)
        {
            return;
        }
        if(GravityZoneManager.getDirection(SolomonClientUtil.MC.level, pAdjacentPos) != Direction.DOWN)
        {
            cir.setReturnValue(true);
        }
    }
}
