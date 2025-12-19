package com.min01.solomonlib.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.player.RemotePlayer;

@Mixin(RemotePlayer.class)
public abstract class RemotePlayerEntityMixin {
    // @Override
    // public Direction gravitychanger$getGravityDirection() {
    //     return this.gravitychanger$getTrackedGravityDirection();
    // }
//
    // @Override
    // public void gravitychanger$setGravityDirection(Direction gravityDirection, boolean initialGravity) {
    //     this.gravitychanger$setTrackedGravityDirection(gravityDirection);
    // }
}
