package com.min01.solomonlib.util;

import java.util.Arrays;
import java.util.List;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.min01.solomonlib.spider.IClimberEntity;
import com.min01.solomonlib.spider.Orientation;
import com.min01.solomonlib.spider.PathingTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SolomonClientUtil
{
	public static final Minecraft MC = Minecraft.getInstance();
	
	public static final int VERTEX_SIZE_INTS = DefaultVertexFormat.BLOCK.getIntegerSize();
	
    public static BakedQuad transform(BakedQuad quad, PoseStack poseStack, boolean flipWindingOrder) 
    {
        int[] originalVertices = quad.getVertices();
        int[] newVertices = Arrays.copyOf(originalVertices, originalVertices.length);

        Matrix4f poseMatrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        for(int i = 0; i < 4; i++) 
        {
            int offset = i * VERTEX_SIZE_INTS;

            float x = Float.intBitsToFloat(newVertices[offset + 0]);
            float y = Float.intBitsToFloat(newVertices[offset + 1]);
            float z = Float.intBitsToFloat(newVertices[offset + 2]);

            Vector4f pos = new Vector4f(x, y, z, 1.0f);
            pos.mul(poseMatrix);
            
            newVertices[offset + 0] = Float.floatToRawIntBits(pos.x());
            newVertices[offset + 1] = Float.floatToRawIntBits(pos.y());
            newVertices[offset + 2] = Float.floatToRawIntBits(pos.z());

            int normalData = newVertices[offset + 6];
            
            byte nxByte = (byte)(normalData >> 0);
            byte nyByte = (byte)(normalData >> 8);
            byte nzByte = (byte)(normalData >> 16);
            
            Vector3f normal = new Vector3f(nxByte / 127.0f, nyByte / 127.0f, nzByte / 127.0f);
            normal.normalize();
            normal.mul(normalMatrix);
            normal.normalize();

            int newNormalData = ((byte)(normal.x() * 127.0f) & 0xFF) | (((byte)(normal.y() * 127.0f) & 0xFF) << 8) | (((byte)(normal.z() * 127.0f) & 0xFF) << 16);
            newVertices[offset + 6] = newNormalData;
        }

        if(flipWindingOrder) 
        {
            int[] v1 = Arrays.copyOfRange(newVertices, 1 * VERTEX_SIZE_INTS, 2 * VERTEX_SIZE_INTS);
            int[] v3 = Arrays.copyOfRange(newVertices, 3 * VERTEX_SIZE_INTS, 4 * VERTEX_SIZE_INTS);
            
            System.arraycopy(v3, 0, newVertices, 1 * VERTEX_SIZE_INTS, VERTEX_SIZE_INTS);
            System.arraycopy(v1, 0, newVertices, 3 * VERTEX_SIZE_INTS, VERTEX_SIZE_INTS);
        }

        return new BakedQuad(newVertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
    }
    
	public static void onPreRenderLiving(LivingEntity entity, float partialTicks, PoseStack matrixStack)
	{
		if(entity instanceof IClimberEntity)
		{
			IClimberEntity climber = (IClimberEntity) entity;

			Orientation orientation = climber.getOrientation();
			Orientation renderOrientation = climber.calculateOrientation(partialTicks);
			climber.setRenderOrientation(renderOrientation);

			float verticalOffset = climber.getVerticalOffset(partialTicks);

			float x = climber.getAttachmentOffset(Direction.Axis.X, partialTicks) - (float) renderOrientation.normal.x * verticalOffset;
			float y = climber.getAttachmentOffset(Direction.Axis.Y, partialTicks) - (float) renderOrientation.normal.y * verticalOffset;
			float z = climber.getAttachmentOffset(Direction.Axis.Z, partialTicks) - (float) renderOrientation.normal.z * verticalOffset;

			matrixStack.translate(x, y, z);

			matrixStack.mulPose(Axis.YP.rotationDegrees(renderOrientation.yaw));
			matrixStack.mulPose(Axis.XP.rotationDegrees(renderOrientation.pitch));
			matrixStack.mulPose(Axis.YP.rotationDegrees((float) Math.signum(0.5F - orientation.componentY - orientation.componentZ - orientation.componentX) * renderOrientation.yaw));
		}
	}

	public static void onPostRenderLiving(LivingEntity entity, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn)
	{
		if(entity instanceof IClimberEntity) 
		{
			IClimberEntity climber = (IClimberEntity) entity;
			Orientation orientation = climber.getOrientation();
			Orientation renderOrientation = climber.getRenderOrientation();

			if(renderOrientation != null)
			{
				float verticalOffset = climber.getVerticalOffset(partialTicks);

				float x = climber.getAttachmentOffset(Direction.Axis.X, partialTicks) - (float) renderOrientation.normal.x * verticalOffset;
				float y = climber.getAttachmentOffset(Direction.Axis.Y, partialTicks) - (float) renderOrientation.normal.y * verticalOffset;
				float z = climber.getAttachmentOffset(Direction.Axis.Z, partialTicks) - (float) renderOrientation.normal.z * verticalOffset;

				matrixStack.mulPose(Axis.YP.rotationDegrees(-(float) Math.signum(0.5F - orientation.componentY - orientation.componentZ - orientation.componentX) * renderOrientation.yaw));
				matrixStack.mulPose(Axis.XP.rotationDegrees(-renderOrientation.pitch));
				matrixStack.mulPose(Axis.YP.rotationDegrees(-renderOrientation.yaw));

				if(SolomonClientUtil.MC.getEntityRenderDispatcher().shouldRenderHitBoxes())
				{
					LevelRenderer.renderLineBox(matrixStack, bufferIn.getBuffer(RenderType.LINES), new AABB(0, 0, 0, 0, 0, 0).inflate(0.2F), 1.0F, 1.0F, 1.0F, 1.0F);

					double rx = entity.xo + (entity.getX() - entity.xo) * partialTicks;
					double ry = entity.yo + (entity.getY() - entity.yo) * partialTicks;
					double rz = entity.zo + (entity.getZ() - entity.zo) * partialTicks;

					Vec3 movementTarget = climber.getTrackedMovementTarget();

					if(movementTarget != null) 
					{
						LevelRenderer.renderLineBox(matrixStack, bufferIn.getBuffer(RenderType.LINES), new AABB(movementTarget.x() - 0.25F, movementTarget.y() - 0.25F, movementTarget.z() - 0.25F, movementTarget.x() + 0.25F, movementTarget.y() + 0.25F, movementTarget.z() + 0.25F).move(-rx - x, -ry - y, -rz - z), 0.0F, 1.0F, 1.0F, 1.0F);
					}

					List<PathingTarget> pathingTargets = climber.getTrackedPathingTargets();

					if(pathingTargets != null)
					{
						int i = 0;

						for(PathingTarget pathingTarget : pathingTargets) 
						{
							BlockPos pos = pathingTarget.pos;

                            LevelRenderer.renderLineBox(matrixStack, bufferIn.getBuffer(RenderType.LINES), new AABB(pos).move(-rx - x, -ry - y, -rz - z), 1.0F, i / (float) (pathingTargets.size() - 1), 0.0F, 0.15F);
							
							matrixStack.pushPose();
							matrixStack.translate(pos.getX() + 0.5D - rx - x, pos.getY() + 0.5D - ry - y, pos.getZ() + 0.5D - rz - z);

							matrixStack.mulPose(pathingTarget.side.getOpposite().getRotation());

							LevelRenderer.renderLineBox(matrixStack, bufferIn.getBuffer(RenderType.LINES), new AABB(-0.501D, -0.501D, -0.501D, 0.501D, -0.45D, 0.501D), 1.0F, i / (float) (pathingTargets.size() - 1), 0.0F, 1.0F);

							Matrix4f matrix4f = matrixStack.last().pose();
							VertexConsumer builder = bufferIn.getBuffer(RenderType.LINES);

							builder.vertex(matrix4f, -0.501F, -0.45F, -0.501F).color(1.0F, i / (float) (pathingTargets.size() - 1), 0.0F, 1.0F).endVertex();
							builder.vertex(matrix4f, 0.501F, -0.45F, 0.501F).color(1.0F, i / (float) (pathingTargets.size() - 1), 0.0F, 1.0F).endVertex();
							builder.vertex(matrix4f, -0.501F, -0.45F, 0.501F).color(1.0F, i / (float) (pathingTargets.size() - 1), 0.0F, 1.0F).endVertex();
							builder.vertex(matrix4f, 0.501F, -0.45F, -0.501F).color(1.0F, i / (float) (pathingTargets.size() - 1), 0.0F, 1.0F).endVertex();

							matrixStack.popPose();

							i++;
						}
					}

					Matrix4f matrix4f = matrixStack.last().pose();
					VertexConsumer builder = bufferIn.getBuffer(RenderType.LINES);

					builder.vertex(matrix4f, 0, 0, 0).color(0, 1, 1, 1).normal(0 ,0, 0).endVertex();
					builder.vertex(matrix4f, (float) orientation.normal.x * 2, (float) orientation.normal.y * 2, (float) orientation.normal.z * 2).color(1.0F, 0.0F, 1.0F, 1.0F).normal(0, 0, 0).endVertex();

					LevelRenderer.renderLineBox(matrixStack, bufferIn.getBuffer(RenderType.LINES), new AABB(0, 0, 0, 0, 0, 0).move((float) orientation.normal.x * 2, (float) orientation.normal.y * 2, (float) orientation.normal.z * 2).inflate(0.025F), 1.0F, 0.0F, 1.0F, 1.0F);

					matrixStack.pushPose();

					matrixStack.translate(-x, -y, -z);

					matrix4f = matrixStack.last().pose();

					builder.vertex(matrix4f, 0, entity.getBbHeight() * 0.5F, 0).color(0, 1, 1, 1).normal(0 ,0, 0).endVertex();
					builder.vertex(matrix4f, (float) orientation.localX.x, entity.getBbHeight() * 0.5F + (float) orientation.localX.y, (float) orientation.localX.z).color(1.0F, 0.0F, 0.0F, 1.0F).normal(0, 0, 0).endVertex();

					LevelRenderer.renderLineBox(matrixStack, bufferIn.getBuffer(RenderType.LINES), new AABB(0, 0, 0, 0, 0, 0).move((float) orientation.localX.x, entity.getBbHeight() * 0.5F + (float) orientation.localX.y, (float) orientation.localX.z).inflate(0.025F), 1.0F, 0.0F, 0.0F, 1.0F);

					builder.vertex(matrix4f, 0, entity.getBbHeight() * 0.5F, 0).color(0, 1, 1, 1).normal(0 ,0, 0).endVertex();
					builder.vertex(matrix4f, (float) orientation.localY.x, entity.getBbHeight() * 0.5F + (float) orientation.localY.y, (float) orientation.localY.z).color(0.0F, 1.0F, 0.0F, 1.0F).normal(0, 0, 0).endVertex();

					LevelRenderer.renderLineBox(matrixStack, bufferIn.getBuffer(RenderType.LINES), new AABB(0, 0, 0, 0, 0, 0).move((float) orientation.localY.x, entity.getBbHeight() * 0.5F + (float) orientation.localY.y, (float) orientation.localY.z).inflate(0.025F), 0.0F, 1.0F, 0.0F, 1.0F);

					builder.vertex(matrix4f, 0, entity.getBbHeight() * 0.5F, 0).color(0, 1, 1, 1).normal(0 ,0, 0).endVertex();
					builder.vertex(matrix4f, (float) orientation.localZ.x, entity.getBbHeight() * 0.5F + (float) orientation.localZ.y, (float) orientation.localZ.z).color(0.0F, 0.0F, 1.0F, 1.0F).normal(0, 0, 0).endVertex();

					LevelRenderer.renderLineBox(matrixStack, bufferIn.getBuffer(RenderType.LINES), new AABB(0, 0, 0, 0, 0, 0).move((float) orientation.localZ.x, entity.getBbHeight() * 0.5F + (float) orientation.localZ.y, (float) orientation.localZ.z).inflate(0.025F), 0.0F, 0.0F, 1.0F, 1.0F);

					matrixStack.popPose();
				}

				matrixStack.translate(-x, -y, -z);
			}
		}
	}
}
