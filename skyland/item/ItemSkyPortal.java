/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import skyland.block.SkyBlocks;

public class ItemSkyPortal extends ItemBlock
{
	public ItemSkyPortal(Block block)
	{
		super(block);
		this.setMaxStackSize(1);
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			BlockPos pos1 = pos.offset(side);

			if (SkyBlocks.sky_portal.func_176548_d(world, pos1))
			{
				world.playSoundEffect(pos1.getX() + 0.5D, pos1.getY() + 0.5D, pos1.getZ() + 0.5D, SkyBlocks.sky_portal.stepSound.getPlaceSound(), 1.0F, 2.0F);

				if (!player.capabilities.isCreativeMode && --stack.stackSize <= 0)
				{
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return false;
	}
}