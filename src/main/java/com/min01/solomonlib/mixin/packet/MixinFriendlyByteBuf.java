package com.min01.solomonlib.mixin.packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.network.FriendlyByteBuf;

@Mixin(value = FriendlyByteBuf.class, priority = -30000)
public class MixinFriendlyByteBuf
{
    @ModifyExpressionValue(method = "readNbt()Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "CONSTANT", args = "longValue=2097152"))
    private long readNbt(long constant) 
    {
        return 2_147_483_647L;
    }
    
    @ModifyExpressionValue(method = "readUtf()Ljava/lang/String;", at = @At(value = "CONSTANT", args = "intValue=32767"))
    private int readUtf(int value)
    {
        return 327670000;
    }

    @ModifyExpressionValue(method = "writeUtf(Ljava/lang/String;)Lnet/minecraft/network/FriendlyByteBuf;", at = @At(value = "CONSTANT", args = "intValue=32767"))
    private int writeUtf(int value)
    {
        return 327670000;
    }

    @ModifyExpressionValue(method = "readResourceLocation", at = @At(value = "CONSTANT", args = "intValue=32767"))
    private int readResourceLocation(int value) 
    {
        return 327670000;
    }
}
