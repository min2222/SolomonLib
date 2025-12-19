package com.min01.solomonlib.mixin.gravity;

import com.min01.solomonlib.gravity.GravityAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.solomonlib.gravity.GravityPathNavigation;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

@Mixin(Mob.class)
public abstract class MobMixin 
{
	@Shadow
	protected PathNavigation navigation;
    
    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    private void tick(CallbackInfo ci)
    {
    	Mob mob = Mob.class.cast(this);
        Direction gravityDirection = GravityAPI.getGravityDirection(mob);
    	if(gravityDirection != Direction.DOWN)
    	{
			if(!(this.navigation instanceof GravityPathNavigation))
			{
				if(this.navigation instanceof GroundPathNavigation)
				{
	    			this.navigation = new GravityPathNavigation(mob, mob.level);
				}
			}
    	}
    	else if(this.navigation instanceof GravityPathNavigation)
    	{
    		this.navigation = this.createNavigation(mob.level);
    	}
    }
    
    @Shadow
    protected PathNavigation createNavigation(Level p_21480_)
    {
    	throw new IllegalStateException();
    }
    
	@WrapOperation(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getYRot()F"))
	private float wrapOperation_tryAttack_getYaw_0(Mob attacker, Operation<Float> original, Entity target)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(target);
		if(gravityDirection == Direction.DOWN)
		{
			return original.call(attacker);
		}

		return RotationUtil.rotWorldToPlayer(original.call(attacker), attacker.getXRot(), gravityDirection).x;
	}

	@Redirect(method = "Lnet/minecraft/world/entity/Mob;lookAt(Lnet/minecraft/world/entity/Entity;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getEyeY()D", ordinal = 0))
	private double redirect_lookAtEntity_getEyeY_0(LivingEntity livingEntity) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(livingEntity);
		if(gravityDirection == Direction.DOWN)
		{
			return livingEntity.getEyeY();
		}

		return livingEntity.getEyePosition().y;
	}

	@Redirect(method = "Lnet/minecraft/world/entity/Mob;lookAt(Lnet/minecraft/world/entity/Entity;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getX()D", ordinal = 0))
	private double redirect_lookAtEntity_getX_0(Entity entity) 
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(entity);
		if(gravityDirection == Direction.DOWN) 
		{
			return entity.getX();
		}

		return entity.getEyePosition().x;
	}

	@Redirect(method = "Lnet/minecraft/world/entity/Mob;lookAt(Lnet/minecraft/world/entity/Entity;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getZ()D", ordinal = 0))
	private double redirect_lookAtEntity_getZ_0(Entity entity)
	{
		Direction gravityDirection = GravityAPI.getGravityDirection(entity);
		if(gravityDirection == Direction.DOWN) 
		{
			return entity.getZ();
		}

		return entity.getEyePosition().z;
	}
}
