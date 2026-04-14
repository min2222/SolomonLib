package com.min01.solomonlib.network;

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

public class GravityZoneSyncPacket
{
    private final ResourceKey<Level> dimension;
    private final ChunkPos chunkPos;
    private final GravityZone zone;

    public GravityZoneSyncPacket(ResourceKey<Level> dimension, ChunkPos chunkPos, GravityZone zone)
    {
        this.dimension = dimension;
        this.chunkPos  = chunkPos;
        this.zone = zone;
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(this.dimension.location());
        buf.writeLong(this.chunkPos.toLong());
        buf.writeNbt(this.zone.toNbt());
    }

    public static GravityZoneSyncPacket read(FriendlyByteBuf buf)
    {
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        ChunkPos chunkPos = new ChunkPos(buf.readLong());
        GravityZone zone = GravityZone.fromNbt(buf.readNbt());
        return new GravityZoneSyncPacket(dimension, chunkPos, zone);
    }

    public static boolean handle(GravityZoneSyncPacket msg, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            GravityZoneManager.handleSyncPacket(msg.dimension, msg.chunkPos, msg.zone);
            Minecraft mc = SolomonClientUtil.MC;
            if(mc.level != null && mc.levelRenderer != null)
            {
                mc.levelRenderer.setBlocksDirty(msg.chunkPos.getMinBlockX(), mc.level.getMinBuildHeight(), msg.chunkPos.getMinBlockZ(), msg.chunkPos.getMaxBlockX(), mc.level.getMaxBuildHeight(), msg.chunkPos.getMaxBlockZ());
            }
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}
