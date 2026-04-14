package com.min01.solomonlib.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.min01.solomonlib.gravity.GravityZone;
import com.min01.solomonlib.gravity.GravityZoneManager;
import com.min01.solomonlib.util.SolomonClientUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class GravityZoneBulkSyncPacket
{
    private final ResourceKey<Level> dimension;
    private final Map<Long, GravityZone> zones;

    public GravityZoneBulkSyncPacket(ResourceKey<Level> dimension, Map<Long, GravityZone> zones)
    {
        this.dimension = dimension;
        this.zones = zones;
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(this.dimension.location());
        buf.writeInt(this.zones.size());
        for(Map.Entry<Long, GravityZone> entry : this.zones.entrySet())
        {
            buf.writeLong(entry.getKey());
            buf.writeNbt(entry.getValue().toNbt());
        }
    }

    public static GravityZoneBulkSyncPacket read(FriendlyByteBuf buf)
    {
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        int count = buf.readInt();
        Map<Long, GravityZone> zones = new HashMap<>(count);
        for(int i = 0; i < count; i++)
        {
            long key = buf.readLong();
            GravityZone zone = GravityZone.fromNbt(buf.readNbt());
            if(!zone.isDefault())
            {
                zones.put(key, zone);
            }
        }
        return new GravityZoneBulkSyncPacket(dimension, zones);
    }

    public static boolean handle(GravityZoneBulkSyncPacket msg, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            GravityZoneManager.handleBulkSyncPacket(msg.dimension, msg.zones);
            Minecraft mc = SolomonClientUtil.MC;
            if(mc.level != null && mc.levelRenderer != null)
            {
                for(long key : msg.zones.keySet())
                {
                    ChunkPos cp = new ChunkPos(key);
                    mc.levelRenderer.setBlocksDirty(cp.getMinBlockX(), mc.level.getMinBuildHeight(), cp.getMinBlockZ(), cp.getMaxBlockX(), mc.level.getMaxBuildHeight(), cp.getMaxBlockZ());
                }
            }
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}
