package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.solomonlib.gravity.GravityBakedModelWrapper;
import com.min01.solomonlib.util.SolomonClientUtil;
import com.min01.solomonlib.util.SolomonUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = BlockRenderDispatcher.class, priority = -10000)
public class MixinBlockRenderDispatcher 
{
	@Shadow
	@Final
	private ModelBlockRenderer modelRenderer;
	   
	@Inject(method = "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;Lnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V", at = @At("HEAD"), cancellable = true, remap = false)
	private void renderBatched(BlockState p_234356_, BlockPos p_234357_, BlockAndTintGetter p_234358_, PoseStack p_234359_, VertexConsumer p_234360_, boolean p_234361_, RandomSource p_234362_, net.minecraftforge.client.model.data.ModelData modelData, net.minecraft.client.renderer.RenderType renderType, CallbackInfo ci) 
	{
		if(SolomonUtil.isBlockUpsideDown(p_234357_, SolomonClientUtil.MC.level))
		{
			ci.cancel();
			try 
			{
				RenderShape rendershape = p_234356_.getRenderShape();
				if(rendershape == RenderShape.MODEL) 
				{
					GravityBakedModelWrapper wrapper = new GravityBakedModelWrapper(this.getBlockModel(p_234356_));
					this.modelRenderer.tesselateBlock(p_234358_, wrapper, p_234356_, p_234357_, p_234359_, p_234360_, p_234361_, p_234362_, p_234356_.getSeed(p_234357_), OverlayTexture.NO_OVERLAY, modelData, renderType);
				}
			} 
			catch (Throwable throwable) 
			{
				CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating block in world");
				CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
				CrashReportCategory.populateBlockDetails(crashreportcategory, p_234358_, p_234357_, p_234356_);
				throw new ReportedException(crashreport);
			}
		}
	}
	
	@Shadow
	public BakedModel getBlockModel(BlockState p_110911_) 
	{
		throw new IllegalStateException();
	}
}
