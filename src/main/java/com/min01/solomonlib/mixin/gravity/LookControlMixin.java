package com.min01.solomonlib.mixin.gravity;


import com.min01.solomonlib.gravity.GravityAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.control.LookControl;

@Mixin(LookControl.class)
public abstract class LookControlMixin {
    @Redirect(
        method = "Lnet/minecraft/world/entity/ai/control/LookControl;getWantedY(Lnet/minecraft/world/entity/Entity;)D",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getEyeY()D",
            ordinal = 0
        )
    )
    private static double redirect_getLookingHeightForgetEyeY_0(Entity entity) {
        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return entity.getEyeY();
        }
        
        return entity.getEyePosition().y;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/ai/control/LookControl;setLookAt(Lnet/minecraft/world/entity/Entity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getX()D",
            ordinal = 0
        )
    )
    private double redirect_lookAt_getX_0_0(Entity entity) {
        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return entity.getX();
        }
        
        return entity.getEyePosition().x;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/ai/control/LookControl;setLookAt(Lnet/minecraft/world/entity/Entity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getZ()D",
            ordinal = 0
        )
    )
    private double redirect_lookAt_getZ_0_0(Entity entity) {
        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return entity.getZ();
        }
        
        return entity.getEyePosition().z;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/ai/control/LookControl;setLookAt(Lnet/minecraft/world/entity/Entity;FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getX()D",
            ordinal = 0
        )
    )
    private double redirect_lookAt_getX_0_1(Entity entity) {
        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return entity.getX();
        }
        
        return entity.getEyePosition().x;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/ai/control/LookControl;setLookAt(Lnet/minecraft/world/entity/Entity;FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getZ()D",
            ordinal = 0
        )
    )
    private double redirect_lookAt_getZ_0_1(Entity entity) {
        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return entity.getZ();
        }
        
        return entity.getEyePosition().z;
    }
}
