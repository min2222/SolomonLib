package com.min01.solomonlib.gravity.zone;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

public class GravityZoneSavedData extends SavedData
{
    private static final String DATA_NAME = "gravity_zones";

    private final Map<Long, GravityZone> zones = new HashMap<>();
    private boolean restored = false;

    public static GravityZoneSavedData get(ServerLevel level)
    {
        GravityZoneSavedData data = level.getDataStorage().computeIfAbsent(GravityZoneSavedData::load, GravityZoneSavedData::new, DATA_NAME);
        data.restoreToManager(level);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        ListTag list = new ListTag();
        for(Map.Entry<Long, GravityZone> entry : this.zones.entrySet())
        {
            if(entry.getValue().isDefault())
            {
            	continue;
            }
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong("Chunk", entry.getKey());
            entryTag.put("Zone", entry.getValue().toNbt());
            list.add(entryTag);
        }
        tag.put("Zones", list);
        return tag;
    }

    private static GravityZoneSavedData load(CompoundTag tag)
    {
        GravityZoneSavedData data = new GravityZoneSavedData();
        ListTag list = tag.getList("Zones", Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); i++)
        {
            CompoundTag entryTag = list.getCompound(i);
            long chunkKey = entryTag.getLong("Chunk");
            GravityZone zone = GravityZone.fromNbt(entryTag.getCompound("Zone"));

            if(!zone.isDefault())
            {
                data.zones.put(chunkKey, zone);
            }
        }
        return data;
    }

    private void restoreToManager(ServerLevel level)
    {
        if(this.restored)
        {
        	return;
        }
        this.restored = true;
        GravityZoneManager.loadFromSavedData(level, this.zones);
    }

    public void putZone(ChunkPos chunkPos, GravityZone zone)
    {
        if(zone.isDefault())
        {
        	this.zones.remove(chunkPos.toLong());
        }
        else
        {
        	this.zones.put(chunkPos.toLong(), zone);
        }
        this.setDirty();
    }

    public Map<Long, GravityZone> getZones()
    {
        return this.zones;
    }
}
