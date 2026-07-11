package com.min01.solomonlib.multipart;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.joml.Matrix3d;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;

import com.min01.kaijuc.KaijuC;
import com.min01.solomonlib.network.AddOBBPacket;
import com.min01.solomonlib.network.SolomonNetwork;
import com.min01.solomonlib.network.UpdateOBBPacket;
import com.min01.solomonlib.util.SolomonUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityPartBuilder
{
	public final ObjectArrayList<OBB> obbs = new ObjectArrayList<>();
	public Predicate<String> collisionPredicate;
	public Predicate<String> ignorePredicate;
	public boolean isBuild;
	
	public void setIgnorePredicate(Predicate<String> predicate)
	{
		this.ignorePredicate = predicate;
	}
	
	public void setCollisionPredicate(Predicate<String> predicate)
	{
		this.collisionPredicate = predicate;
	}
	
	public List<OBB> getIntersecting(AABB other, Predicate<String> predicate)
	{
	    ObjectArrayList<OBB> obbs = new ObjectArrayList<>();
	    for(OBB obb : this.obbs)
	    {
	        if(!obb.enabled || !predicate.test(obb.path))
	        {
	            continue;
	        }
	        if(KaijuC.intersects(obb, other))
	        {
	            obbs.add(obb);
	        }
	    }
	    return obbs;
	}
	
	public Optional<OBB> clip(Vec3 from, Vec3 to, Predicate<String> predicate)
	{
	    OBB min = null;
	    double minT = -1.0;
	    for(OBB obb : this.obbs)
	    {
	        if(!obb.enabled || !predicate.test(obb.path))
	        {
	            continue;
	        }
	        double t = KaijuC.raycast(obb, from, to);
	        if(t < 0.0)
	        {
	            continue;
	        }
	        if(minT < 0.0 || t < minT)
	        {
	        	minT = t;
	            min = obb;
	        }
	    }
        return Optional.ofNullable(min);
	}
	
	public boolean isIntersecting(AABB aabb, String name)
	{
	    return !this.getIntersecting(aabb, p -> p.contains(name)).isEmpty();
	}
	
	public boolean clip(Entity entity, double dist, String name)
	{
        Vec3 pos = entity.getEyePosition(1.0F);
        Vec3 dir = entity.getViewVector(1.0F);
		return this.clip(pos, pos.add(dir.scale(dist)), p -> p.contains(name)).isPresent();
	}
	
	public void update(List<OBBData> list)
	{
		//update rotation, size, position of hitbox;
	    for(int i = 0; i < list.size() && i < this.obbs.size(); i++)
	    {
	        OBBData data = list.get(i);
	        OBB obb = this.obbs.get(i);
	        obb.update(data);
	    }
	}
	
	public COBB build(AABB aabb)
	{
		return new COBB(aabb, this.obbs);
	}
	
	@OnlyIn(Dist.CLIENT)
	public <T extends Entity & IMultipart> void send(HierarchicalModel<T> model, T entity, float partialTick, Consumer<PoseStack> rotationConsumer, Consumer<PoseStack> scaleConsumer)
	{
		if(!this.isBuild)
		{
			ObjectArrayList<OBBData> data = new ObjectArrayList<>();
			this.calculate(model.root(), entity, partialTick, rotationConsumer, scaleConsumer, t -> 
			{
				data.add(t);
				this.addOBB(t);
			});
			SolomonNetwork.sendToServer(new AddOBBPacket(entity.getUUID(), data));
			this.isBuild = true;
		}
		else
		{
			ObjectArrayList<OBBData> data = new ObjectArrayList<>();
			this.calculate(model.root(), entity, partialTick, rotationConsumer, scaleConsumer, t -> 
			{
				data.add(t);
			});
			this.update(data);
			SolomonNetwork.sendToServer(new UpdateOBBPacket(entity.getUUID(), data));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public <T extends Entity & IMultipart> void calculate(ModelPart part, T entity, float partialTick, Consumer<PoseStack> rotationConsumer, Consumer<PoseStack> scaleConsumer, Consumer<OBBData> consumer)
	{
		PoseStack poseStack = new PoseStack();
		poseStack.translate(entity.getX(), entity.getY(), entity.getZ());
		rotationConsumer.accept(poseStack);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		scaleConsumer.accept(poseStack);
		poseStack.translate(0.0F, -1.501F, 0.0F);
		this.visit(part, poseStack, "", consumer);
	}
		   
	@OnlyIn(Dist.CLIENT)
	public void visit(ModelPart part, PoseStack pPoseStack, String pPath, Consumer<OBBData> consumer) 
	{
		if(!part.cubes.isEmpty() || !part.children.isEmpty())
		{
			pPoseStack.pushPose();
			part.translateAndRotate(pPoseStack);
			PoseStack.Pose pose = pPoseStack.last();
			for(int i = 0; i < part.cubes.size(); ++i)
			{
				ModelPart.Cube cube = part.cubes.get(i);
				Matrix4f matrix = pose.pose();
				
				float scaleFactor = 1.0F / 32.0F;
				float cx = (cube.minX + cube.maxX) * scaleFactor;
				float cy = (cube.minY + cube.maxY) * scaleFactor;
				float cz = (cube.minZ + cube.maxZ) * scaleFactor;

				float hx = (cube.maxX - cube.minX) * scaleFactor;
				float hy = (cube.maxY - cube.minY) * scaleFactor;
				float hz = (cube.maxZ - cube.minZ) * scaleFactor;

				Vector3f localCenter = new Vector3f(cx, cy, cz);
				Vector3f worldCenter = matrix.transformPosition(localCenter);
				Vec3 center = new Vec3(worldCenter.x, worldCenter.y, worldCenter.z);

				Vector3f col0 = matrix.getColumn(0, new Vector3f());
				Vector3f col1 = matrix.getColumn(1, new Vector3f());
				Vector3f col2 = matrix.getColumn(2, new Vector3f());
				
				float sx = col0.length();
				float sy = col1.length();
				float sz = col2.length();
				
				Vec3 halfExtents = new Vec3(Math.abs(hx * sx), Math.abs(hy * sy), Math.abs(hz * sz));
				if(sx * sy * sz < 0)
				{
				    col2.negate();
				    sz = -sz;
				}
				
				col0.div(sx);
				col1.div(sy);
				col2.div(sz);
				Matrix3d rotMat = new Matrix3d();
				rotMat.setColumn(0, new Vector3d(col0.x, col0.y, col0.z));
				rotMat.setColumn(1, new Vector3d(col1.x, col1.y, col1.z));
				rotMat.setColumn(2, new Vector3d(col2.x, col2.y, col2.z));
				
				Quaterniond rotation = rotMat.getNormalizedRotation(new Quaterniond());
				
				consumer.accept(new OBBData(center, halfExtents, rotation, part.visible || !this.ignore(pPath), this.collide(pPath), pPath));
			}
			String s = pPath + "/";
			part.children.forEach((name, child) ->
			{
				this.visit(child, pPoseStack, s + name, consumer);
			});
			pPoseStack.popPose();
		}
	}
	
	public boolean ignore(String pPath)
	{
		if(this.ignorePredicate == null)
		{
			return true;
		}
		return this.ignorePredicate.test(pPath);
	}
	
	public boolean collide(String pPath)
	{
		return this.collisionPredicate != null && this.collisionPredicate.test(pPath);
	}
	
    public float defaultBodyRotation(LivingEntity entity, float partialTick) 
    {
        boolean shouldSit = entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit();
        float bodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headRot = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        if(shouldSit && entity.getVehicle() instanceof LivingEntity vehicle) 
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
        return bodyRot;
    }
	
	public void addOBB(OBBData data)
	{
		this.obbs.add(new OBB(data));
	}
	
	public static record OBBData(Vec3 center, Vec3 halfExtents, Quaterniond rotation, boolean enabled, boolean collide, String path)
	{
		public static OBBData read(FriendlyByteBuf buf)
		{
			return new OBBData(SolomonUtil.readVec3(buf), SolomonUtil.readVec3(buf), new Quaterniond(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readBoolean(), buf.readBoolean(), buf.readUtf());
		}
		
		public void write(FriendlyByteBuf buf)
		{
			SolomonUtil.writeVec3(buf, this.center);
			SolomonUtil.writeVec3(buf, this.halfExtents);
			buf.writeDouble(this.rotation.x);
			buf.writeDouble(this.rotation.y);
			buf.writeDouble(this.rotation.z);
			buf.writeDouble(this.rotation.w);
			buf.writeBoolean(this.enabled);
			buf.writeBoolean(this.collide);
			buf.writeUtf(this.path);
		}
	}
}
