package com.min01.solomonlib.mixin.gravity;

import com.min01.solomonlib.gravity.GravityAPI;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.solomonlib.gravity.RotationAnimation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin
{
	@Shadow
	@Final
	private Camera mainCamera;

	@Shadow
	@Final
	private LightTexture lightTexture;

	@Shadow
	@Final
	Minecraft minecraft;

	@Shadow
	private float renderDistance;
	
	@Shadow
	private int tick;
	
	@Shadow
	private boolean renderHand;

    @Inject(method = "renderLevel", at = @At(value = "HEAD"), cancellable = true)
    private void renderLevel(float p_109090_, long p_109091_, PoseStack p_109092_, CallbackInfo ci)
    {
        if(this.mainCamera.getEntity() != null) 
        {
            Entity focusedEntity = this.mainCamera.getEntity();
            Direction gravityDirection = GravityAPI.getGravityDirection(focusedEntity);
            RotationAnimation animation = GravityAPI.getRotationAnimation(focusedEntity);
            // Only override vanilla when a gravity transform/animation is active
            if(animation == null || (gravityDirection == Direction.DOWN && !animation.isInAnimation())) 
            {
                return;
            }
            ci.cancel();
            long timeMs = focusedEntity.level().getGameTime() * 50 + (long) (p_109090_ * 50);
            Quaternionf currentGravityRotation = animation.getCurrentGravityRotation(gravityDirection, timeMs);

			if(animation.isInAnimation()) 
			{
				// make sure that frustum culling updates when running rotation animation
				Minecraft.getInstance().levelRenderer.needsUpdate();
			}

			GameRenderer renderer = GameRenderer.class.cast(this);
			this.lightTexture.updateLightTexture(p_109090_);
			if(this.minecraft.getCameraEntity() == null)
			{
				this.minecraft.setCameraEntity(this.minecraft.player);
			}

			renderer.pick(p_109090_);
			this.minecraft.getProfiler().push("center");
			boolean flag = this.shouldRenderBlockOutline();
			this.minecraft.getProfiler().popPush("camera");
			Camera camera = this.mainCamera;
			this.renderDistance = (float) (this.minecraft.options.getEffectiveRenderDistance() * 16);
            PoseStack posestack = new PoseStack();
            double d0 = this.getFov(camera, p_109090_, true);
            posestack.mulPoseMatrix(renderer.getProjectionMatrix(d0));

			float f = this.minecraft.options.screenEffectScale().get().floatValue();
			float f1 = Mth.lerp(p_109090_, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity) * f * f;
			if(f1 > 0.0F)
			{
				int i = this.minecraft.player.hasEffect(MobEffects.CONFUSION) ? 7 : 20;
				float f2 = 5.0F / (f1 * f1 + 5.0F) - f1 * 0.04F;
				f2 *= f2;
				Axis axis = Axis.of(new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F));
				posestack.mulPose(axis.rotationDegrees(((float) this.tick + p_109090_) * (float) i));
				posestack.scale(1.0F / f2, 1.0F, 1.0F);
				float f3 = -((float) this.tick + p_109090_) * (float) i;
				posestack.mulPose(axis.rotationDegrees(f3));
			}

            Matrix4f matrix4f = posestack.last().pose();
            renderer.resetProjectionMatrix(matrix4f);
			camera.setup(this.minecraft.level, (Entity) (this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()), !this.minecraft.options.getCameraType().isFirstPerson(), this.minecraft.options.getCameraType().isMirrored(), p_109090_);

			net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles cameraSetup = net.minecraftforge.client.ForgeHooksClient.onCameraSetup(renderer, camera, p_109090_);
			camera.setAnglesInternal(cameraSetup.getYaw(), cameraSetup.getPitch());
            p_109092_.mulPose(Axis.ZP.rotationDegrees(cameraSetup.getRoll()));

            p_109092_.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
            p_109092_.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));

            this.bobHurt(p_109092_, p_109090_);
            if(this.minecraft.options.bobView().get())
            {
                this.bobView(p_109092_, p_109090_);
            }

            p_109092_.mulPose(currentGravityRotation);
            Matrix3f matrix3f = (new Matrix3f(p_109092_.last().normal())).invert();
            RenderSystem.setInverseViewRotationMatrix(matrix3f);
			this.minecraft.levelRenderer.prepareCullFrustum(p_109092_, camera.getPosition(), renderer.getProjectionMatrix(Math.max(d0, (double) this.minecraft.options.fov().get().intValue())));
			this.minecraft.levelRenderer.renderLevel(p_109092_, p_109090_, p_109091_, flag, camera, renderer, this.lightTexture, matrix4f);
			this.minecraft.getProfiler().popPush("forge_render_last");
			net.minecraftforge.client.ForgeHooksClient.dispatchRenderStage(net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL, this.minecraft.levelRenderer, posestack, matrix4f, this.minecraft.levelRenderer.getTicks(), camera, this.minecraft.levelRenderer.getFrustum());
			this.minecraft.getProfiler().popPush("hand");
			if(this.renderHand) 
			{
				RenderSystem.clear(256, Minecraft.ON_OSX);
				this.renderItemInHand(p_109092_, camera, p_109090_);
			}

			this.minecraft.getProfiler().pop();
		}
	}
	
	@Shadow
	private void renderItemInHand(PoseStack p_109121_, Camera p_109122_, float p_109123_) 
	{
		   
	}

	@Shadow
	private boolean shouldRenderBlockOutline() 
	{
		throw new IllegalStateException();
	}
	
	@Shadow
	private double getFov(Camera p_109142_, float p_109143_, boolean p_109144_) 
	{
		throw new IllegalStateException();
	}
	
	@Shadow
	private void bobHurt(PoseStack p_109118_, float p_109119_) 
	{
		   
	}
	
	@Shadow
	private void bobView(PoseStack p_109139_, float p_109140_) 
	{
		
	}
}
