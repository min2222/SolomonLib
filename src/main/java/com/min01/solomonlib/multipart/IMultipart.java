package com.min01.solomonlib.multipart;

import java.util.List;

import net.minecraft.world.phys.AABB;

public interface IMultipart
{
    EntityBounds getBounds();

    CompoundOrientedBox getCompoundBoundingBox(AABB bounds);
	
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
