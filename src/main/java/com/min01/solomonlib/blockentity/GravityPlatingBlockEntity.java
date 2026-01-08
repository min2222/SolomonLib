package com.min01.solomonlib.blockentity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import com.min01.solomonlib.block.GravityPlatingBlock;
import com.min01.solomonlib.block.SolomonBlocks;
import com.min01.solomonlib.config.SolomonConfig;
import com.min01.solomonlib.gravity.GravityAPI;
import com.min01.solomonlib.gravity.GravityCapabilityImpl;
import com.min01.solomonlib.gravity.RotationUtil;
import com.min01.solomonlib.item.GravityPlatingItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Based on code from AmethystGravity (by CyborgCabbage)
 */
public class GravityPlatingBlockEntity extends BlockEntity
{
    private static final int MAX_LEVEL = 64;
    
    public GravityPlatingBlockEntity(BlockPos pos, BlockState state) 
    {
        super(SolomonBlocks.GRAVITY_PLATING_BLOCK_ENTITY.get(), pos, state);
    }
    
    public static class SideData
    {
        public boolean isAttracting = true;
        public int level = 1;
        
        public @Nullable AABB effectBoxCache = null;
        
        public SideData(boolean isAttracting, int level) 
        {
            this.isAttracting = isAttracting;
            this.level = level;
        }
        
        public static SideData createDefault()
        {
            return new SideData(true, 1);
        }
        
        public static SideData fromTag(CompoundTag tag) 
        {
            boolean isAttracting_ = tag.getBoolean("isAttracting");
            int level_ = tag.getInt("level");
            
            level_ = Mth.clamp(level_, 1, MAX_LEVEL);
            
            return new SideData(isAttracting_, level_);
        }
        
        public CompoundTag toTag()
        {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("isAttracting", isAttracting);
            tag.putInt("level", level);
            return tag;
        }
        
        public double getEffectRange() 
        {
            return level - 0.1;
        }
        
        public AABB getEffectBox(BlockPos blockPos, Direction plateDir, Level world) 
        {
            if(this.effectBoxCache == null)
            {
                double expand = 0.001;
                
                double minX = blockPos.getX() - expand;
                double minY = blockPos.getY() - expand;
                double minZ = blockPos.getZ() - expand;
                double maxX = blockPos.getX() + 1 + expand;
                double maxY = blockPos.getY() + 1 + expand;
                double maxZ = blockPos.getZ() + 1 + expand;
                
                double delta = this.getEffectRange() - 1;
                switch (plateDir)
                {
                    case DOWN -> maxY += delta;
                    case UP -> minY -= delta;
                    case NORTH -> maxZ += delta;
                    case SOUTH -> minZ -= delta;
                    case WEST -> maxX += delta;
                    case EAST -> minX -= delta;
                }
                
                BlockPos wallPos = blockPos.relative(plateDir);
                for(Direction sideDir : Direction.values())
                {
                    if(sideDir.getAxis() == plateDir.getAxis()) 
                    {
                    	continue;
                    }
                    
                    BlockPos sidePos = wallPos.relative(sideDir);
                    BlockState sideBlockState = world.getBlockState(sidePos);
                    if(!(sideBlockState.getBlock() instanceof GravityPlatingBlock)) 
                    {
                    	continue;
                    }
                    
                    if(!GravityPlatingBlock.hasDir(sideBlockState, sideDir.getOpposite())) 
                    {
                    	continue;
                    }
                    
                    if(!(world.getBlockEntity(sidePos) instanceof GravityPlatingBlockEntity)) 
                    {
                    	continue;
                    }
                    
                    double sideDelta = this.getEffectRange();
                    switch (sideDir) 
                    {
                        case DOWN -> minY -= sideDelta;
                        case UP -> maxY += sideDelta;
                        case NORTH -> minZ -= sideDelta;
                        case SOUTH -> maxZ += sideDelta;
                        case WEST -> minX -= sideDelta;
                        case EAST -> maxX += sideDelta;
                    }
                }
                
                this.effectBoxCache = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
            }
            
            return this.effectBoxCache;
        }
    }
    
    private @Nullable SideData[] sideData = null;
    
    private @Nullable AABB roughAreaBoxCache = null;
    
