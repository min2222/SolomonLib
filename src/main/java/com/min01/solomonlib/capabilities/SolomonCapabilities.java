package com.min01.solomonlib.capabilities;

import javax.annotation.Nonnull;

import com.min01.solomonlib.gravity.GravityCapabilityImpl;
import com.min01.solomonlib.gravity.IGravityCapability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class SolomonCapabilities
{
	public static final Capability<IGravityCapability> GRAVITY = CapabilityManager.get(new CapabilityToken<>() {});
	
	public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e)
	{
		e.addCapability(IGravityCapability.ID, new ICapabilitySerializable<CompoundTag>() 
		{
			LazyOptional<IGravityCapability> inst = LazyOptional.of(() -> 
			{
				GravityCapabilityImpl i = new GravityCapabilityImpl();
				i.setEntity(e.getObject());
				return i;
			});

			@Nonnull
			@Override
			public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) 
			{
				return GRAVITY.orEmpty(capability, this.inst.cast());
			}

			@Override
			public CompoundTag serializeNBT() 
			{
				return this.inst.orElseThrow(NullPointerException::new).serializeNBT();
			}

			@Override
			public void deserializeNBT(CompoundTag nbt)
			{
				this.inst.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
			}
		});
	}
}
