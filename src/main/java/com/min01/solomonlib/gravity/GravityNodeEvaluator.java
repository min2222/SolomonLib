package com.min01.solomonlib.gravity;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GravityNodeEvaluator extends WalkNodeEvaluator
{
    private Direction gravityDirection = Direction.DOWN;
    private final Direction[] horizontalDirections = new Direction[4];
    private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap<>();
    
    @Override
    public void prepare(PathNavigationRegion p_77620_, Mob p_77621_) 
    {
        this.collisionCache.clear();
        super.prepare(p_77620_, p_77621_);
        this.gravityDirection = GravityAPI.getGravityDirection(p_77621_);
        this.updateHorizontalDirections();
    }
    
    private void updateHorizontalDirections() 
    {
        this.horizontalDirections[0] = RotationUtil.dirPlayerToWorld(Direction.NORTH, this.gravityDirection);
        this.horizontalDirections[1] = RotationUtil.dirPlayerToWorld(Direction.SOUTH, this.gravityDirection);
        this.horizontalDirections[2] = RotationUtil.dirPlayerToWorld(Direction.WEST, this.gravityDirection);
        this.horizontalDirections[3] = RotationUtil.dirPlayerToWorld(Direction.EAST, this.gravityDirection);
    }
    
    @Override
    public Node getStart() 
    {
    	BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
    	BlockPos blockPos = this.mob.blockPosition();
    	BlockState blockstate = this.level.getBlockState(blockpos$mutableblockpos.set(blockPos));
    	if(!this.mob.canStandOnFluid(blockstate.getFluidState())) 
    	{
    		if(this.canFloat() && this.mob.isInWater())
    		{
    			while(true)
    			{
    				if(!blockstate.is(Blocks.WATER) && blockstate.getFluidState() != Fluids.WATER.getSource(false)) 
    				{
    					blockPos = blockPos.relative(this.gravityDirection);
    					break;
    				}
					blockPos = blockPos.relative(this.gravityDirection.getOpposite());
    				blockstate = this.level.getBlockState(blockpos$mutableblockpos.set(blockPos));
    			}
           }
    		else if(this.mob.onGround()) 
    		{
    			blockPos = this.mob.blockPosition();
    		}
    		else 
    		{
    			int i = 0;
    			BlockPos blockpos;
    			for(blockpos = this.mob.blockPosition(); (this.level.getBlockState(blockpos).isAir() || this.level.getBlockState(blockpos).isPathfindable(this.level, blockpos, PathComputationType.LAND)) && i < this.mob.level.getMaxBuildHeight(); blockpos = blockpos.relative(this.gravityDirection))
    			{
    				i++;
    			}
    			blockPos = blockpos.relative(this.gravityDirection.getOpposite());
    		}
        } 
    	else 
    	{
    		while(this.mob.canStandOnFluid(blockstate.getFluidState())) 
    		{
				blockPos = blockPos.relative(this.gravityDirection.getOpposite());
    			blockstate = this.level.getBlockState(blockpos$mutableblockpos.set(blockPos));
    		}
			blockPos = blockPos.relative(this.gravityDirection);
        }
        return this.getStartNode(blockPos);
    }
    
    @Override
    protected double getFloorLevel(BlockPos p_164733_) 
    {
    	return (this.canFloat() || this.isAmphibious()) && this.level.getFluidState(p_164733_).is(FluidTags.WATER) ? (double)p_164733_.get(this.gravityDirection.getAxis()) + 0.5D : this.getFloorLevelForGravity(this.level, p_164733_);
    }
    
    public double getFloorLevelForGravity(BlockGetter p_77612_, BlockPos p_77613_) 
    {
    	BlockPos blockpos = p_77613_.relative(this.gravityDirection);
    	VoxelShape voxelshape = p_77612_.getBlockState(blockpos).getCollisionShape(p_77612_, blockpos);
        return (double)blockpos.get(this.gravityDirection.getAxis()) + (voxelshape.isEmpty() ? 0.0D : voxelshape.max(this.gravityDirection.getOpposite().getAxis()));
    }
    
    @Override
    public int getNeighbors(Node[] neighbors, Node currentNode) 
    {
        int i = 0;
        int stepHeight = 0;
        
        BlockPos currentNodePos = currentNode.asBlockPos();
        BlockPos upPos = currentNodePos.relative(this.gravityDirection.getOpposite());

        BlockPathTypes pathTypeUp = this.getCachedBlockType(this.mob, upPos.getX(), upPos.getY(), upPos.getZ());
        BlockPathTypes pathTypeCurrent = this.getCachedBlockType(this.mob, currentNodePos.getX(), currentNodePos.getY(), currentNodePos.getZ());

        if(this.mob.getPathfindingMalus(pathTypeUp) >= 0.0F && pathTypeCurrent != BlockPathTypes.STICKY_HONEY) 
        {
            stepHeight = Mth.floor(Math.max(1.0F, this.mob.getStepHeight()));
        }

        double floorLevel = this.getFloorLevel(currentNodePos);

        Node southNode = this.findAcceptedNode(currentNodePos.relative(this.horizontalDirections[1]), stepHeight, floorLevel, this.horizontalDirections[1], pathTypeCurrent);
        if(this.isNeighborValid(southNode, currentNode))
        {
        	neighbors[i++] = southNode;
        }

        Node westNode = this.findAcceptedNode(currentNodePos.relative(this.horizontalDirections[2]), stepHeight, floorLevel, this.horizontalDirections[2], pathTypeCurrent);
        if(this.isNeighborValid(westNode, currentNode))
        {
        	neighbors[i++] = westNode;
        }

        Node eastNode = this.findAcceptedNode(currentNodePos.relative(this.horizontalDirections[3]), stepHeight, floorLevel, this.horizontalDirections[3], pathTypeCurrent);
        if(this.isNeighborValid(eastNode, currentNode))
        {
        	neighbors[i++] = eastNode;
        }

        Node northNode = this.findAcceptedNode(currentNodePos.relative(this.horizontalDirections[0]), stepHeight, floorLevel, this.horizontalDirections[0], pathTypeCurrent);
        if(this.isNeighborValid(northNode, currentNode))
        {
        	neighbors[i++] = northNode;
        }

        Node northWestNode = this.findAcceptedNode(currentNodePos.relative(this.horizontalDirections[0]).relative(this.horizontalDirections[2]), stepHeight, floorLevel, this.horizontalDirections[0], pathTypeCurrent);
        if(this.isDiagonalValid(currentNode, westNode, northNode, northWestNode)) 
        {
            neighbors[i++] = northWestNode;
        }

        Node northEastNode = this.findAcceptedNode(currentNodePos.relative(this.horizontalDirections[0]).relative(this.horizontalDirections[3]), stepHeight, floorLevel, this.horizontalDirections[0], pathTypeCurrent);
        if(this.isDiagonalValid(currentNode, eastNode, northNode, northEastNode)) {
            neighbors[i++] = northEastNode;
        }

        Node southWestNode = this.findAcceptedNode(currentNodePos.relative(this.horizontalDirections[1]).relative(this.horizontalDirections[2]), stepHeight, floorLevel, this.horizontalDirections[1], pathTypeCurrent);
        if(this.isDiagonalValid(currentNode, westNode, southNode, southWestNode))
        {
            neighbors[i++] = southWestNode;
        }
        
        Node southEastNode = this.findAcceptedNode(currentNodePos.relative(this.horizontalDirections[1]).relative(this.horizontalDirections[3]), stepHeight, floorLevel, this.horizontalDirections[1], pathTypeCurrent);
        if(this.isDiagonalValid(currentNode, eastNode, southNode, southEastNode)) 
        {
            neighbors[i++] = southEastNode;
        }
        
        return i;
    }
    
    private int getGravityAxisCoord(Node node) 
    {
        return node.asBlockPos().get(this.gravityDirection.getAxis());
    }

    private boolean isLowerOrSame(Node node1, Node node2) 
    {
        int coord1 = this.getGravityAxisCoord(node1);
        int coord2 = this.getGravityAxisCoord(node2);
        
        if(this.gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) 
        {
            return coord1 >= coord2;
        }
        else 
        {
            return coord1 <= coord2;
        }
    }

    private boolean isLower(Node node1, Node node2)
    {
        int coord1 = this.getGravityAxisCoord(node1);
        int coord2 = this.getGravityAxisCoord(node2);
        
        if(this.gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) 
        {
            return coord1 > coord2;
        } 
        else 
        {
            return coord1 < coord2;
        }
    }

    @Override
    protected boolean isDiagonalValid(Node p_77630_, @Nullable Node p_77631_, @Nullable Node p_77632_, @Nullable Node p_77633_)
    {
        if(p_77633_ == null || p_77632_ == null || p_77631_ == null)
        {
            return false;
        }
        
        if(p_77633_.closed) 
        {
            return false;
        }
        
        if(this.isLowerOrSame(p_77632_, p_77630_) && this.isLowerOrSame(p_77631_, p_77630_))
        {
            if(p_77631_.type == BlockPathTypes.WALKABLE_DOOR || p_77632_.type == BlockPathTypes.WALKABLE_DOOR || p_77633_.type == BlockPathTypes.WALKABLE_DOOR)
            {
                return false;
            }
            boolean flag = p_77632_.type == BlockPathTypes.FENCE && p_77631_.type == BlockPathTypes.FENCE && (double)this.mob.getBbWidth() < 0.5D;
            return p_77633_.costMalus >= 0.0F && (this.isLower(p_77632_, p_77630_) || p_77632_.costMalus >= 0.0F || flag) && (this.isLower(p_77631_, p_77630_) || p_77631_.costMalus >= 0.0F || flag);
        }
        
        return false;
    }
    
    @Override
    protected Node findAcceptedNode(int p_164726_, int p_164727_, int p_164728_, int p_164729_, double p_164730_, Direction p_164731_, BlockPathTypes p_164732_) 
    {
        Node node = null;
        BlockPos currentPos = new BlockPos(p_164726_, p_164727_, p_164728_);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(p_164726_, p_164727_, p_164728_);

        double d0 = this.getFloorLevel(mutablePos);
        if(d0 - p_164730_ > this.getMobJumpHeight())
        {
            return null;
        }

        BlockPathTypes blockpathtypes = this.getCachedBlockType(this.mob, p_164726_, p_164727_, p_164728_);
        float f = this.mob.getPathfindingMalus(blockpathtypes);
        if(f >= 0.0F) 
        {
            node = this.getNodeAndUpdateCostToMax(p_164726_, p_164727_, p_164728_, blockpathtypes, f);
        }

        if(doesBlockHavePartialCollision(p_164732_) && node != null && node.costMalus >= 0.0F && !this.canReachWithoutCollision(node))
        {
            node = null;
        }

        if (blockpathtypes != BlockPathTypes.WALKABLE && (!this.isAmphibious() || blockpathtypes != BlockPathTypes.WATER))
        {
            if((node == null || node.costMalus < 0.0F) && p_164729_ > 0 && (blockpathtypes != BlockPathTypes.FENCE || this.canWalkOverFences()) && blockpathtypes != BlockPathTypes.UNPASSABLE_RAIL && blockpathtypes != BlockPathTypes.TRAPDOOR && blockpathtypes != BlockPathTypes.POWDER_SNOW) 
            {
                BlockPos upPos = currentPos.relative(this.gravityDirection.getOpposite());
                node = this.findAcceptedNode(upPos.getX(), upPos.getY(), upPos.getZ(), p_164729_ - 1, p_164730_, p_164731_, p_164732_);

                if (node != null && (node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE) && this.mob.getBbWidth() < 1.0F)
                {
                	AABB rawAABB = this.getBoundingBoxForPose(this.mob.getPose());
                	if(this.gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE)
                	{
                		rawAABB = rawAABB.move(0.0D, -1.0E-6D, 0.0D);
                	}
                    Vec3 targetPosition = Vec3.atBottomCenterOf(node.asBlockPos());
                    AABB aabb = RotationUtil.boxPlayerToWorld(rawAABB, this.gravityDirection).move(targetPosition);
                    if(this.hasCollisions(aabb))
                    {
                        node = null;
                    }
                }
            }

            if(!this.isAmphibious() && blockpathtypes == BlockPathTypes.WATER && !this.canFloat()) 
            {
                BlockPos downPos = currentPos.relative(this.gravityDirection);
                if(this.getCachedBlockType(this.mob, downPos.getX(), downPos.getY(), downPos.getZ()) != BlockPathTypes.WATER)
                {
                    return node;
                }
                
                mutablePos.set(currentPos);
                while(mutablePos.getY() > this.mob.level.getMinBuildHeight())
                {
                    mutablePos.move(this.gravityDirection);
                    blockpathtypes = this.getCachedBlockType(this.mob, mutablePos.getX(), mutablePos.getY(), mutablePos.getZ());
                    if(blockpathtypes != BlockPathTypes.WATER)
                    {
                        return node;
                    }
                    node = this.getNodeAndUpdateCostToMax(mutablePos.getX(), mutablePos.getY(), mutablePos.getZ(), blockpathtypes, this.mob.getPathfindingMalus(blockpathtypes));
                }
            }

            if(blockpathtypes == BlockPathTypes.OPEN)
            {
                int fallDistance = 0;
                mutablePos.set(currentPos);
                while(blockpathtypes == BlockPathTypes.OPEN)
                {
                    mutablePos.move(this.gravityDirection);
                    if(mutablePos.getY() < this.mob.level.getMinBuildHeight())
                    {
                        return this.getBlockedNode(p_164726_, p_164727_, p_164728_);
                    }
                    
                    if(fallDistance++ >= this.mob.getMaxFallDistance())
                    {
                        return this.getBlockedNode(mutablePos.getX(), mutablePos.getY(), mutablePos.getZ());
                    }

                    blockpathtypes = this.getCachedBlockType(this.mob, mutablePos.getX(), mutablePos.getY(), mutablePos.getZ());
                    f = this.mob.getPathfindingMalus(blockpathtypes);
                    
                    if(blockpathtypes != BlockPathTypes.OPEN && f >= 0.0F) 
                    {
                        node = this.getNodeAndUpdateCostToMax(mutablePos.getX(), mutablePos.getY(), mutablePos.getZ(), blockpathtypes, f);
                        break;
                    }
                    
                    if(f < 0.0F)
                    {
                        return this.getBlockedNode(mutablePos.getX(), mutablePos.getY(), mutablePos.getZ());
                    }
                }
            }

            if(doesBlockHavePartialCollision(blockpathtypes) && node == null)
            {
                node = this.getNode(p_164726_, p_164727_, p_164728_);
                node.closed = true;
                node.type = blockpathtypes;
                node.costMalus = blockpathtypes.getMalus();
            }
        }
        return node;
    }
    
    protected AABB getBoundingBoxForPose(Pose p_20218_) 
    {
    	EntityDimensions entitydimensions = this.mob.getDimensions(p_20218_);
    	float f = entitydimensions.width / 2.0F;
        Vec3 vec3 = new Vec3(this.mob.getX() - (double)f, this.mob.getY(), this.mob.getZ() - (double)f);
        Vec3 vec31 = new Vec3(this.mob.getX() + (double)f, this.mob.getY() + (double)entitydimensions.height, this.mob.getZ() + (double)f);
        return new AABB(vec3, vec31);
    }
    
    @Nullable
    protected Node findAcceptedNode(BlockPos pos, int stepHeight, double floorLevel, Direction moveDir, BlockPathTypes originPathType)
    {
        return this.findAcceptedNode(pos.getX(), pos.getY(), pos.getZ(), stepHeight, floorLevel, moveDir, originPathType);
    }
    
    private boolean canReachWithoutCollision(Node p_77625_) 
    {
    	AABB aabb = this.mob.getBoundingBox();
    	Vec3 vec3 = new Vec3((double)p_77625_.x - this.mob.getX() + aabb.getXsize() / 2.0D, (double)p_77625_.y - this.mob.getY() + aabb.getYsize() / 2.0D, (double)p_77625_.z - this.mob.getZ() + aabb.getZsize() / 2.0D);
        int i = Mth.ceil(vec3.length() / aabb.getSize());
        vec3 = vec3.scale((double)(1.0F / (float)i));
        for(int j = 1; j <= i; ++j) 
        {
        	aabb = aabb.move(vec3);
        	if(this.hasCollisions(aabb)) 
        	{
        		return false;
        	}
        }
        return true;
    }
    
    private boolean hasCollisions(AABB p_77635_) 
    {
    	return this.collisionCache.computeIfAbsent(p_77635_, (p_192973_) -> 
    	{
    		return !this.level.noCollision(this.mob, p_77635_);
    	});
    }
    
    private Node getNodeAndUpdateCostToMax(int p_230620_, int p_230621_, int p_230622_, BlockPathTypes p_230623_, float p_230624_) 
    {
        Node node = this.getNode(p_230620_, p_230621_, p_230622_);
        node.type = p_230623_;
        node.costMalus = Math.max(node.costMalus, p_230624_);
        return node;
    }
    
    private Node getBlockedNode(int p_230628_, int p_230629_, int p_230630_)
    {
    	Node node = this.getNode(p_230628_, p_230629_, p_230630_);
    	node.type = BlockPathTypes.BLOCKED;
    	node.costMalus = -1.0F;
    	return node;
    }
    
    private static boolean doesBlockHavePartialCollision(BlockPathTypes p_230626_) 
    {
    	return p_230626_ == BlockPathTypes.FENCE || p_230626_ == BlockPathTypes.DOOR_WOOD_CLOSED || p_230626_ == BlockPathTypes.DOOR_IRON_CLOSED;
    }
    
    private double getMobJumpHeight()
    {
    	return Math.max(1.125D, (double)this.mob.getStepHeight());
    }
}
