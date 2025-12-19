package com.min01.solomonlib.mixin.packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

@Mixin(value = ClientboundCustomPayloadPacket.class, priority = -30000)
public class MixinClientboundCustomPayloadPacket
{
    @ModifyConstant(method = {"<init>*"}, constant = @Constant(intValue = 1048576))
    private int init(int constant)
    {
        return 2147483647;
    }
}
