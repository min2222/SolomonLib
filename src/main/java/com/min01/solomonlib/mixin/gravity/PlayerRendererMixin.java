package com.min01.solomonlib.mixin.gravity;

import com.min01.solomonlib.gravity.GravityAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin 
{
	@Redirect(method = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"))
	private Vec3 modify_setupTransforms_Vec3d_0(AbstractClientPlayer instance, float partialTick)
	{
		Vec3 viewVector = instance.getViewVector(partialTick);

		Direction gravityDirection = GravityAPI.getGravityDirection(instance);
		if(gravityDirection == Direction.DOWN) 
		{
			return viewVector;
		}

		return RotationUtil.vecWorldToPlayer(viewVector, gravityDirection);
	}
}