    @Override
    public void load(CompoundTag tag) 
    {
        super.load(tag);
        
        this.sideData = new SideData[6];
        for(Direction dir : Direction.values())
        {
            String dirName = dir.getName();
            if(tag.contains(dirName))
            {
                CompoundTag sideTag = tag.getCompound(dirName);
                this.sideData[dir.ordinal()] = SideData.fromTag(sideTag);
            }
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) 
    {
        super.saveAdditional(tag);
        
        if(this.sideData != null) 
        {
            for(Direction dir : Direction.values()) 
            {
                String dirName = dir.getName();
                SideData side = this.sideData[dir.ordinal()];
                if(side != null)
                {
                    tag.put(dirName, side.toTag());
                }
            }
        }
    }
    
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public CompoundTag getUpdateTag() 
    {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }
    
    public void refreshCache()
    {
        Level world = this.getLevel();
        
        if(world == null) 
        {
            //LOGGER.error("Refreshing cache when world is null {}", this);
            return;
        }
        
        if(this.sideData == null)
        {
        	this.sideData = new SideData[6];
        }
        
        BlockState blockState = world.getBlockState(this.getBlockPos());
        for(Direction dir : Direction.values()) 
        {
            if(GravityPlatingBlock.hasDir(blockState, dir)) 
            {
                if(this.sideData[dir.ordinal()] == null) 
                {
                	this.sideData[dir.ordinal()] = SideData.createDefault();
                }
            }
            else 
            {
            	this.sideData[dir.ordinal()] = null;
            }
        }
        
        if(this.worldPosition.hashCode() % 5 == world.getGameTime() % 5) 
        {
        	this.roughAreaBoxCache = null;
            for(SideData sideDatum : this.sideData) 
            {
                if(sideDatum != null) 
                {
                    sideDatum.effectBoxCache = null;
                }
            }
        }
    }
    
    private AABB getRoughEffectBox() 
    {
        if(this.roughAreaBoxCache == null) 
        {
            double maxRange = 0;
            for (SideData sideDatum : this.sideData)
            {
                if(sideDatum != null)
                {
                    maxRange = Math.max(maxRange, sideDatum.getEffectRange());
                }
            }
            
            BlockPos blockPos = this.getBlockPos();
            double expand = 0.001;
            double delta = maxRange + expand;
            return new AABB(blockPos.getX() - delta, blockPos.getY() - delta, blockPos.getZ() - delta, blockPos.getX() + 1 + delta, blockPos.getY() + 1 + delta, blockPos.getZ() + 1 + delta);
        }
        return this.roughAreaBoxCache;
    }
    
    public static void tick(Level world, BlockPos blockPos, BlockState blockState, GravityPlatingBlockEntity be)
    {
        if(!(blockState.getBlock() instanceof GravityPlatingBlock)) 
        {
            return;
        }
        
        be.refreshCache();
        
        AABB roughBox = be.getRoughEffectBox();
        
        List<Entity> entities = world.getEntitiesOfClass(Entity.class, roughBox);
        
        for(Entity entity : entities) 
        {
            boolean applies = false;
            
            GravityCapabilityImpl comp = GravityAPI.getGravityComponent(entity);
            Direction entityGravityDir = comp.getCurrGravityDirection();
            
            for(Direction plateDir : Direction.values()) 
            {
                SideData sideDatum = be.sideData[plateDir.ordinal()];
                if(sideDatum != null)
                {
                    Direction gravityEffectDir = sideDatum.isAttracting ? plateDir : plateDir.getOpposite();
                    
                    // when the player has no gravity effect and is touching the plate with their eyes,
                    // test the eye pos
                    boolean isOpposite = (entityGravityDir == gravityEffectDir.getOpposite());
                    Vec3 testingPos = isOpposite ? entity.getEyePosition() : entity.position();
                    
                    AABB gravityEffectBox = sideDatum.getEffectBox(blockPos, plateDir, world);
                    if(!gravityEffectBox.contains(testingPos))
                    {
                        continue;
                    }
                    
                    Vec3 plateDirVec = Vec3.atLowerCornerOf(plateDir.getNormal());
                    Vec3 effectCenter = Vec3.atCenterOf(blockPos).add(plateDirVec.scale(0.5));
                    
                    // move the center out a little
                    // to make the distance to sharing edge different to different plates
                    double adjustment = 0.1;
                    Vec3 effectCenterAdjusted = effectCenter.add(plateDirVec.scale(-adjustment));
                    
                    Vec3 deltaVec = testingPos.subtract(effectCenterAdjusted);
                    
                    double distanceToPlane = -deltaVec.dot(plateDirVec);
                    if(distanceToPlane < -adjustment - 0.001) 
                    {
                        continue;
                    }
                    
                    Vec3 localVec = RotationUtil.vecWorldToPlayer(deltaVec, plateDir);
                    double dx = GravityAPI.distanceToRange(localVec.x, -0.5, 0.5);
                    double dz = GravityAPI.distanceToRange(localVec.z, -0.5, 0.5);
                    double distanceToPlate = Math.sqrt(dx * dx + dz * dz + distanceToPlane * distanceToPlane);
                    
                    double priority = 1000 - distanceToPlate;
                    if(isOpposite)
                    {
                        // reduce the chance of opposite side plating interference
                        priority -= 10;
                    }
                    comp.applyGravityDirectionEffect(gravityEffectDir, null, priority);
                    applies = true;
                }
            }
            
            if(applies && SolomonConfig.autoJumpOnGravityPlateInnerCorner.get()) 
            {
                tryToDoCornerAutoJump(blockState, blockPos, entity, comp);
            }
        }
    }
    
    // when approaching an inward corner, do auto-jump to make it smoothly go forward
    private static void tryToDoCornerAutoJump(BlockState blockState, BlockPos blockPos, Entity entity, GravityCapabilityImpl comp) 
    {
        if(!entity.onGround())
        {
            return;
        }
        
        // apply levitation when the entity is close to corner
        Direction entityGravityDir = comp.getCurrGravityDirection();
        
        for(Direction plateDir : Direction.values())
        {
            if(GravityPlatingBlock.hasDir(blockState, plateDir)) 
            {
                boolean orthogonal = entityGravityDir.getAxis() != plateDir.getAxis();
                if(!orthogonal)
                {
                    continue;
                }
                
                Vec3 plateDirVec = Vec3.atLowerCornerOf(plateDir.getNormal());
                
                Vec3 effectCenter = Vec3.atCenterOf(blockPos).add(plateDirVec.scale(0.5));
                Vec3 offset = effectCenter.subtract(entity.position());
                if(offset.dot(Vec3.atLowerCornerOf(entityGravityDir.getNormal())) > 0)
                {
                    // that plate is lower than entity
                    continue;
                }
                
                Vec3 worldVelocity = GravityAPI.getWorldVelocity(entity);
                if(worldVelocity.dot(plateDirVec) < 0.01)
                {
                    continue;
                }
                
                double distanceToPlate = Math.abs(entity.position().subtract(effectCenter).dot(plateDirVec));
                if(distanceToPlate < 0.8) 
                {
                    double strengthSqrt = Math.sqrt(comp.getCurrGravityStrength());
                    
                    Vec3 entityGravityVec = Vec3.atLowerCornerOf(entityGravityDir.getNormal());
                    
                    Vec3 deltaWorldVelocity = entityGravityVec.scale(-strengthSqrt * 0.4).add(plateDirVec.scale(0.08));
                    
                    GravityAPI.setWorldVelocity(entity, GravityAPI.getWorldVelocity(entity).add(deltaWorldVelocity));
                    
                    if(entity.level.isClientSide()) 
                    {
                        //LOGGER.info("Client entity auto-jump on gravity plate corner {}", entity);
                    }
                    return;
                }
            }
        }
        
    }
    
    public InteractionResult interact(Level level, BlockPos pos, Direction plateDir, Player player, InteractionHand hand) 
    {
        if(level.isClientSide())
        {
            return InteractionResult.SUCCESS;
        }
        
        this.refreshCache();
        
        SideData sideDatum = sideData[plateDir.ordinal()];
        
        if(sideDatum == null) 
        {
            return InteractionResult.FAIL;
        }
        
        ItemStack handItem = player.getItemInHand(hand);
        if(handItem.getItem() == Items.AIR)
        {
            // reducing level
            if(sideDatum.level != 1) 
            {
                sideDatum.level -= 1;
                if(!player.isCreative())
                {
                    player.getInventory().add(new ItemStack(Items.AMETHYST_CLUSTER));
                }
            }
            else 
            {
                sideDatum.isAttracting = !sideDatum.isAttracting;
            }
        }
        else if(handItem.getItem() == Items.AMETHYST_CLUSTER) 
        {
            if(!player.isCreative())
            {
                handItem.shrink(1);
            }
            
            sideDatum.level += 1;
            
            if(sideDatum.level > MAX_LEVEL) 
            {
                sideDatum.level = MAX_LEVEL;
            }
        }
        else
        {
            ((ServerPlayer) player).sendSystemMessage(Component.translatable("gravity_changer.plate.wrong_interaction"), true);
            return InteractionResult.FAIL;
        }
        
        this.sync();
        
        boolean isAttracting = sideDatum.isAttracting;
        ((ServerPlayer) player).sendSystemMessage(Component.translatable("gravity_changer.plate.status", GravityAPI.getDirectionText(plateDir.getOpposite()), sideDatum.level, getForceText(isAttracting)), true);
        
        return InteractionResult.SUCCESS;
    }
    
    public static MutableComponent getForceText(boolean isAttracting) 
    {
        return Component.translatable(isAttracting ? "gravity_changer.plate.force.attract" : "gravity_changer.plate.force.repulse");
    }
    
    public void sync()
    {
        Level world = this.getLevel();
        Validate.notNull(world);
        Validate.isTrue(!world.isClientSide());
        
        this.setChanged();
        
        // make the packet to be sent from ChunkHolder, so the packet will be redirected by ImmPtl
        // don't directly send update packet
        ((ServerChunkCache) world.getChunkSource()).blockChanged(this.getBlockPos());
    }
    
    public void onPlacing(Direction side, SideData sideData) 
    {
    	this.refreshCache();
        this.sideData[side.ordinal()] = sideData;
        this.sync();
    }
    
    public List<ItemStack> getDrops()
    {
        if(this.sideData == null)
        {
            return List.of();
        }
        
        List<ItemStack> drops = new ArrayList<>();
        for(Direction value : Direction.values())
        {
            SideData sideDatum = this.sideData[value.ordinal()];
            if(sideDatum != null)
            {
                ItemStack stack = GravityPlatingItem.createStack(sideDatum);
                drops.add(stack);
            }
        }
        return drops;
    }
}

