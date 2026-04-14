package com.min01.solomonlib.mixin.packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

@Mixin(value = ClientboundCustomPayloadPacket.class, priority = -30000)
public class MixinClientboundCustomPayloadPacket
{
    @ModifyExpressionValue(method = "<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "CONSTANT", args = "intValue=1048576"))
    private static int init(int value) 
    {
        return Integer.MAX_VALUE;
    }

    @ModifyExpressionValue(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "CONSTANT", args = "intValue=1048576"))
    private static int init2(int value)
    {
        return Integer.MAX_VALUE;
    }
}
