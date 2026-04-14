package com.min01.solomonlib.mixin.packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;

@Mixin(value = ServerboundCustomPayloadPacket.class, priority = -30000)
public class MixinServerboundCustomPayloadPacket 
{
    @ModifyExpressionValue(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "CONSTANT", args = "intValue=32767"))
    private int init(int value) 
    {
        return Integer.MAX_VALUE;
    }
}
