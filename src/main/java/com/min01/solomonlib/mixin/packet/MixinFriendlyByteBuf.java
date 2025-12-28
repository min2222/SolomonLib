package com.min01.solomonlib.mixin.packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.network.FriendlyByteBuf;

@Mixin(value = FriendlyByteBuf.class, priority = -30000)
public class MixinFriendlyByteBuf
{
    @ModifyConstant(method = "readNbt()Lnet/minecraft/nbt/CompoundTag;", constant = @Constant(longValue = 2097152L))
    private long readNbt(long constant) 
    {
        return 2_147_483_647L;
    }
    
    @ModifyConstant(method = "readUtf()Ljava/lang/String;", constant = @Constant(intValue = 32767))
    private int readUtf(int value)
    {
        return 327670000;
    }

    @ModifyConstant(method = "writeUtf(Ljava/lang/String;)Lnet/minecraft/network/FriendlyByteBuf;", constant = @Constant(intValue = 32767))
    private int writeUtf(int value)
    {
        return 327670000;
    }

    @ModifyConstant(method = "readResourceLocation", constant = @Constant(intValue = 32767))
    private int readResourceLocation(int value) 
    {
        return 327670000;
    }
}
