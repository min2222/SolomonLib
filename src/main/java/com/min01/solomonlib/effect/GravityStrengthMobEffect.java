package com.min01.solomonlib.effect;

import com.min01.solomonlib.gravity.GravityCapabilityImpl;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class GravityStrengthMobEffect extends MobEffect 
{
    public final double base;
    public final int signum;
    
    public GravityStrengthMobEffect(int color, double base, int signum) 
    {
        super(MobEffectCategory.NEUTRAL, color);
        this.base = base;
        this.signum = signum;
    }
    
    public double getGravityStrengthMultiplier(int level)
    {
        return Math.pow(this.base, (double) level) * this.signum;
    }
    
    public void apply(LivingEntity entity, GravityCapabilityImpl component)
    {
        MobEffectInstance effectInstance = entity.getEffect(this);
        
        if(effectInstance == null)
        {
            return;
        }
        
        int level = effectInstance.getAmplifier() + 1;
    
        component.applyGravityStrengthEffect(this.getGravityStrengthMultiplier(level));
    }
}
