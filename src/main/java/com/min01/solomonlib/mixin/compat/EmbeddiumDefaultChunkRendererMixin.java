package com.min01.solomonlib.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.min01.solomonlib.gravity.GravityZoneManager;
import com.min01.solomonlib.util.SolomonClientUtil;

import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.minecraft.core.Direction;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public class EmbeddiumDefaultChunkRendererMixin
{
	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/DefaultChunkRenderer;fillCommandBuffer(Lme/jellysquid/mods/sodium/client/gl/device/MultiDrawBatch;Lme/jellysquid/mods/sodium/client/render/chunk/region/RenderRegion;Lme/jellysquid/mods/sodium/client/render/chunk/data/SectionRenderDataStorage;Lme/jellysquid/mods/sodium/client/render/chunk/lists/ChunkRenderList;Lme/jellysquid/mods/sodium/client/render/viewport/CameraTransform;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;Z)V"), index = 6)
	private boolean solomon$disableSliceCullingInGravityZone(boolean useBlockFaceCulling)
	{
		if(SolomonClientUtil.MC.level == null || SolomonClientUtil.MC.player == null)
		{
			return useBlockFaceCulling;
		}
		if(GravityZoneManager.getDirection(SolomonClientUtil.MC.level, SolomonClientUtil.MC.player.blockPosition()) != Direction.DOWN)
		{
			return false;
		}
		return useBlockFaceCulling;
	}
}
