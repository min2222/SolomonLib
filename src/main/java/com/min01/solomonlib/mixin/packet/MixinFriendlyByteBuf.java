package com.min01.solomonlib.mixin.packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.network.FriendlyByteBuf;

@Mixin(value = FriendlyByteBuf.class, priority = -10000)
public class MixinFriendlyByteBuf
{
    @ModifyConstant(method = "readNbt()Lnet/minecraft/nbt/CompoundTag;", constant = @Constant(longValue = 2097152L))
    private long readNbt(long constant) 
    {
        return 2_147_483_647L;
    }
}
