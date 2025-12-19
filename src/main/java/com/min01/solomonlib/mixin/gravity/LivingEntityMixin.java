package com.min01.solomonlib.mixin.gravity;


import com.min01.solomonlib.gravity.GravityAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.solomonlib.gravity.RotationUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract void readAdditionalSaveData(CompoundTag nbt);
    
    @Shadow
    public abstract EntityDimensions getDimensions(Pose pose);
    
    @Shadow
    public abstract float getViewYRot(float tickDelta);
    
    
    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getY()D",
            ordinal = 0
        )
    )
    private double redirect_travel_getY_0(LivingEntity livingEntity) {
        Direction gravityDirection = GravityAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return livingEntity.getY();
        }
        
        return RotationUtil.vecWorldToPlayer(livingEntity.position(), gravityDirection).y;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getY()D",
            ordinal = 1
        )
    )
    private double redirect_travel_getY_1(LivingEntity livingEntity) {
        Direction gravityDirection = GravityAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return livingEntity.getY();
        }
        
        return RotationUtil.vecWorldToPlayer(livingEntity.position(), gravityDirection).y;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getY()D",
            ordinal = 2
        )
    )
    private double redirect_travel_getY_2(LivingEntity livingEntity) {
        Direction gravityDirection = GravityAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return livingEntity.getY();
        }
        
        return RotationUtil.vecWorldToPlayer(livingEntity.position(), gravityDirection).y;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getY()D",
            ordinal = 3
        )
    )
    private double redirect_travel_getY_3(LivingEntity livingEntity) {
        Direction gravityDirection = GravityAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return livingEntity.getY();
        }
        
        return RotationUtil.vecWorldToPlayer(livingEntity.position(), gravityDirection).y;
    }
    
    @ModifyVariable(
        method = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;",
            ordinal = 0
        ),
        ordinal = 2
    )
    private Vec3 modify_travel_Vec3d_2(Vec3 vec3d) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        
        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }
    
    @ModifyArg(
        method = "playBlockFallSound",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
        ),
        index = 0
    )
    private BlockPos modify_playBlockFallSound_getBlockState_0(BlockPos blockPos) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return blockPos;
        }
        
        return BlockPos.containing(this.position().add(RotationUtil.vecPlayerToWorld(0, -0.20000000298023224D, 0, gravityDirection)));
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z",
        at = @At(
            value = "NEW",
            target = "(DDD)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 0
        )
    )
    private Vec3 redirect_canSee_new_0(double x, double y, double z) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return new Vec3(x, y, z);
        }
        
        return this.getEyePosition();
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z",
        at = @At(
            value = "NEW",
            target = "(DDD)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 1
        )
    )
    private Vec3 redirect_canSee_new_1(double x, double y, double z, Entity entity) {
        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return new Vec3(x, y, z);
        }
        
        return entity.getEyePosition();
    }
    
    @Inject(
        method = "Lnet/minecraft/world/entity/LivingEntity;getLocalBoundsForPose(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/phys/AABB;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void inject_getBoundingBox(Pose pose, CallbackInfoReturnable<AABB> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        AABB box = cir.getReturnValue();
        if (gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            box = box.move(0.0D, -1.0E-6D, 0.0D);
        }
        cir.setReturnValue(RotationUtil.boxPlayerToWorld(box, gravityDirection));
    }

    @Inject(
            method = "calculateEntityAnimation",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_updateLimbs(boolean flutter, CallbackInfo ci) {
    	LivingEntity entity = (LivingEntity) (Object) this;
        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
        if(gravityDirection == Direction.DOWN) return;

        ci.cancel();

        Vec3 playerPosDelta = RotationUtil.vecWorldToPlayer(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo, gravityDirection);

        double d = playerPosDelta.x;
        double e = flutter ? playerPosDelta.y : 0.0D;
        double f = playerPosDelta.z;
        float g = (float)Math.sqrt(d * d + e * e + f * f) * 4.0F;
        if (g > 1.0F) {
            g = 1.0F;
        }

        entity.walkAnimation.update(g, 0.4F);
    }
    
    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getX()D",
            ordinal = 0
        )
    )
    private double wrapOperation_tick_getX_0(LivingEntity livingEntity, Operation<Double> original) {
        Direction gravityDirection = GravityAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return original.call(livingEntity);
        }
        
        return RotationUtil.vecWorldToPlayer(original.call(livingEntity) - livingEntity.xo, livingEntity.getY() - livingEntity.yo, livingEntity.getZ() - livingEntity.zo, gravityDirection).x + livingEntity.xo;
    }
    
    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
            ordinal = 0
        )
    )
    private double wrapOperation_tick_getZ_0(LivingEntity livingEntity, Operation<Double> original) {
        Direction gravityDirection = GravityAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return original.call(livingEntity);
        }
        
        return RotationUtil.vecWorldToPlayer(livingEntity.getX() - livingEntity.xo, livingEntity.getY() - livingEntity.yo, original.call(livingEntity) - livingEntity.zo, gravityDirection).z + livingEntity.zo;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getX()D",
            ordinal = 0
        )
    )
    private double redirect_damage_getX_0(Entity attacker) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            if (GravityAPI.getGravityDirection(attacker) == Direction.DOWN) {
                return attacker.getX();
            }
            else {
                return attacker.getEyePosition().x;
            }
        }
        
        return RotationUtil.vecWorldToPlayer(attacker.getEyePosition(), gravityDirection).x;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getZ()D",
            ordinal = 0
        )
    )
    private double redirect_damage_getZ_0(Entity attacker) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            if (GravityAPI.getGravityDirection(attacker) == Direction.DOWN) {
                return attacker.getZ();
            }
            else {
                return attacker.getEyePosition().z;
            }
        }
        
        return RotationUtil.vecWorldToPlayer(attacker.getEyePosition(), gravityDirection).z;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getX()D",
            ordinal = 0
        )
    )
    private double redirect_damage_getX_0(LivingEntity target) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return target.getX();
        }
        
        return RotationUtil.vecWorldToPlayer(target.position(), gravityDirection).x;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
            ordinal = 0
        )
    )
    private double redirect_damage_getZ_0(LivingEntity target) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return target.getZ();
        }
        
        return RotationUtil.vecWorldToPlayer(target.position(), gravityDirection).z;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;blockedByShield(Lnet/minecraft/world/entity/LivingEntity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getX()D",
            ordinal = 0
        )
    )
    private double redirect_knockback_getX_0(LivingEntity target) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return target.getX();
        }
        
        return RotationUtil.vecWorldToPlayer(target.position(), gravityDirection).x;
    }
    
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;blockedByShield(Lnet/minecraft/world/entity/LivingEntity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
            ordinal = 0
        )
    )
    private double redirect_knockback_getZ_0(LivingEntity target) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return target.getZ();
        }
        
        return RotationUtil.vecWorldToPlayer(target.position(), gravityDirection).z;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;blockedByShield(Lnet/minecraft/world/entity/LivingEntity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getX()D",
            ordinal = 1
        )
    )
    private double redirect_knockback_getX_1(LivingEntity attacker, LivingEntity target) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            if (GravityAPI.getGravityDirection(attacker) == Direction.DOWN) {
                return attacker.getX();
            }
            else {
                return attacker.getEyePosition().x;
            }
        }
        
        return RotationUtil.vecWorldToPlayer(attacker.getEyePosition(), gravityDirection).x;
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;blockedByShield(Lnet/minecraft/world/entity/LivingEntity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
            ordinal = 1
        )
    )
    private double redirect_knockback_getZ_1(LivingEntity attacker, LivingEntity target) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            if (GravityAPI.getGravityDirection(attacker) == Direction.DOWN) {
                return attacker.getZ();
            }
            else {
                return attacker.getEyePosition().z;
            }
        }
        
        return RotationUtil.vecWorldToPlayer(attacker.getEyePosition(), gravityDirection).z;
    }
    
    @WrapOperation(
        method = "spawnItemParticles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 0
        )
    )
    private Vec3 wrapOperation_spawnItemParticles_add_0(Vec3 vec3d, double x, double y, double z, Operation<Vec3> original) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return original.call(vec3d, x, y, z);
        }
        
        Vec3 rotated = RotationUtil.vecPlayerToWorld(vec3d, gravityDirection);
        return original.call(this.getEyePosition(), rotated.x, rotated.y, rotated.z);
    }
    
    @ModifyVariable(
        method = "Lnet/minecraft/world/entity/LivingEntity;spawnItemParticles(Lnet/minecraft/world/item/ItemStack;I)V",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/phys/Vec3;yRot(F)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 0
        ),
        ordinal = 0
    )
    private Vec3 modify_spawnItemParticles_Vec3d_0(Vec3 vec3d) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        
        return RotationUtil.vecPlayerToWorld(vec3d, gravityDirection);
    }
    
    @WrapOperation(
        method = "tickEffects",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
        )
    )
    private void modify_tickStatusEffects_addParticle_0(Level instance, ParticleOptions particle, double x, double y, double z, double dx, double dy, double dz, Operation<Void> original) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN)
        {
            original.call(instance, particle, x, y, z, dx, dy, dz);
        }
        else {
            Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(x, y, z), gravityDirection));
            original.call(instance, particle, vec3d.x, vec3d.y, vec3d.z, dx, dy, dz);
        }

    }
    
    @WrapOperation(
        method = "makePoofParticles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
            ordinal = 0
        )
    )
    private void modify_addDeathParticless_addParticle_0(Level instance, ParticleOptions particle, double x, double y, double z, double dx, double dy, double dz, Operation<Void> original) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            original.call(instance, particle, x, y, z, dx, dy, dz);
        }
        else {
            Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(x, y, z), gravityDirection));
            original.call(instance, particle, vec3d.x, vec3d.y, vec3d.z, dx, dy, dz);
        }
    }
    
    @ModifyVariable(
        method = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/entity/LivingEntity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 0
        ),
        ordinal = 1
    )
    private Vec3 modify_blockedByShield_Vec3d_1(Vec3 vec3d) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        
        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }
    
    // TODO shield knockback
