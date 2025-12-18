package com.min01.solomonlib.gravity;

import com.min01.solomonlib.config.SolomonConfig;

import net.minecraft.nbt.CompoundTag;

//currently ignores rotateView param;
public record RotationParameters(boolean rotateVelocity, boolean rotateView, int rotationTimeMS) 
{
    public static RotationParameters defaultParam = new RotationParameters(true, true, 500);
    
    public static void updateDefault()
    {
        defaultParam = new RotationParameters(!SolomonConfig.worldVelocity.get(), true, SolomonConfig.rotationTime.get());
    }
    
    public static RotationParameters getDefault() 
    {
        return defaultParam;
    }
    
    public RotationParameters withRotationTimeMs(int rotationTimeMS) 
    {
        return new RotationParameters(this.rotateVelocity, this.rotateView, rotationTimeMS);
    }
    
    public CompoundTag toTag() 
    {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("RotateVelocity", this.rotateVelocity);
        tag.putBoolean("RotateView", this.rotateView);
        tag.putInt("RotationTimeMS", this.rotationTimeMS);
        return tag;
    }
    
    public static RotationParameters fromTag(CompoundTag tag) 
    {
        return new RotationParameters(tag.getBoolean("RotateVelocity"), tag.getBoolean("RotateView"), tag.getInt("RotationTimeMS"));
    }
}
