package com.min01.solomonlib.gravity;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

public class GravityEffectEvent extends EntityEvent
{
	public GravityEffectEvent(Entity entity)
	{
		super(entity);
	}
}
