package com.min01.solomonlib.mixin.multipart;

import org.spongepowered.asm.mixin.Mixin;

import com.min01.solomonlib.multipart.OBB;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;

@Mixin(LevelReader.class)
public interface MixinLevelReader extends CollisionGetter
{
	//fix a issue that unable to move on obb while crouching;
	@Override
	default boolean noCollision(Entity pEntity, AABB pCollisionBox) 
	{
		if(pEntity != null && !OBB.getOBBEntityCollisions(pEntity.level, pCollisionBox).isEmpty())
		{
			return false;
		}
		return CollisionGetter.super.noCollision(pEntity, pCollisionBox);
	}
}
