package com.min01.solomonlib.mixin.lights;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.solomonlib.lights.IDynamicLight;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

@Mixin(value = ClientLevel.class, priority = -10000)
public abstract class MixinClientLevel
{
	@Shadow
	protected abstract LevelEntityGetter<Entity> getEntities();

	@Inject(method = "removeEntity(ILnet/minecraft/world/entity/Entity$RemovalReason;)V", at = @At("HEAD"))
	private void removeEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) 
	{
		var entity = this.getEntities().get(entityId);
		if(entity != null) 
		{
			var dls = (IDynamicLight) entity;
			dls.setBTADynamicLightEnabled(false);
		}
	}
}
