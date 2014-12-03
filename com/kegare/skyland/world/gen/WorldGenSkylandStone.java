/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.skyland.world.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.pattern.BlockHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import com.google.common.base.Predicate;

public class WorldGenSkylandStone extends WorldGenerator
{
	private final Predicate target = BlockHelper.forBlock(Blocks.dirt);

	@Override
	public boolean generate(World world, Random random, BlockPos pos)
	{
		float f = random.nextFloat() * (float)Math.PI;
		int size = 60;
		double d0 = pos.getX() + 8 + MathHelper.sin(f) * size / 8.0F;
		double d1 = pos.getX() + 8 - MathHelper.sin(f) * size / 8.0F;
		double d2 = pos.getZ() + 8 + MathHelper.cos(f) * size / 8.0F;
		double d3 = pos.getZ() + 8 - MathHelper.cos(f) * size / 8.0F;
		double d4 = pos.getY() + random.nextInt(3) - 2;
		double d5 = pos.getY() + random.nextInt(3) - 2;

		for (int l = 0; l <= size; ++l)
		{
			double d6 = d0 + (d1 - d0) * l / size;
			double d7 = d4 + (d5 - d4) * l / size;
			double d8 = d2 + (d3 - d2) * l / size;
			double d9 = random.nextDouble() * size / 16.0D;
			double d10 = (MathHelper.sin(l * (float)Math.PI / size) + 1.0F) * d9 + 1.0D;
			double d11 = (MathHelper.sin(l * (float)Math.PI / size) + 1.0F) * d9 + 1.0D;

			for (int i = MathHelper.floor_double(d6 - d10 / 2.0D); i <= MathHelper.floor_double(d6 + d10 / 2.0D); ++i)
			{
				double xScale = (i + 0.5D - d6) / (d10 / 2.0D);

				if (xScale * xScale < 1.0D)
				{
					for (int j = MathHelper.floor_double(d7 - d11 / 2.0D); j <= MathHelper.floor_double(d7 + d11 / 2.0D); ++j)
					{
						double yScale = (j + 0.5D - d7) / (d11 / 2.0D);

						if (xScale * xScale + yScale * yScale < 1.0D)
						{
							for (int k = MathHelper.floor_double(d8 - d10 / 2.0D); k <= MathHelper.floor_double(d8 + d10 / 2.0D); ++k)
							{
								double zScale = (k + 0.5D - d8) / (d10 / 2.0D);

								if (xScale * xScale + yScale * yScale + zScale * zScale < 1.0D)
								{
									BlockPos pos1 = new BlockPos(i, j, k);

									if (world.getBlockState(pos1).getBlock().isReplaceableOreGen(world, pos1, target))
									{
										BlockPos pos2 = pos1.up();
										Block block = world.getBlockState(pos2).getBlock();

										if (block != Blocks.grass && !block.isWood(world, pos2))
										{
											world.setBlockState(pos1, Blocks.stone.getDefaultState(), 2);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return true;
	}
}