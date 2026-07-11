package com.min01.solomonlib.mixin.multipart;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.solomonlib.multipart.COBB;
import com.min01.solomonlib.multipart.OBB;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher
{
    @Inject(method = "renderHitbox", at = @At("TAIL"))
    private static void renderHitbox(PoseStack pPoseStack, VertexConsumer pBuffer, Entity pEntity, float pPartialTicks, CallbackInfo ci) 
    {
    	//visually draw obb hitbox
        AABB aabb = pEntity.getBoundingBox();
        if(aabb instanceof COBB cobb)
        {
        	pPoseStack.pushPose();
        	pPoseStack.translate(-pEntity.getX(), -pEntity.getY(), -pEntity.getZ());
            for(OBB obb : cobb) 
            {
            	if(!obb.enabled)
            	{
            		continue;
            	}
            	pPoseStack.pushPose();
                Vec3 center = obb.center;
                pPoseStack.translate(center.x, center.y, center.z);
                pPoseStack.mulPose(new Quaternionf(obb.rotation));
                LevelRenderer.renderLineBox(pPoseStack, pBuffer, obb.getExtents(), 0.0F, 0.0F, 1.0F, 1.0F);
                pPoseStack.popPose();
            }
            pPoseStack.popPose();
        }
    }
}
