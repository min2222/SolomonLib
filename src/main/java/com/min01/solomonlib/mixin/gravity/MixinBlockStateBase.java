package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.solomonlib.gravity.GravityBlockPos;
import com.min01.solomonlib.gravity.GravityZoneManager;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(value = BlockBehaviour.BlockStateBase.class, priority = -10000)
public abstract class MixinBlockStateBase
{
    @Shadow
    protected abstract BlockState asState();

    @Inject(method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("RETURN"), cancellable = true)
    private void getShape(BlockGetter pLevel, BlockPos pPos, CollisionContext pContext, CallbackInfoReturnable<VoxelShape> cir)
    {
        if(!(pLevel instanceof Level level))
        {
            return;
        }

        Direction gravDir = GravityZoneManager.getDirection(level, pPos);
        if(gravDir == Direction.DOWN)
        {
            return;
        }

        VoxelShape shape = cir.getReturnValue();
        if(shape.isEmpty())
        {
            return;
        }

        cir.setReturnValue(rotateShape(shape, gravDir));
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void canSurvive(LevelReader pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir)
    {
        if(!(pLevel instanceof Level level))
        {
            return;
        }

        Direction gravDir = GravityZoneManager.getDirection(level, pPos);
        if(gravDir == Direction.DOWN)
        {
            return;
        }

        GravityBlockPos gravityPos = new GravityBlockPos(pPos, gravDir);
        cir.setReturnValue(this.asState().getBlock().canSurvive(this.asState(), pLevel, gravityPos));
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(Level pLevel, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir)
    {
        BlockPos pos = pHit.getBlockPos();
        Direction gravDir = GravityZoneManager.getDirection(pLevel, pos);
        if(gravDir == Direction.DOWN)
        {
            return;
        }

        GravityBlockPos gravityPos = new GravityBlockPos(pos, gravDir);
        cir.setReturnValue(this.asState().getBlock().use(this.asState(), pLevel, gravityPos, pPlayer, pHand, pHit));
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void updateShape(Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos, CallbackInfoReturnable<BlockState> cir)
    {
        if(!(pLevel instanceof Level level))
        {
            return;
        }

        Direction gravDir = GravityZoneManager.getDirection(level, pPos);
        if(gravDir == Direction.DOWN)
        {
            return;
        }

        GravityBlockPos gravityPos = new GravityBlockPos(pPos, gravDir);
        cir.setReturnValue(this.asState().getBlock().updateShape(this.asState(), pDirection, pNeighborState, pLevel, gravityPos, pNeighborPos));
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void neighborChanged(Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving, CallbackInfo ci)
    {
        Direction gravDir = GravityZoneManager.getDirection(pLevel, pPos);
        if(gravDir == Direction.DOWN)
        {
            return;
        }

        ci.cancel();
        GravityBlockPos gravityPos = new GravityBlockPos(pFromPos, gravDir);
        this.asState().getBlock().neighborChanged(this.asState(), pLevel, pPos, pBlock, gravityPos, pIsMoving);
    }

    private static VoxelShape rotateShape(VoxelShape original, Direction gravDir)
    {
        VoxelShape result = Shapes.empty();
        for(AABB box : original.toAabbs())
        {
            result = Shapes.or(result, Shapes.create(rotateBox(box, gravDir)));
        }
        return result;
    }

    private static AABB rotateBox(AABB box, Direction gravDir)
    {
        return switch(gravDir)
        {
            case UP -> new AABB(box.minX, 1.0 - box.maxY, box.minZ, box.maxX, 1.0 - box.minY, box.maxZ);
            case NORTH -> new AABB(box.minX, 1.0 - box.maxZ, box.minY, box.maxX, 1.0 - box.minZ, box.maxY);
            case SOUTH -> new AABB(box.minX, box.minZ, 1.0 - box.maxY, box.maxX, box.maxZ, 1.0 - box.minY);
            case WEST -> new AABB(box.minY, 1.0 - box.maxZ, 1.0 - box.maxX, box.maxY, 1.0 - box.minZ, 1.0 - box.minX);
            case EAST -> new AABB(1.0 - box.maxY, 1.0 - box.maxZ, box.minX, 1.0 - box.minY, 1.0 - box.minZ, box.maxX);
            default -> box;
        };
    }
}
