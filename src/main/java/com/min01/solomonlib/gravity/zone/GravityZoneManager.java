package com.min01.solomonlib.gravity.zone;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.min01.solomonlib.network.GravityZoneBulkSyncPacket;
import com.min01.solomonlib.network.GravityZoneSyncPacket;
import com.min01.solomonlib.network.SolomonNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class GravityZoneManager
{
    private static final Map<ResourceKey<Level>, Map<Long, GravityZone>> SERVER_ZONES = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<Long, GravityZone>> CLIENT_ZONES = new HashMap<>();

    @NotNull
    public static GravityZone getZone(Level level, BlockPos pos)
    {
        return getZone(level, new ChunkPos(pos));
    }

    @NotNull
    public static GravityZone getZone(Level level, ChunkPos chunkPos)
    {
        Map<Long, GravityZone> map = getMap(level);
        GravityZone zone = map.get(chunkPos.toLong());
        return zone != null ? zone : GravityZone.DEFAULT;
    }

    @NotNull
    public static Direction getDirection(Level level, BlockPos pos)
    {
        return getZone(level, pos).getDirection();
    }

    public static double getStrength(Level level, BlockPos pos)
    {
        return getZone(level, pos).getStrength();
    }

    public static void setZone(ServerLevel level, ChunkPos chunkPos, GravityZone zone)
    {
        Map<Long, GravityZone> map = getServerMap(level);

        if(zone.isDefault())
        {
            map.remove(chunkPos.toLong());
        }
        else
        {
            map.put(chunkPos.toLong(), zone);
        }

        GravityZoneSavedData.get(level).setDirty();
        SolomonNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunk(chunkPos.x, chunkPos.z)), new GravityZoneSyncPacket(level.dimension(), chunkPos, zone));
    }

    public static void setZone(ServerLevel level, BlockPos pos, GravityZone zone)
    {
        setZone(level, new ChunkPos(pos), zone);
    }

    public static void setDirection(ServerLevel level, ChunkPos chunkPos, Direction direction)
    {
        GravityZone current = getZone(level, chunkPos);
        setZone(level, chunkPos, GravityZone.of(direction, current.getStrength()));
    }

    public static void setStrength(ServerLevel level, ChunkPos chunkPos, double strength)
    {
        GravityZone current = getZone(level, chunkPos);
        setZone(level, chunkPos, GravityZone.of(current.getDirection(), strength));
    }

    public static void resetZone(ServerLevel level, ChunkPos chunkPos)
    {
        setZone(level, chunkPos, GravityZone.DEFAULT);
    }

    public static void syncAllToPlayer(ServerLevel level, ServerPlayer player)
    {
        Map<Long, GravityZone> map = getServerMap(level);
        if(map.isEmpty())
        {
            return;
        }
        SolomonNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new GravityZoneBulkSyncPacket(level.dimension(), map));
    }

    public static void syncChunkToPlayer(ServerLevel level, ChunkPos chunkPos, ServerPlayer player)
    {
        GravityZone zone = getZone(level, chunkPos);
        if(zone.isDefault())
        {
            return;
        }
        SolomonNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new GravityZoneSyncPacket(level.dimension(), chunkPos, zone));
    }

    public static void handleSyncPacket(ResourceKey<Level> dimension, ChunkPos chunkPos, GravityZone zone)
    {
        Map<Long, GravityZone> map = CLIENT_ZONES.computeIfAbsent(dimension, k -> new HashMap<>());

        if(zone.isDefault())
        {
            map.remove(chunkPos.toLong());
        }
        else
        {
            map.put(chunkPos.toLong(), zone);
        }
    }

    public static void handleBulkSyncPacket(ResourceKey<Level> dimension, Map<Long, GravityZone> zones)
    {
        CLIENT_ZONES.put(dimension, new HashMap<>(zones));
    }

    public static void onLevelUnload(ResourceKey<Level> dimension, boolean isClientSide)
    {
        if(isClientSide)
        {
            CLIENT_ZONES.remove(dimension);
        }
        else
        {
            SERVER_ZONES.remove(dimension);
        }
    }

    public static void loadFromSavedData(ServerLevel level, Map<Long, GravityZone> savedZones)
    {
        Map<Long, GravityZone> map = getServerMap(level);
        map.clear();
        map.putAll(savedZones);
    }

    public static Map<Long, GravityZone> getZonesForSave(ServerLevel level)
    {
        return getServerMap(level);
    }

    private static Map<Long, GravityZone> getMap(Level level)
    {
        Map<ResourceKey<Level>, Map<Long, GravityZone>> root = level.isClientSide() ? CLIENT_ZONES : SERVER_ZONES;
        return root.computeIfAbsent(level.dimension(), k -> new HashMap<>());
    }

    private static Map<Long, GravityZone> getServerMap(ServerLevel level)
    {
        return SERVER_ZONES.computeIfAbsent(level.dimension(), k -> new HashMap<>());
    }
}
