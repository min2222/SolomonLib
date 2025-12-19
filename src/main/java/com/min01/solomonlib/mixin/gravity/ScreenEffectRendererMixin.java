package com.min01.solomonlib.mixin.gravity;

import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin 
{
	@Inject(method = "getOverlayBlock", at = @At("HEAD"), cancellable = true, remap = false)
	private static void inject_getInWallBlockState(Player player, CallbackInfoReturnable<Pair<BlockState, BlockPos>> cir)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(player);
		if(gravityDirection == Direction.DOWN)
			return;

		cir.cancel();

		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		Vec3 eyePos = player.getEyePosition();
		Vector3f multipliers = RotationUtil.vecPlayerToWorld(player.getBbWidth() * 0.8F, 0.1F, player.getBbWidth() * 0.8F, gravityDirection);
		for(int i = 0; i < 8; ++i) 
		{
			double d = eyePos.x + (double) (((float) ((i >> 0) % 2) - 0.5F) * multipliers.x());
			double e = eyePos.y + (double) (((float) ((i >> 1) % 2) - 0.5F) * multipliers.y());
			double f = eyePos.z + (double) (((float) ((i >> 2) % 2) - 0.5F) * multipliers.z());
			mutable.set(d, e, f);
			BlockState blockState = player.level().getBlockState(mutable);
			if(blockState.getRenderShape() != RenderShape.INVISIBLE && blockState.isViewBlocking(player.level(), mutable))
			{
				cir.setReturnValue(Pair.of(blockState, mutable.immutable()));
			}
		}

		cir.setReturnValue(null);
	}
}
