package com.min01.solomonlib.mixin.gravity;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.GravityZoneManager;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(value = ServerLevel.class, priority = -15000)
public abstract class MixinServerLevel extends Level
{
    protected MixinServerLevel(WritableLevelData pLevelData, ResourceKey<Level> pDimension, RegistryAccess pRegistryAccess, Holder<DimensionType> pDimensionTypeRegistration, Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed, int pMaxChainedNeighborUpdates) 
    {
		super(pLevelData, pDimension, pRegistryAccess, pDimensionTypeRegistration, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed, pMaxChainedNeighborUpdates);
	}

	@Inject(at = @At("HEAD"), method = "addFreshEntity")
    private void onAddFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> ci)
    {
        if(GravityAPI.getBaseGravityDirection(entity) != Direction.DOWN)
        {
            return;
        }
        Direction zoneDirection = GravityZoneManager.getDirection((ServerLevel) (Object) this, entity.blockPosition());
        if(zoneDirection != Direction.DOWN)
        {
            GravityAPI.setBaseGravityDirection(entity, zoneDirection);
        }
    }
}
