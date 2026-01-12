package com.min01.solomonlib.capabilities;

import com.min01.solomonlib.gravity.GravityCapabilityImpl;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class SolomonCapabilities
{
	public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
    	Entity entity = event.getObject();
		event.addCapability(GravityCapabilityImpl.ID, new GravityCapabilityImpl(entity));
	}
}
