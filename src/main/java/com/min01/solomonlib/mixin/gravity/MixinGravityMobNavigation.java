package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.solomonlib.gravity.GravityPathNavigation;
import com.min01.solomonlib.gravity.GravityZoneManager;
import com.min01.solomonlib.spider.IClimberEntity;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

@Mixin(Mob.class)
public abstract class MixinGravityMobNavigation
{
	@Shadow 
	protected PathNavigation navigation;

	@Unique 
	private Direction solomonlib$lastGravDir = null;
	
	@Unique 
	private PathNavigation solomonlib$originalNavigation = null;

	@Inject(method = "aiStep", at = @At("HEAD"))
	private void solomonlib$onAiStep(CallbackInfo ci)
	{
		Mob self = (Mob) (Object) this;
		if(self instanceof IClimberEntity)
		{
			return;
		}
		
		if(self.level.isClientSide())
		{
			return;
		}

		Direction gravDir = GravityZoneManager.getDirection(self.level, self.blockPosition());

		if(gravDir == this.solomonlib$lastGravDir)
		{
			return;
		}
		this.solomonlib$lastGravDir = gravDir;

		if(gravDir != Direction.DOWN)
		{
			if(!(this.navigation instanceof GravityPathNavigation))
			{
				this.navigation.stop();
				this.solomonlib$originalNavigation = this.navigation;
				this.navigation = new GravityPathNavigation(self, self.level);
			}
		}
		else
		{
			if(this.navigation instanceof GravityPathNavigation)
			{
				this.navigation.stop();
				this.navigation = this.solomonlib$originalNavigation != null ? this.solomonlib$originalNavigation : this.createNavigation(self.level);
				this.solomonlib$originalNavigation = null;
			}
		}
	}
	
	@Shadow
	protected PathNavigation createNavigation(Level pLevel) 
	{
		throw new IllegalStateException();
	}
}
