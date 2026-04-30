package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.min01.solomonlib.gravity.GravityAPI;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;

@Mixin(ForgeHooks.class)
public class MixinForgeHooks
{
    @WrapOperation(method = "Lnet/minecraftforge/common/ForgeHooks;onLivingBreathe(Lnet/minecraft/world/entity/LivingEntity;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;", ordinal = 0))
    private static BlockPos wrap_baseTick_containing_0(double x, double y, double z, Operation<BlockPos> original, @Local(argsOnly = true) LivingEntity entity)
    {
        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
        if(gravityDirection == Direction.DOWN)
        {
            return original.call(x, y, z);
        }
        return BlockPos.containing(entity.getEyePosition());
    }
}
