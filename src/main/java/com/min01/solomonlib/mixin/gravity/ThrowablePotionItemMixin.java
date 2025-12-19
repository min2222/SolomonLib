package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ThrowablePotionItem;

@Mixin(value = ThrowablePotionItem.class, priority = 1001)
public abstract class ThrowablePotionItemMixin extends PotionItem {
    
    public ThrowablePotionItemMixin(Properties settings) {
        super(settings);
    }

//    @WrapOperation(
//            method = "use",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/entity/projectile/thrown/PotionEntity;setProperties(Lnet/minecraft/entity/Entity;FFFFF)V"
//            )
//    )
//    public void wrapOperation_use_setProperties(PotionEntity target, Entity user, float pitch, float yaw, float roll, float modifierZ, float modifierXYZ, Operation<Void> original) {
//        original.call(target, user, pitch, yaw, 0.0F, modifierZ, modifierXYZ);
//    }

}



