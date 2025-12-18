package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.gravityapi.api.GravityBlockPos;
import com.min01.solomonlib.util.SolomonUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(value = BlockPlaceContext.class, priority = -10000)
public abstract class MixinBlockPlaceContext extends UseOnContext
{
	public MixinBlockPlaceContext(Level p_43713_, Player p_43714_, InteractionHand p_43715_, ItemStack p_43716_, BlockHitResult p_43717_) 
	{
		super(p_43713_, p_43714_, p_43715_, p_43716_, p_43717_);
	}

	@Inject(method = "getClickedPos", at = @At("RETURN"), cancellable = true)
	private void getClickedPos(CallbackInfoReturnable<BlockPos> cir) 
	{
		BlockPos pos = cir.getReturnValue();
		if(SolomonUtil.isBlockUpsideDown(pos, this.getLevel()))
		{
			GravityBlockPos gravityPos = new GravityBlockPos(pos, Direction.UP);
			cir.setReturnValue(gravityPos);
		}
	}
}
