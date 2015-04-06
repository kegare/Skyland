/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import skyland.block.SkyBlocks;
import skyland.core.Skyland;

public class ItemSkyFeather extends Item
{
	public ItemSkyFeather()
	{
		this.setUnlocalizedName("skyFeather");
		this.setCreativeTab(Skyland.tabSkyland);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		BlockPos pos1 = pos.offset(side);

		if (SkyBlocks.sky_portal.func_176548_d(worldIn, pos1))
		{
			worldIn.playSoundEffect(pos1.getX() + 0.5D, pos1.getY() + 0.5D, pos1.getZ() + 0.5D, SkyBlocks.sky_portal.stepSound.getPlaceSound(), 1.0F, 2.0F);

			if (!playerIn.capabilities.isCreativeMode && --stack.stackSize <= 0)
			{
				playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, null);
			}

			return true;
		}

		return super.onItemUse(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
	{
		if (world.provider.getDimensionId() == 0)
		{
			BlockPos pos = player.getPosition();

			while (pos.getY() < world.getHeight() && world.isAirBlock(pos.up()))
			{
				pos = pos.up();
			}

			if (!world.isAirBlock(pos))
			{
				return itemstack;
			}

			if (!world.isRemote)
			{
				if (!player.capabilities.isCreativeMode && --itemstack.stackSize <= 0)
				{
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}

				player.getEntityData().setBoolean("Skyland:SkyJump", true);
			}
			else
			{
				Skyland.proxy.playSoundSkyJump();
			}

			player.addVelocity(0.0D, 15.0D, 0.0D);
		}

		return itemstack;
	}
}