//    @ModifyArg(
//        method = "blockedByShield",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/world/phys/Vec3;vectorTo(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
//            ordinal = 0
//        ),
//        index = 0
//    )
//    private Vec3 modify_blockedByShield_relativize_0(Vec3 vec3d) {
//        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity)(Object)this);
//        if(gravityDirection == Direction.DOWN) {
//            return vec3d;
//        }
//
//        return this.getEyePosition();
//    }

//    @ModifyVariable(
//        method = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z",
//        at = @At(
//            value = "INVOKE_ASSIGN",
//            target = "Lnet/minecraft/world/phys/Vec3;normalize()Lnet/minecraft/world/phys/Vec3;",
//            ordinal = 0
//        ),
//        ordinal = 2
//    )
//    private Vec3 modify_blockedByShield_Vec3d_2(Vec3 vec3d) {
//        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity)(Object)this);
//        if(gravityDirection == Direction.DOWN) {
//            return vec3d;
//        }
//
//        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
//    }
    
    @ModifyConstant(method = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V", constant = @Constant(doubleValue = 0.08))
    private double multiplyGravity(double constant) {
        return constant * GravityAPI.getGravityStrength(this);
    }
    
    @ModifyVariable(method = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(FF)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float diminishFallDamage(float value) {
        return value * (float) Math.sqrt(GravityAPI.getGravityStrength(this));
    }
}
