package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

@Mixin(Entity.class)
public interface EntityAccessor 
{
    @Invoker("makeBoundingBox")
    AABB gc_makeBoundingBox();
}
