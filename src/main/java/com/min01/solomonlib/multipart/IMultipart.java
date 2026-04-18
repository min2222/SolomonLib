package com.min01.solomonlib.multipart;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.phys.AABB;

public interface IMultipart
{
    EntityBounds getBounds();

    CompoundOrientedBox getCompoundBoundingBox(AABB bounds);

	default @Nullable String canonicalMultipartPartName(@Nullable String hitPartKey)
	{
		if(hitPartKey == null)
		{
			return null;
		}
		String mapped = this.getPartBuilder().cubeToModelPart.get(hitPartKey);
		return mapped != null ? mapped : hitPartKey;
	}
	
	default List<String> getCollidePart()
	{
		return List.of();
	}
	
	default List<String> getIgnorePart()
	{
		return List.of();
	}
	
	default boolean skipInvisiblePart()
	{
		return true;
	}

	EntityPartBuilder<?> getPartBuilder();
}
