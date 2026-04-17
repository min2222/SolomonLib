package com.min01.solomonlib.multipart;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Quaternionf;

import com.min01.solomonlib.network.BuildMultipartPacket;
import com.min01.solomonlib.network.SolomonNetwork;
import com.min01.solomonlib.network.UpdatePartPacket;
import com.min01.solomonlib.util.SolomonClientUtil;
import com.mojang.math.Axis;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;	
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityPartBuilder<T extends Entity & IMultipart>
{
	public static final String ROOT = "root";
	public static final float SCALE = 0.0625F;
	public final T entity;
	public EntityBounds hitbox = EntityBounds.builder().add(ROOT).setBounds(0.0, 0.0, 0.0).build().getFactory().create();
	public final Map<String, Vec3> partOffset = new HashMap<>();
	public final Map<String, String> parts = new HashMap<>();
	public final Map<String, Part> partMap = new HashMap<>();
	public final Map<ModelPart, String> partNameCache = new HashMap<>();
	public final Map<String, String> cubeToModelPart = new HashMap<>();

	public EntityPartBuilder(T entity)
	{
		this.entity = entity;
	}
	
	public void tick(float partialTick)
	{
		if(this.entity.level.isClientSide)
		{
			Vec3 pos = this.entity.position();
	        double posX = pos.x;
	        double posY = pos.y;
	        double posZ = pos.z;

	        EntityPart root = this.hitbox.getPart(ROOT);
	        
	        Vec3 renderOffset = this.getOffset();
	        root.setOffX(posX + renderOffset.x);
	        root.setOffY(posY + renderOffset.y);
	        root.setOffZ(posZ + renderOffset.z);
	        
			this.clientTick(SolomonClientUtil.getModelFromEntity(this.entity));
			this.partTick();
	        
			if(this.entity instanceof LivingEntity living)
			{
		        QuaternionD rotation = this.defaultEntityRotation(living, partialTick);
		        root.rotate(rotation);
			}

	        if(this.isInWater() && !this.entity.isInWater())
	        {
	        	root.setPivotY(-this.getWaterOffset());
	        }

	        if(this.entity.tickCount == 2)
	        {
	    		this.hitbox = this.buildHitbox();
	        }
    		SolomonNetwork.sendToServer(new BuildMultipartPacket(this.entity.getUUID(), this.partOffset, this.parts, this.partMap, this.hitbox));
		}
		else
		{
			Vec3 pos = this.entity.position();
	        double posX = pos.x;
	        double posY = pos.y;
	        double posZ = pos.z;

	        EntityPart root = this.hitbox.getPart(ROOT);
	        
	        Vec3 renderOffset = this.getOffset();
	        root.setOffX(posX + renderOffset.x);
	        root.setOffY(posY + renderOffset.y);
	        root.setOffZ(posZ + renderOffset.z);
	        
			this.partTick();
	        
			if(this.entity instanceof LivingEntity living)
			{
		        QuaternionD rotation = this.defaultEntityRotation(living, partialTick);
		        root.rotate(rotation);
			}

	        if(this.isInWater() && !this.entity.isInWater())
	        {
	        	root.setPivotY(-this.getWaterOffset());
	        }
		}
	}
	
	public void partTick()
	{
	    for(Part part : this.partMap.values())
	    {
	        String name = part.name;
	        EntityPart entityPart = this.hitbox.getPart(name);
	        if(entityPart == null)
	        {
	        	continue;
	        }

	        if(this.cubeToModelPart.containsKey(name))
	        {
	            Vec3 offset = this.partOffset.getOrDefault(name, Vec3.ZERO);
	            entityPart.setX(offset.x);
	            entityPart.setY(offset.y);
	            entityPart.setZ(offset.z);
	            entityPart.setPivotX(offset.x);
	            entityPart.setPivotY(offset.y);
	            entityPart.setPivotZ(offset.z);
	            entityPart.setRotation(new QuaternionD(0, 0, 0, 1));
	            continue;
	        }

	        Vec3 partPos = new Vec3(-part.x, -part.y, part.z).scale(SCALE * this.getRenderScale());
	        Vec3 pivot = Vec3.ZERO;

	        if(this.partOffset.containsKey(name))
	        {
	            Vec3 offset = this.partOffset.get(name);
	            pivot = offset;
	            partPos = partPos.add(offset);
	        }

	        if(this.parts.containsKey(name))
	        {
	            String parentName = this.parts.get(name);
	            if(this.partOffset.containsKey(parentName))
	            {
	                partPos = partPos.subtract(this.partOffset.get(parentName));
	            }
	        }

	        entityPart.setX(partPos.x);
	        entityPart.setY(partPos.y);
	        entityPart.setZ(partPos.z);
	        entityPart.setPivotX(pivot.x);
	        entityPart.setPivotY(pivot.y);
	        entityPart.setPivotZ(pivot.z);

	        Quaternionf rotation = new Quaternionf().rotateZYX(part.zRot, -part.yRot, -part.xRot);
	        entityPart.setRotation(new QuaternionD(rotation.x, rotation.y, rotation.z, rotation.w));
	    }
	}
	
	record PartState(float x, float y, float z, float xRot, float yRot, float zRot)
	{
		public boolean changed(PartState state)
	    {
	    	return this.x != state.x || this.y != state.y || this.z != state.z || this.xRot != state.xRot || this.yRot != state.yRot || this.zRot != state.zRot;
	    }
	}

    @OnlyIn(Dist.CLIENT)
	public void clientTick(HierarchicalModel<T> model)
	{
    	Map<String, PartState> lastStates = new HashMap<>();
    	ModelPart root = model.root();
    	root.getAllParts().forEach(part -> 
    	{
    	    if(!this.partNameCache.containsKey(part))
    	    {
    	    	String name = this.getModelPartName(model.root(), part);
    	    	this.partNameCache.put(part, name);
    	    	return;
    	    }
    	    String name = this.partNameCache.get(part);
    	    Part p = this.partMap.get(name);
    	    if(p != null)
    	    {
    	        PartState current = new PartState(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot);
    	        PartState last = lastStates.get(name);
    	        if(last == null || current.changed(last))
    	        {
    	            p.tick(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot);
    	            SolomonNetwork.sendToServer(new UpdatePartPacket(this.entity.getUUID(), name, part.x, part.y, part.z, part.xRot, part.yRot, part.zRot));
    	            lastStates.put(name, current);
    	        }
    	    }
    	});
	}

    @OnlyIn(Dist.CLIENT)
	public EntityBounds buildHitbox()
	{
		HierarchicalModel<T> model = SolomonClientUtil.getModelFromEntity(this.entity);
        EntityBounds.EntityBoundsBuilder builder = EntityBounds.builder();
        return this.addPart(builder, model.root(), false, null).getFactory().create();
	}

    @OnlyIn(Dist.CLIENT)
    public EntityBounds.EntityBoundsBuilder addPart(EntityBounds.EntityBoundsBuilder builder, ModelPart part, boolean collide, @Nullable String parent)
    {
        HierarchicalModel<T> model = SolomonClientUtil.getModelFromEntity(this.entity);
        String name = this.getModelPartName(model.root(), part);
        boolean isCollide = this.entity.getCollidePart().contains(name) || collide;
        boolean isIgnore  = this.entity.getIgnorePart().contains(name);
        boolean flag = (this.entity.skipInvisiblePart() ? part.visible : true) && !isIgnore;

        EntityBounds.EntityPartInfoBuilder pivotInfo = builder.add(name);
        pivotInfo.setCollide(false);
        if (flag && parent != null)
        {
            pivotInfo.setParent(parent);
            this.parts.put(name, parent);
        }
        this.partOffset.put(name, Vec3.ZERO);
        pivotInfo.setBounds(new AABB(Vec3.ZERO, Vec3.ZERO));
        EntityBounds.EntityBoundsBuilder builder2 = pivotInfo.build();

        if(flag)
        {
            Part p = new Part(name, part.x, part.y, part.z, part.xRot, part.yRot, part.zRot);
            this.partMap.putIfAbsent(name, p);
        }
        AABB empty = new AABB(Vec3.ZERO, Vec3.ZERO);
        if(part.cubes.isEmpty())
        {
            String dummyName = name + "_empty";
            EntityBounds.EntityPartInfoBuilder dummyInfo = builder2.add(dummyName);
            dummyInfo.setCollide(false);
            dummyInfo.setParent(name);
            this.parts.put(dummyName, name);
            this.partOffset.put(dummyName, Vec3.ZERO);
            dummyInfo.setBounds(empty);
            builder2 = dummyInfo.build();
            this.partMap.putIfAbsent(dummyName, new Part(dummyName, 0, 0, 0, 0, 0, 0));
            this.cubeToModelPart.put(dummyName, name);
        }
        else
        {
            int cubeIdx = 0;
            for(ModelPart.Cube cube : part.cubes)
            {
                String cubeName = part.cubes.size() == 1 ? name + "_cube" : name + "_cube_" + cubeIdx;
                this.cubeToModelPart.put(cubeName, name);

                EntityBounds.EntityPartInfoBuilder cubeInfo = builder2.add(cubeName);
                cubeInfo.setCollide(isCollide);
                cubeInfo.setParent(name);
                this.parts.put(cubeName, name);

                double scale = SCALE * this.getRenderScale();
                Vec3 min = new Vec3(cube.minX, cube.minY, cube.minZ).scale(scale);
                Vec3 max = new Vec3(cube.maxX, cube.maxY, cube.maxZ).scale(scale);
                AABB cubeAABB = sanitizeAABB(new AABB(min, max));

                Vec3 center = cubeAABB.getCenter();
                Vec3 offset = center.multiply(-1.0, -1.0, 1.0);
                this.partOffset.put(cubeName, offset);

                double hw = cubeAABB.getXsize() / 2.0;
                double hh = cubeAABB.getYsize() / 2.0;
                double hd = cubeAABB.getZsize() / 2.0;
                cubeInfo.setBounds(new AABB(-hw, -hh, -hd, hw, hh, hd));
                builder2 = cubeInfo.build();

                this.partMap.putIfAbsent(cubeName, new Part(cubeName, 0, 0, 0, 0, 0, 0));
                cubeIdx++;
            }
        }

        for(ModelPart child : part.children.values())
        {
            this.addPart(builder2, child, isCollide, name);
        }
        return builder2;
    }

    private AABB sanitizeAABB(AABB box)
    {
        final double MIN = 0.005;
        double minX = box.minX, maxX = box.maxX;
        double minY = box.minY, maxY = box.maxY;
        double minZ = box.minZ, maxZ = box.maxZ;
        if(maxX - minX < MIN)
        { 
        	double c = (minX + maxX) / 2; minX = c - MIN/2; maxX = c + MIN/2;
        }
        if(maxY - minY < MIN) 
        { 
        	double c = (minY + maxY) / 2; minY = c - MIN/2; maxY = c + MIN/2; 
        }
        if(maxZ - minZ < MIN)
        { 
        	double c = (minZ + maxZ) / 2; minZ = c - MIN/2; maxZ = c + MIN/2; 
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @OnlyIn(Dist.CLIENT)
    public String getModelPartName(ModelPart root, ModelPart target) 
    {
        for(ModelPart part : root.getAllParts().toList())
        {
            for(Map.Entry<String, ModelPart> entry : part.children.entrySet()) 
            {
                if(entry.getValue() == target) 
                {
                    return entry.getKey();
                }
            }
        }
        return ROOT;
    }

    public Vec2 defaultHeadRotation(LivingEntity entity, float partialTick)
    {
        boolean shouldSit = entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit();
        float headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        float headRot = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float bodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float realHeadRot = headRot - bodyRot;
        if(shouldSit)
        {
            if(entity.getVehicle() instanceof LivingEntity vehicle)
            {
                bodyRot = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
                float delta = Mth.wrapDegrees(headRot - bodyRot);
                delta = Mth.clamp(delta, -85.0F, 85.0F);
                bodyRot = headRot - delta;
                if(delta * delta > 2500.0F) 
                {
                    bodyRot += delta * 0.2F;
                }

                realHeadRot = headRot - bodyRot;
            }
        }

        if(this.isEntityUpsideDown(entity)) 
        {
            headPitch *= -1.0F;
            realHeadRot *= -1.0F;
        }

        return new Vec2(headPitch, realHeadRot);
    }
    
    public float defaultBodyRotation(LivingEntity entity, float partialTick) 
    {
        boolean shouldSit = entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit();
        float bodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headRot = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        if(shouldSit) 
        {
            if(entity.getVehicle() instanceof LivingEntity vehicle) 
            {
                bodyRot = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
                float delta = Mth.wrapDegrees(headRot - bodyRot);
                delta = Mth.clamp(delta, -85.0F, 85.0F);
                bodyRot = headRot - delta;
                if(delta * delta > 2500.0F)
                {
                    bodyRot += delta * 0.2F;
                }
            }
        }

        return bodyRot;
    }
    
    public QuaternionD defaultEntityRotation(LivingEntity entity, float partialTick)
    {
    	Quaternionf rotation = new Quaternionf();
        float bodyRot = this.defaultBodyRotation(entity, partialTick);
        if(entity.isFullyFrozen())
        {
        	bodyRot = (float)((double)bodyRot + Math.cos((double)entity.tickCount * 3.25) * Math.PI * 0.4000000059604645);
        }

        if(!entity.hasPose(Pose.SLEEPING)) 
        {
            rotation.mul(Axis.YP.rotationDegrees(180.0F - bodyRot));
        }

        if(entity.deathTime > 0)
        {
            float progress = ((float)entity.deathTime + partialTick - 1.0F) / 20.0F * 1.6F;
            progress = Mth.sqrt(progress);
            if(progress > 1.0F) 
            {
                progress = 1.0F;
            }

            rotation.mul(Axis.ZP.rotationDegrees(progress * 90.0F));
        }
        else if(entity.isAutoSpinAttack()) 
        {
        	rotation.mul(Axis.XP.rotationDegrees(-90.0F - entity.getXRot()));
            rotation.mul(Axis.YP.rotationDegrees(((float)entity.tickCount + partialTick) * -75.0F));
        }
        else if(entity.hasPose(Pose.SLEEPING)) 
        {
            Direction direction = entity.getBedOrientation();
            float sleepRot = direction != null ? this.sleepDirectionToRotation(direction) : bodyRot;
            rotation.mul(Axis.YP.rotationDegrees(sleepRot));
            rotation.mul(Axis.ZP.rotationDegrees(90.0F));
            rotation.mul(Axis.YP.rotationDegrees(270.0F));
        }
        else if(this.isEntityUpsideDown(entity)) 
        {
        	rotation.mul(Axis.ZP.rotationDegrees(180.0F));
        }
        
        if(this.isInWater() && !this.entity.isInWater())
        {
        	rotation.mul(Axis.ZP.rotationDegrees(90.0F));
        }

        return new QuaternionD((double)rotation.x, (double)rotation.y, (double)rotation.z, (double)rotation.w);
    }
    
    public float sleepDirectionToRotation(Direction direction) 
    {
        switch(direction)
        {
           case SOUTH:
              return 90.0F;
           case WEST:
              return 0.0F;
           case NORTH:
              return 270.0F;
           case EAST:
              return 180.0F;
           default:
              return 0.0F;
        }
    }
    
    public boolean isEntityUpsideDown(LivingEntity entity) 
    {
        if(entity instanceof Player || entity.hasCustomName())
        {
        	String s = ChatFormatting.stripFormatting(entity.getName().getString());
        	if("Dinnerbone".equals(s) || "Grumm".equals(s)) 
        	{
        		return !(entity instanceof Player) || ((Player)entity).isModelPartShown(PlayerModelPart.CAPE);
        	}
        }
        return false;
    }
	
	public float getRenderScale()
	{
		return 1.0F;
	}
	
	public Vec3 getOffset()
	{
        float waterOffset = this.isInWater() && !this.entity.isInWater() ? -this.getWaterOffset() : 0.0F;
		return new Vec3(0.0F, 1.5F + waterOffset, 0.0F);
	}
	
	public boolean isInWater()
	{
		return false;
	}
	
	public float getWaterOffset()
	{
		return 0.5F;
	}
    
    public static class Part
    {
    	public String name;
    	
    	public float x;
    	public float y;
    	public float z;
    	
    	public float xRot;
    	public float yRot;
    	public float zRot;
    	
    	public Part(String name, float x, float y, float z, float xRot, float yRot, float zRot)
    	{
    		this.name = name;
    		this.x = x;
    		this.y = y;
    		this.z = z;
    		this.xRot = xRot;
    		this.yRot = yRot;
    		this.zRot = zRot;
    	}
    	
    	public void tick(float x, float y, float z, float xRot, float yRot, float zRot)
    	{
    		this.x = x;
    		this.y = y;
    		this.z = z;
    		this.xRot = xRot;
    		this.yRot = yRot;
    		this.zRot = zRot;
    	}
    	
    	public static Part read(FriendlyByteBuf buf)
    	{
    		return new Part(buf.readUtf(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    	}
    	
    	public static void write(FriendlyByteBuf buf, Part part)
    	{
    		buf.writeUtf(part.name);
    		buf.writeFloat(part.x);
    		buf.writeFloat(part.y);
    		buf.writeFloat(part.z);
    		buf.writeFloat(part.xRot);
    		buf.writeFloat(part.yRot);
    		buf.writeFloat(part.zRot);
    	}
    }
}
