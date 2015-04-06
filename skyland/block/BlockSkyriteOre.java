/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.block;

import java.util.Random;

import net.minecraft.block.BlockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import skyland.core.Skyland;
import skyland.item.SkyItems;

public class BlockSkyriteOre extends BlockOre
{
	public BlockSkyriteOre()
	{
		super();
		this.setUnlocalizedName("oreSkyrite");
		this.setHardness(3.0F);
		this.setResistance(5.0F);
		this.setStepSound(soundTypePiston);
		this.setCreativeTab(Skyland.tabSkyland);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return SkyItems.skyrite;
	}

	@Override
	public int getExpDrop(IBlockAccess world, BlockPos pos, int fortune)
	{
		Random rand = world instanceof World ? ((World)world).rand : new Random();

		return MathHelper.getRandomIntegerInRange(rand, 5, 8);
	}
}