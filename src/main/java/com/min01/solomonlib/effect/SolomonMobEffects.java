package com.min01.solomonlib.effect;

import com.min01.solomonlib.SolomonLib;

import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SolomonMobEffects 
{
	public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SolomonLib.MODID);
	public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, SolomonLib.MODID);

    public static final RegistryObject<GravityDirectionMobEffect> DOWN = EFFECTS.register("down", () -> new GravityDirectionMobEffect(Direction.DOWN));
    public static final RegistryObject<GravityDirectionMobEffect> UP = EFFECTS.register("up", () -> new GravityDirectionMobEffect(Direction.UP));
    public static final RegistryObject<GravityDirectionMobEffect> NORTH = EFFECTS.register("north", () -> new GravityDirectionMobEffect(Direction.NORTH));
    public static final RegistryObject<GravityDirectionMobEffect> SOUTH = EFFECTS.register("south", () -> new GravityDirectionMobEffect(Direction.SOUTH));
    public static final RegistryObject<GravityDirectionMobEffect> WEST = EFFECTS.register("west", () -> new GravityDirectionMobEffect(Direction.WEST));
    public static final RegistryObject<GravityDirectionMobEffect> EAST = EFFECTS.register("east", () -> new GravityDirectionMobEffect(Direction.EAST));
    
    public static final RegistryObject<GravityInvertMobEffect> INVERT = EFFECTS.register("invert", () -> new GravityInvertMobEffect());
    
    public static final RegistryObject<GravityStrengthMobEffect> INCREASE = EFFECTS.register("increase", () -> new GravityStrengthMobEffect(0x98D982, 1.2, 1));
    public static final RegistryObject<GravityStrengthMobEffect> DECREASE = EFFECTS.register("decrease", () -> new GravityStrengthMobEffect(0x98D982, 0.7, 1));
    
    // it turns gravity into levitation but does not change player orientation
    public static final RegistryObject<GravityStrengthMobEffect> REVERSE = EFFECTS.register("reverse", () -> new GravityStrengthMobEffect(0x98D982, 1.0, -1));
    
    public static final RegistryObject<Potion> DOWN_POTION = POTIONS.register("gravity_down_0", () -> new Potion(new MobEffectInstance(DOWN.get(), 9600, 1)));
    
    public static final RegistryObject<Potion> UP_POTION = POTIONS.register("gravity_up_0", () -> new Potion(new MobEffectInstance(UP.get(), 9600, 1)));
    
    public static final RegistryObject<Potion> NORTH_POTION = POTIONS.register("gravity_north_0", () -> new Potion(new MobEffectInstance(NORTH.get(), 9600, 1)));
    
    public static final RegistryObject<Potion> SOUTH_POTION = POTIONS.register("gravity_south_0", () -> new Potion(new MobEffectInstance(SOUTH.get(), 9600, 1)));

    public static final RegistryObject<Potion> WEST_POTION = POTIONS.register("gravity_west_0", () -> new Potion(new MobEffectInstance(WEST.get(), 9600, 1)));
    
    public static final RegistryObject<Potion> EAST_POTION = POTIONS.register("gravity_east_0", () -> new Potion(new MobEffectInstance(EAST.get(), 9600, 1)));
    
    public static final RegistryObject<Potion> STRENGTH_DECR_POTION_0 = POTIONS.register("gravity_decr_0", () -> new Potion(new MobEffectInstance(DECREASE.get(), 9600, 0)));
    
    public static final RegistryObject<Potion> STRENGTH_DECR_POTION_1 = POTIONS.register("gravity_decr_1", () -> new Potion(new MobEffectInstance(DECREASE.get(), 9600, 1)));
    
    public static final RegistryObject<Potion> STRENGTH_INCR_POTION_0 = POTIONS.register("gravity_incr_0", () -> new Potion(new MobEffectInstance(INCREASE.get(), 9600, 0)));
    
    public static final RegistryObject<Potion> STRENGTH_INCR_POTION_1 = POTIONS.register("gravity_incr_1", () -> new Potion(new MobEffectInstance(INCREASE.get(), 9600, 1)));
    
    public static final RegistryObject<Potion> STRENGTH_REVERSE_POTION_0 = POTIONS.register("gravity_reverse_0", () -> new Potion(new MobEffectInstance(REVERSE.get(), 9600, 0)));
    
    public static final RegistryObject<Potion> STRENGTH_REVERSE_POTION_1 = POTIONS.register("gravity_reverse_1", () -> new Potion(new MobEffectInstance( REVERSE.get(), 9600, 1)));
}
