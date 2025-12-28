package com.min01.solomonlib.mixin.packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;

@Mixin(value = ServerboundCustomPayloadPacket.class, priority = -30000)
public class MixinServerboundCustomPayloadPacket 
{
    @ModifyConstant(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", constant = @Constant(intValue = 32767))
    private int init(int value) 
    {
        return Integer.MAX_VALUE;
    }
}
