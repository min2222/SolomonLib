package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.solomonlib.gravity.GravityBakedModelWrapper;
import com.min01.solomonlib.gravity.zone.GravityZoneManager;
import com.min01.solomonlib.util.SolomonClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

@Mixin(value = BlockRenderDispatcher.class, priority = -10000)
public class MixinBlockRenderDispatcher
{
    @Shadow
    @Final
    private ModelBlockRenderer modelRenderer;

    @Inject(method = "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;Lnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void renderBatched(BlockState pState, BlockPos pPos, BlockAndTintGetter pLevel, PoseStack pPoseStack, VertexConsumer pConsumer, boolean pCheckSides, RandomSource pRandom, ModelData modelData, RenderType renderType, CallbackInfo ci)
    {
        if(SolomonClientUtil.MC.level == null)
        {
        	return;
        }
        Direction gravDir = GravityZoneManager.getDirection(SolomonClientUtil.MC.level, pPos);
        if(gravDir == Direction.DOWN)
        {
        	return;
        }
        ci.cancel();
        try
        {
            if(pState.getRenderShape() == RenderShape.MODEL)
            {
                GravityBakedModelWrapper wrapper = new GravityBakedModelWrapper(this.getBlockModel(pState), gravDir);
                this.modelRenderer.tesselateBlock(pLevel, wrapper, pState, pPos, pPoseStack, pConsumer, pCheckSides, pRandom, pState.getSeed(pPos), OverlayTexture.NO_OVERLAY, modelData, renderType);
            }
        }
        catch(Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating block in world");
            CrashReportCategory cat = crashreport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(cat, pLevel, pPos, pState);
            throw new ReportedException(crashreport);
        }
    }

    @Shadow
    public BakedModel getBlockModel(BlockState state)
    {
        throw new IllegalStateException();
    }
}
