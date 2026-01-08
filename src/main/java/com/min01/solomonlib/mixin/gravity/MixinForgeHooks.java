package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.min01.solomonlib.gravity.GravityAPI;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;

@Mixin(ForgeHooks.class)
public class MixinForgeHooks
{
    @Redirect(method = "Lnet/minecraftforge/common/ForgeHooks;onLivingBreathe(Lnet/minecraft/world/entity/LivingEntity;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;", ordinal = 0))
    private static BlockPos redirect_baseTick_new_0(double x, double y, double z, LivingEntity entity) 
    {
        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
        if(gravityDirection == Direction.DOWN) 
        {
            return BlockPos.containing(x, y, z);
        }
        return BlockPos.containing(entity.getEyePosition());
    }
}
