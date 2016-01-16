/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.world;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import skyland.api.SkylandAPI;
import skyland.block.SkyBlocks;

public class TeleporterSkyland extends Teleporter
{
	private final WorldServer worldObj;
	private final Random random;

	private final LongHashMap coordCache = new LongHashMap();
	private final Set<Long> coordKeys = Sets.newHashSet();

	public TeleporterSkyland(WorldServer worldServer)
	{
		super(worldServer);
		this.worldObj = worldServer;
		this.worldObj.customTeleporters.add(this);
		this.random = new Random(worldServer.getSeed());
	}

	@Override
	public void placeInPortal(Entity entity, float rotationYaw)
	{
		if (!placeInExistingPortal(entity, rotationYaw))
		{
			if (SkylandAPI.isEntityInSkyland(entity))
			{
				if (!placeInExistingPortal(entity, rotationYaw, true))
				{
					makePortal(entity);

					placeInExistingPortal(entity, rotationYaw, true);
				}
			}
			else
			{
				makePortal(entity);

				placeInExistingPortal(entity, rotationYaw);
			}
		}

		if (entity instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)entity;

			player.addExperienceLevel(0);
			player.addPotionEffect(new PotionEffect(Potion.blindness.getId(), 25, 0, false, false));
		}
	}

	@Override
	public boolean placeInExistingPortal(Entity entity, float par2)
	{
		return placeInExistingPortal(entity, par2, false);
	}

	public boolean placeInExistingPortal(Entity entity, float par2, boolean flag)
	{
		double d0 = -1.0D;
		int i = MathHelper.floor_double(entity.posX);
		int j = MathHelper.floor_double(entity.posZ);
		boolean flag1 = true;
		Object object = BlockPos.ORIGIN;
		long k = ChunkCoordIntPair.chunkXZ2Int(i, j);

		if (coordCache.containsItem(k))
		{
			Teleporter.PortalPosition portalposition = (PortalPosition)coordCache.getValueByKey(k);
			d0 = 0.0D;
			object = portalposition;
			portalposition.lastUpdateTime = worldObj.getTotalWorldTime();
			flag1 = false;
		}
		else
		{
			BlockPos pos = new BlockPos(entity);

			if (flag)
			{
				pos = BlockPos.ORIGIN;
			}

			for (int l = -128; l <= 128; ++l)
			{
				BlockPos blockpos1;

				for (int i1 = -128; i1 <= 128; ++i1)
				{
					for (BlockPos blockpos = pos.add(l, worldObj.getActualHeight() - 1 - pos.getY(), i1); blockpos.getY() >= 0; blockpos = blockpos1)
					{
						blockpos1 = blockpos.down();

						if (worldObj.getBlockState(blockpos).getBlock() == SkyBlocks.sky_portal)
						{
							while (worldObj.getBlockState(blockpos1 = blockpos.down()).getBlock() == SkyBlocks.sky_portal)
							{
								blockpos = blockpos1;
							}

							double d1 = blockpos.distanceSq(pos);

							if (d0 < 0.0D || d1 < d0)
							{
								d0 = d1;
								object = blockpos;
							}
						}
					}
				}
			}
		}

		if (d0 >= 0.0D)
		{
			if (flag1)
			{
				coordCache.add(k, new Teleporter.PortalPosition((BlockPos)object, worldObj.getTotalWorldTime()));
				coordKeys.add(Long.valueOf(k));
			}

			double d4 = ((BlockPos)object).getX() + 0.5D;
			double d5 = ((BlockPos)object).getY() + 0.5D;
			double d6 = ((BlockPos)object).getZ() + 0.5D;
			EnumFacing enumfacing = null;

			if (worldObj.getBlockState(((BlockPos)object).west()).getBlock() == SkyBlocks.sky_portal)
			{
				enumfacing = EnumFacing.NORTH;
			}

			if (worldObj.getBlockState(((BlockPos)object).east()).getBlock() == SkyBlocks.sky_portal)
			{
				enumfacing = EnumFacing.SOUTH;
			}

			if (worldObj.getBlockState(((BlockPos)object).north()).getBlock() == SkyBlocks.sky_portal)
			{
				enumfacing = EnumFacing.EAST;
			}

			if (worldObj.getBlockState(((BlockPos)object).south()).getBlock() == SkyBlocks.sky_portal)
			{
				enumfacing = EnumFacing.WEST;
			}

			EnumFacing enumfacing1 = EnumFacing.getHorizontal(0);

			if (enumfacing != null)
			{
				EnumFacing enumfacing2 = enumfacing.rotateYCCW();
				BlockPos pos = ((BlockPos)object).offset(enumfacing);
				boolean flag2 = func_180265_a(pos);
				boolean flag3 = func_180265_a(pos.offset(enumfacing2));

				if (flag3 && flag2)
				{
					object = ((BlockPos)object).offset(enumfacing2);
					enumfacing = enumfacing.getOpposite();
					enumfacing2 = enumfacing2.getOpposite();
					BlockPos blockpos3 = ((BlockPos)object).offset(enumfacing);
					flag2 = func_180265_a(blockpos3);
					flag3 = func_180265_a(blockpos3.offset(enumfacing2));
				}

				float f6 = 0.5F;
				float f1 = 0.5F;

				if (!flag3 && flag2)
				{
					f6 = 1.0F;
				}
				else if (flag3 && !flag2)
				{
					f6 = 0.0F;
				}
				else if (flag3)
				{
					f1 = 0.0F;
				}

				d4 = ((BlockPos)object).getX() + 0.5D;
				d5 = ((BlockPos)object).getY() + 0.5D;
				d6 = ((BlockPos)object).getZ() + 0.5D;
				d4 += enumfacing2.getFrontOffsetX() * f6 + enumfacing.getFrontOffsetX() * f1;
				d6 += enumfacing2.getFrontOffsetZ() * f6 + enumfacing.getFrontOffsetZ() * f1;
				float f2 = 0.0F;
				float f3 = 0.0F;
				float f4 = 0.0F;
				float f5 = 0.0F;

				if (enumfacing == enumfacing1)
				{
					f2 = 1.0F;
					f3 = 1.0F;
				}
				else if (enumfacing == enumfacing1.getOpposite())
				{
					f2 = -1.0F;
					f3 = -1.0F;
				}
				else if (enumfacing == enumfacing1.rotateY())
				{
					f4 = 1.0F;
					f5 = -1.0F;
				}
				else
				{
					f4 = -1.0F;
					f5 = 1.0F;
				}

				double d2 = entity.motionX;
				double d3 = entity.motionZ;
				entity.motionX = d2 * f2 + d3 * f5;
				entity.motionZ = d2 * f4 + d3 * f3;
				entity.rotationYaw = par2 - enumfacing1.getHorizontalIndex() * 90 + enumfacing.getHorizontalIndex() * 90;
			}
			else
			{
				entity.motionX = entity.motionY = entity.motionZ = 0.0D;
			}

			entity.setLocationAndAngles(d4, d5, d6, entity.rotationYaw, entity.rotationPitch);

			return true;
		}
		else return false;
	}

	private boolean func_180265_a(BlockPos pos)
	{
		return !worldObj.isAirBlock(pos) || !worldObj.isAirBlock(pos.up());
	}

	@Override
	public boolean makePortal(Entity entity)
	{
		byte b0 = 16;
		double d0 = -1.0D;
		double posX = entity.posX;
		double posY = entity.posY;
		double posZ = entity.posZ;

		if (SkylandAPI.isEntityInSkyland(entity))
		{
			posX = 0.0D;
			posZ = 0.0D;
		}

		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(posY);
		int k = MathHelper.floor_double(posZ);
		int l = i;
		int i1 = j;
		int j1 = k;
		int k1 = 0;
		int l1 = random.nextInt(4);
		int i2;
		double d1;
		int k2;
		double d2;
		int i3;
		int j3;
		int k3;
		int l3;
		int i4;
		int j4;
		int k4;
		int l4;
		int i5;
		double d3;
		double d4;

		for (i2 = i - b0; i2 <= i + b0; ++i2)
		{
			d1 = i2 + 0.5D - posX;

			for (k2 = k - b0; k2 <= k + b0; ++k2)
			{
				d2 = k2 + 0.5D - posZ;

				outside: for (i3 = worldObj.getActualHeight() - 1; i3 >= 0; --i3)
				{
					if (worldObj.isAirBlock(new BlockPos(i2, i3, k2)))
					{
						while (i3 > 0 && worldObj.isAirBlock(new BlockPos(i2, i3 - 1, k2)))
						{
							--i3;
						}

						for (j3 = l1; j3 < l1 + 4; ++j3)
						{
							k3 = j3 % 2;
							l3 = 1 - k3;

							if (j3 % 4 >= 2)
							{
								k3 = -k3;
								l3 = -l3;
							}

							for (i4 = 0; i4 < 3; ++i4)
							{
								for (j4 = 0; j4 < 4; ++j4)
								{
									for (k4 = -1; k4 < 4; ++k4)
									{
										l4 = i2 + (j4 - 1) * k3 + i4 * l3;
										i5 = i3 + k4;
										int j5 = k2 + (j4 - 1) * l3 - i4 * k3;

										if (k4 < 0 && !worldObj.getBlockState(new BlockPos(l4, i5, j5)).getBlock().getMaterial().isSolid() || k4 >= 0 && !worldObj.isAirBlock(new BlockPos(l4, i5, j5)))
										{
											continue outside;
										}
									}
								}
							}

							d3 = i3 + 0.5D - posY;
							d4 = d1 * d1 + d3 * d3 + d2 * d2;

							if (d0 < 0.0D || d4 < d0)
							{
								d0 = d4;
								l = i2;
								i1 = i3;
								j1 = k2;
								k1 = j3 % 4;
							}
						}
					}
				}
			}
		}

		if (d0 < 0.0D)
		{
			for (i2 = i - b0; i2 <= i + b0; ++i2)
			{
				d1 = i2 + 0.5D - posX;

				for (k2 = k - b0; k2 <= k + b0; ++k2)
				{
					d2 = k2 + 0.5D - posZ;

					outside: for (i3 = worldObj.getActualHeight() - 1; i3 >= 0; --i3)
					{
						if (worldObj.isAirBlock(new BlockPos(i2, i3, k2)))
						{
							while (i3 > 0 && worldObj.isAirBlock(new BlockPos(i2, i3 - 1, k2)))
							{
								--i3;
							}

							for (j3 = l1; j3 < l1 + 2; ++j3)
							{
								k3 = j3 % 2;
								l3 = 1 - k3;

								for (i4 = 0; i4 < 4; ++i4)
								{
									for (j4 = -1; j4 < 4; ++j4)
									{
										k4 = i2 + (i4 - 1) * k3;
										l4 = i3 + j4;
										i5 = k2 + (i4 - 1) * l3;

										if (j4 < 0 && !worldObj.getBlockState(new BlockPos(k4, l4, i5)).getBlock().getMaterial().isSolid() || j4 >= 0 && !worldObj.isAirBlock(new BlockPos(k4, l4, i5)))
										{
											continue outside;
										}
									}
								}

								d3 = i3 + 0.5D - posY;
								d4 = d1 * d1 + d3 * d3 + d2 * d2;

								if (d0 < 0.0D || d4 < d0)
								{
									d0 = d4;
									l = i2;
									i1 = i3;
									j1 = k2;
									k1 = j3 % 2;
								}
							}
						}
					}
				}
			}
		}

		int k5 = l;
		int j2 = i1;
		k2 = j1;
		int l5 = k1 % 2;
		int l2 = 1 - l5;

		if (k1 % 4 >= 2)
		{
			l5 = -l5;
			l2 = -l2;
		}

		if (d0 < 0.0D)
		{
			i1 = MathHelper.clamp_int(i1, 70, worldObj.getActualHeight() - 10);
			j2 = i1;

			for (i3 = -1; i3 <= 1; ++i3)
			{
				for (j3 = 1; j3 < 3; ++j3)
				{
					for (k3 = -1; k3 < 3; ++k3)
					{
						l3 = k5 + (j3 - 1) * l5 + i3 * l2;
						i4 = j2 + k3;
						j4 = k2 + (j3 - 1) * l2 - i3 * l5;
						boolean flag = k3 < 0;

						worldObj.setBlockState(new BlockPos(l3, i4, j4), flag ? Blocks.quartz_block.getDefaultState() : Blocks.air.getDefaultState());
					}
				}
			}
		}

		IBlockState blockstate = SkyBlocks.sky_portal.getDefaultState().withProperty(BlockPortal.AXIS, l5 != 0 ? EnumFacing.Axis.X : EnumFacing.Axis.Z);

		for (j3 = 0; j3 < 4; ++j3)
		{
			for (k3 = 0; k3 < 4; ++k3)
			{
				for (l3 = -1; l3 < 4; ++l3)
				{
					i4 = k5 + (k3 - 1) * l5;
					j4 = j2 + l3;
					k4 = k2 + (k3 - 1) * l2;
					boolean flag1 = k3 == 0 || k3 == 3 || l3 == -1 || l3 == 3;

					worldObj.setBlockState(new BlockPos(i4, j4, k4), flag1 ? Blocks.quartz_block.getDefaultState() : blockstate, 2);
				}
			}

			for (k3 = 0; k3 < 4; ++k3)
			{
				for (l3 = -1; l3 < 4; ++l3)
				{
					i4 = k5 + (k3 - 1) * l5;
					j4 = j2 + l3;
					k4 = k2 + (k3 - 1) * l2;

					worldObj.notifyNeighborsOfStateChange(new BlockPos(i4, j4, k4), worldObj.getBlockState(new BlockPos(i4, j4, k4)).getBlock());
				}
			}
		}

		return true;
	}

	@Override
	public void removeStalePortalLocations(long time)
	{
		if (time % 100L == 0L)
		{
			Iterator<Long> iterator = coordKeys.iterator();
			long var1 = time - 600L;

			while (iterator.hasNext())
			{
				long chunkSeed = iterator.next();
				PortalPosition portal = (PortalPosition)coordCache.getValueByKey(chunkSeed);

				if (portal == null || portal.lastUpdateTime < var1)
				{
					iterator.remove();
					coordCache.remove(chunkSeed);
				}
			}
		}
	}
}