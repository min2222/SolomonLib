package com.min01.solomonlib.gravity;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import com.min01.gravityapi.util.RotationUtil;
import com.min01.solomonlib.util.SolomonClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;

public class GravityBakedModelWrapper extends BakedModelWrapper<BakedModel>
{
	public GravityBakedModelWrapper(BakedModel originalModel)
	{
		super(originalModel);
	}
	
	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) 
	{
	    List<BakedQuad> originalQuads = super.getQuads(state, side, rand, extraData, renderType);

	    PoseStack poseStack = new PoseStack();
	    poseStack.translate(0.5F, 0.5F, 0.5F);
	    
	    Quaternionf quat = new Quaternionf(RotationUtil.getWorldRotationQuaternion(Direction.UP));
	    poseStack.mulPose(quat);

	    poseStack.scale(-1.0F, 1.0F, 1.0F); 

	    poseStack.translate(-0.5F, -0.5F, -0.5F);

	    List<BakedQuad> transformedQuads = new ArrayList<>(originalQuads.size());
	    
	    for(BakedQuad quad : originalQuads)
	    {
	        transformedQuads.add(SolomonClientUtil.transform(quad, poseStack, true));
	    }
	    
	    return transformedQuads;
	}
}
