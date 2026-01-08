package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.solomonlib.gravity.GravityBlockPos;
import com.min01.solomonlib.util.SolomonUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(value = BlockBehaviour.BlockStateBase.class, priority = -10000)
public abstract class MixinBlockStateBase
{
	@Inject(method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("RETURN"), cancellable = true)
    private void getShape(BlockGetter pLevel, BlockPos pPos, CollisionContext pContext, CallbackInfoReturnable<VoxelShape> cir)
    {
    	VoxelShape shape = cir.getReturnValue();
    	if(pLevel instanceof Level level)
    	{
            if(!shape.isEmpty() && SolomonUtil.isBlockUpsideDown(level, pPos))
            {
                VoxelShape flipped = Shapes.empty();
                for(AABB box : shape.toAabbs()) 
                {
                    double minY = 1.0 - box.maxY;
                    double maxY = 1.0 - box.minY;
                    AABB flippedBox = new AABB(box.minX, minY, box.minZ, box.maxX, maxY, box.maxZ);
                    flipped = Shapes.or(flipped, Shapes.create(flippedBox));
                }
                cir.setReturnValue(flipped);
            }
    	}
    }
	
	@SuppressWarnings("deprecation")
	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void canSurvive(LevelReader p_60711_, BlockPos p_60712_, CallbackInfoReturnable<Boolean> cir)
    {
    	if(p_60711_ instanceof Level level)
    	{
    		BlockStateBase base = BlockStateBase.class.cast(this);
    		if(SolomonUtil.isBlockUpsideDown(level, p_60712_))
    		{
    			GravityBlockPos gravityPos = new GravityBlockPos(p_60712_, Direction.UP);
    			cir.setReturnValue(base.getBlock().canSurvive(this.asState(), p_60711_, gravityPos));
    		}
    	}
    }

	@SuppressWarnings("deprecation")
	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(Level p_60665_, Player p_60666_, InteractionHand p_60667_, BlockHitResult p_60668_, CallbackInfoReturnable<InteractionResult> cir)
    {
    	BlockPos pos = p_60668_.getBlockPos();
		BlockStateBase base = BlockStateBase.class.cast(this);
		if(SolomonUtil.isBlockUpsideDown(p_60665_, pos))
		{
			GravityBlockPos gravityPos = new GravityBlockPos(pos, Direction.UP);
	        cir.setReturnValue(base.getBlock().use(this.asState(), p_60665_, gravityPos, p_60666_, p_60667_, p_60668_));
		}
    }

	@SuppressWarnings("deprecation")
	@Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void updateShape(Direction p_60729_, BlockState p_60730_, LevelAccessor p_60731_, BlockPos p_60732_, BlockPos p_60733_, CallbackInfoReturnable<BlockState> cir)
    {
		if(p_60731_ instanceof Level level)
		{
    		BlockStateBase base = BlockStateBase.class.cast(this);
			if(SolomonUtil.isBlockUpsideDown(level, p_60732_))
			{
    			GravityBlockPos gravityPos = new GravityBlockPos(p_60732_, Direction.UP);
    	        cir.setReturnValue(base.getBlock().updateShape(this.asState(), p_60729_, p_60730_, p_60731_, gravityPos, p_60733_));
			}
		}
    }

	@SuppressWarnings("deprecation")
	@Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void neighborChanged(Level p_60691_, BlockPos p_60692_, Block p_60693_, BlockPos p_60694_, boolean p_60695_, CallbackInfo ci)
    {
		BlockStateBase base = BlockStateBase.class.cast(this);
		if(SolomonUtil.isBlockUpsideDown(p_60691_, p_60694_))
		{
			ci.cancel();
			GravityBlockPos gravityPos = new GravityBlockPos(p_60694_, Direction.UP);
			base.getBlock().neighborChanged(this.asState(), p_60691_, p_60692_, p_60693_, gravityPos, p_60695_);
		}
    }
	
	@Shadow
    protected abstract BlockState asState();
}
