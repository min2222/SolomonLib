package com.min01.solomonlib.spider;

import net.minecraft.network.syncher.EntityDataAccessor;

public interface ILivingEntityDataManagerHook 
{
	public void onNotifyDataManagerChange(EntityDataAccessor<?> key);
}
