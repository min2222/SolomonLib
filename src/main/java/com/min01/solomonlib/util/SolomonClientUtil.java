package com.min01.solomonlib.util;

import java.util.Arrays;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;

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
}
