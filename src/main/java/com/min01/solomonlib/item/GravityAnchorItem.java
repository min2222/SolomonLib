package com.min01.solomonlib.item;

import java.util.List;

import com.min01.solomonlib.gravity.GravityZone;
import com.min01.solomonlib.gravity.GravityZoneManager;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

// based on AmethystGravity
public class GravityAnchorItem extends Item
{
    public final Direction direction;
    
    public GravityAnchorItem(Properties settings, Direction _direction)
    {
        super(settings);
        this.direction = _direction;
    }
    
    @Override
    public InteractionResult useOn(UseOnContext pContext) {
    	if(pContext.getLevel() instanceof ServerLevel serverLevel)
    	{
    		GravityZoneManager.setZone(serverLevel, pContext.getClickedPos(), new GravityZone(this.direction, 1.0D));
    	}
    	return super.useOn(pContext);
    }
    
    @Override
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        tooltip.add(Component.translatable("gravity_changer.gravity_anchor.tooltip.0").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gravity_changer.gravity_anchor.tooltip.1").withStyle(ChatFormatting.GRAY));
    }
}
