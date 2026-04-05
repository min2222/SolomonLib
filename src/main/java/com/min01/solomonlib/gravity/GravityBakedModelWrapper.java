package com.min01.solomonlib.gravity;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    private final Direction gravDir;

    public GravityBakedModelWrapper(BakedModel originalModel, Direction gravDir)
    {
        super(originalModel);
        this.gravDir = gravDir;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType)
    {
        Direction playerSide = (side != null) ? RotationUtil.dirWorldToPlayer(side, this.gravDir) : null;
        List<BakedQuad> originalQuads = super.getQuads(state, playerSide, rand, extraData, renderType);

        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(new Quaternionf(RotationUtil.getCameraRotationQuaternion(this.gravDir)));
        if(this.gravDir == Direction.UP)
        {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        boolean flipWinding = (this.gravDir == Direction.UP);

        List<BakedQuad> result = new ArrayList<>(originalQuads.size());
        for(BakedQuad quad : originalQuads)
        {
            BakedQuad transformed = SolomonClientUtil.transform(quad, poseStack, flipWinding);
            Direction rotatedFace = this.transformFaceDir(quad.getDirection());
            result.add(new BakedQuad(transformed.getVertices(), transformed.getTintIndex(), rotatedFace, transformed.getSprite(), transformed.isShade()));
        }
        return result;
    }

    private Direction transformFaceDir(Direction dir)
    {
        Vector3f normal = new Vector3f(dir.getStepX(), dir.getStepY(), dir.getStepZ());
        new Quaternionf(RotationUtil.getCameraRotationQuaternion(this.gravDir)).transform(normal);
        if(this.gravDir == Direction.UP)
        {
            normal.mul(-1.0F, 1.0F, 1.0F);
        }
        return Direction.getNearest(normal.x(), normal.y(), normal.z());
    }
}
