package com.min01.solomonlib.mixin.spider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.solomonlib.util.SolomonClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T>
{
    protected MixinLivingEntityRenderer(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void renderPre(T livingEntity, float f, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) 
    {
        SolomonClientUtil.onPreRenderLiving(livingEntity, partialTicks, poseStack);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void renderPost(T livingEntity, float f, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) 
    {
    	SolomonClientUtil.onPostRenderLiving(livingEntity, partialTicks, poseStack, multiBufferSource);
    }
}