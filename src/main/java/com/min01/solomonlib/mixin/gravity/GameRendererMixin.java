package com.min01.solomonlib.mixin.gravity;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationAnimation;
import com.min01.solomonlib.util.SolomonClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin
{
	@Shadow
	@Final
	private Camera mainCamera;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V", ordinal = 4, shift = Shift.AFTER), cancellable = true)
    private void renderLevel(float deltaTick, long limitTime, PoseStack poseStack, CallbackInfo ci)
    {
        Entity focusedEntity = this.mainCamera.getEntity();
        Direction gravityDirection = GravityAPI.getGravityDirection(focusedEntity);
        RotationAnimation animation = GravityAPI.getRotationAnimation(focusedEntity);
        long timeMs = focusedEntity.level.getGameTime() * 50 + (long) (deltaTick * 50);
        Quaternionf currentGravityRotation = animation.getCurrentGravityRotation(gravityDirection, timeMs);
        if(animation == null || (gravityDirection == Direction.DOWN && !animation.isInAnimation())) 
        {
            return;
        }
		if(animation.isInAnimation()) 
		{
			// make sure that frustum culling updates when running rotation animation
			SolomonClientUtil.MC.levelRenderer.needsUpdate();
		}
    	poseStack.mulPose(currentGravityRotation);
	}
}
