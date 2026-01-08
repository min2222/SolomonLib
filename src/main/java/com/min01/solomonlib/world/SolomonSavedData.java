package com.min01.solomonlib.world;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class SolomonSavedData extends SavedData
{
	public static final String NAME = "solomon_data";
	
	private final List<BlockPos> blocks = new ArrayList<BlockPos>();
	
    public static SolomonSavedData get(Level level)
    {
        if(level instanceof ServerLevel serverLevel) 
        {
            DimensionDataStorage storage = serverLevel.getDataStorage();
            SolomonSavedData data = storage.computeIfAbsent(t -> load(serverLevel, t), SolomonSavedData::new, NAME);
            return data;
        }
        return null;
    }

    public static SolomonSavedData load(ServerLevel level, CompoundTag nbt) 
    {
    	SolomonSavedData data = new SolomonSavedData();
    	ListTag blocks = nbt.getList("Blocks", 10);
		for(int i = 0; i < blocks.size(); ++i)
		{
			CompoundTag tag = blocks.getCompound(i);
			BlockPos pos = NbtUtils.readBlockPos(tag);
			BlockState state = level.getBlockState(pos);
			if(!state.isAir())
			{
				data.setBlockUpsideDown(pos);
			}
		}
    	return data;
    }
	
	@Override
	public CompoundTag save(CompoundTag nbt)
	{
		ListTag blocks = new ListTag();
		this.blocks.forEach(t -> 
		{
			blocks.add(NbtUtils.writeBlockPos(t));
		});
		nbt.put("Blocks", blocks);
		return nbt;
	}
	
	public void setBlockUpsideDown(BlockPos pos)
	{
		this.blocks.add(pos);
		this.setDirty();
	}
	
	public boolean isBlockUpsideDown(Level level, BlockPos pos)
	{
		return this.blocks.contains(pos);
	}
	
	public List<BlockPos> getUpsideDownBlocks()
	{
		return this.blocks;
	}
}
