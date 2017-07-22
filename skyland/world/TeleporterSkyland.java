package skyland.world;

import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import skyland.block.SkyBlocks;
import skyland.core.Skyland;
import skyland.stats.IPortalCache;
import skyland.stats.PortalCache;
import skyland.util.SkyUtils;

public class TeleporterSkyland extends Teleporter
{
	public static final ResourceLocation KEY_PORTAL = new ResourceLocation("skyland", "teleporter_portal");

	private final boolean isInSkyland;

	public TeleporterSkyland(WorldServer world)
	{
		super(world);
		this.isInSkyland = world.provider.getDimensionType() == Skyland.DIM_SKYLAND;
	}

	@Override
	public void placeInPortal(Entity entity, float rotationYaw)
	{
		if (entity instanceof EntityPlayerMP)
		{
			SkyUtils.setDimensionChange((EntityPlayerMP)entity);
		}

		IPortalCache cache = PortalCache.get(entity);
		DimensionType type = world.provider.getDimensionType();
		double posX = entity.posX;
		double posY = entity.posY;
		double posZ = entity.posZ;
		boolean flag = false;

		if (cache.hasLastPos(KEY_PORTAL, type))
		{
			BlockPos pos = cache.getLastPos(KEY_PORTAL, type);

			entity.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, rotationYaw, 0.0F);

			if (placeInExistingPortal(entity, rotationYaw))
			{
				flag = true;
			}
			else
			{
				entity.setLocationAndAngles(posX, posY, posZ, rotationYaw, 0.0F);
			}
		}

		if (!flag)
		{
			if (isInSkyland)
			{
				entity.setLocationAndAngles(0.0D, 64.0D, 0.0D, rotationYaw, 0.0F);
			}

			if (!placeInExistingPortal(entity, rotationYaw))
			{
				makePortal(entity);

				placeInExistingPortal(entity, rotationYaw);
			}
		}

		if (entity instanceof EntityLivingBase)
		{
			((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 25, 0, false, false));
		}

		if (entity instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)entity;

			player.addExperienceLevel(0);
		}
	}

	@Override
	public boolean placeInExistingPortal(Entity entity, float rotationYaw)
	{
		double d0 = -1.0D;
		int i = MathHelper.floor(entity.posX);
		int j = MathHelper.floor(entity.posZ);
		boolean flag1 = true;
		BlockPos object = BlockPos.ORIGIN;
		long coord = ChunkPos.asLong(i, j);

		if (destinationCoordinateCache.containsKey(coord))
		{
			PortalPosition portalposition = destinationCoordinateCache.get(coord);
			d0 = 0.0D;
			object = portalposition;
			portalposition.lastUpdateTime = world.getTotalWorldTime();
			flag1 = false;
		}
		else
		{
			BlockPos pos = new BlockPos(entity);

			for (int x = -128; x <= 128; ++x)
			{
				BlockPos current;

				for (int z = -128; z <= 128; ++z)
				{
					for (BlockPos blockpos = pos.add(x, world.getActualHeight() - 1 - pos.getY(), z); blockpos.getY() >= 0; blockpos = current)
					{
						current = blockpos.down();

						if (world.getBlockState(blockpos).getBlock() == SkyBlocks.SKY_PORTAL)
						{
							while (world.getBlockState(current = blockpos.down()).getBlock() == SkyBlocks.SKY_PORTAL)
							{
								blockpos = current;
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
				destinationCoordinateCache.put(coord, new PortalPosition(object, world.getTotalWorldTime()));
			}

			double posX = object.getX() + 0.5D;
			double posY = object.getY() + 0.5D;
			double posZ = object.getZ() + 0.5D;
			EnumFacing face = null;

			if (world.getBlockState(object.west()).getBlock() == SkyBlocks.SKY_PORTAL)
			{
				face = EnumFacing.NORTH;
			}

			if (world.getBlockState(object.east()).getBlock() == SkyBlocks.SKY_PORTAL)
			{
				face = EnumFacing.SOUTH;
			}

			if (world.getBlockState(object.north()).getBlock() == SkyBlocks.SKY_PORTAL)
			{
				face = EnumFacing.EAST;
			}

			if (world.getBlockState(object.south()).getBlock() == SkyBlocks.SKY_PORTAL)
			{
				face = EnumFacing.WEST;
			}

			EnumFacing face0 = EnumFacing.getHorizontal(0);

			if (face != null)
			{
				EnumFacing face1 = face.rotateYCCW();
				BlockPos pos = object.offset(face);
				boolean flag2 = isNotAir(pos);
				boolean flag3 = isNotAir(pos.offset(face1));

				if (flag3 && flag2)
				{
					object = object.offset(face1);
					face = face.getOpposite();
					face1 = face1.getOpposite();
					BlockPos blockpos = object.offset(face);
					flag2 = isNotAir(blockpos);
					flag3 = isNotAir(blockpos.offset(face1));
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

				posX = object.getX() + 0.5D;
				posY = object.getY() + 0.5D;
				posZ = object.getZ() + 0.5D;
				posX += face1.getFrontOffsetX() * f6 + face.getFrontOffsetX() * f1;
				posZ += face1.getFrontOffsetZ() * f6 + face.getFrontOffsetZ() * f1;
				float f2 = 0.0F;
				float f3 = 0.0F;
				float f4 = 0.0F;
				float f5 = 0.0F;

				if (face == face0)
				{
					f2 = 1.0F;
					f3 = 1.0F;
				}
				else if (face == face0.getOpposite())
				{
					f2 = -1.0F;
					f3 = -1.0F;
				}
				else if (face == face0.rotateY())
				{
					f4 = 1.0F;
					f5 = -1.0F;
				}
				else
				{
					f4 = -1.0F;
					f5 = 1.0F;
				}

				double mx = entity.motionX;
				double mz = entity.motionZ;
				entity.motionX = mx * f2 + mz * f5;
				entity.motionZ = mx * f4 + mz * f3;
				entity.rotationYaw = rotationYaw - face0.getHorizontalIndex() * 90 + face.getHorizontalIndex() * 90;
			}
			else
			{
				entity.motionX = entity.motionY = entity.motionZ = 0.0D;
			}

			entity.setLocationAndAngles(posX, posY, posZ, rotationYaw, 0.0F);

			return true;
		}
		else return false;
	}

	protected boolean isNotAir(BlockPos pos)
	{
		return !world.isAirBlock(pos) || !world.isAirBlock(pos.up());
	}

	@Override
	public boolean makePortal(Entity entity)
	{
		byte b0 = 16;
		double d0 = -1.0D;
		double posX = entity.posX;
		double posY = entity.posY;
		double posZ = entity.posZ;

		if (isInSkyland)
		{
			posX = 0.0D;
			posZ = 0.0D;
		}

		int i = MathHelper.floor(posX);
		int j = MathHelper.floor(posY);
		int k = MathHelper.floor(posZ);
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
		MutableBlockPos blockpos = new MutableBlockPos();

		for (i2 = i - b0; i2 <= i + b0; ++i2)
		{
			d1 = i2 + 0.5D - posX;

			for (k2 = k - b0; k2 <= k + b0; ++k2)
			{
				d2 = k2 + 0.5D - posZ;

				outside: for (i3 = world.getActualHeight() - 1; i3 >= 0; --i3)
				{
					if (world.isAirBlock(blockpos.setPos(i2, i3, k2)))
					{
						while (i3 > 0 && world.isAirBlock(blockpos.setPos(i2, i3 - 1, k2)))
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

										blockpos.setPos(l4, i5, j5);

										if (k4 < 0 && !world.getBlockState(blockpos).getMaterial().isSolid() || k4 >= 0 && !world.isAirBlock(blockpos))
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

					outside: for (i3 = world.getActualHeight() - 1; i3 >= 0; --i3)
					{
						if (world.isAirBlock(blockpos.setPos(i2, i3, k2)))
						{
							while (i3 > 0 && world.isAirBlock(blockpos.setPos(i2, i3 - 1, k2)))
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

										blockpos.setPos(k4, l4, i5);

										if (j4 < 0 && !world.getBlockState(blockpos).getMaterial().isSolid() || j4 >= 0 && !world.isAirBlock(blockpos))
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
			i1 = MathHelper.clamp(i1, 70, world.getActualHeight() - 10);
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

						world.setBlockState(blockpos.setPos(l3, i4, j4), flag ? Blocks.QUARTZ_BLOCK.getDefaultState() : Blocks.AIR.getDefaultState());
					}
				}
			}
		}

		IBlockState state = SkyBlocks.SKY_PORTAL.getDefaultState().withProperty(BlockPortal.AXIS, l5 != 0 ? EnumFacing.Axis.X : EnumFacing.Axis.Z);

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

					world.setBlockState(blockpos.setPos(i4, j4, k4), flag1 ? Blocks.QUARTZ_BLOCK.getDefaultState() : state, 2);
				}
			}

			for (k3 = 0; k3 < 4; ++k3)
			{
				for (l3 = -1; l3 < 4; ++l3)
				{
					i4 = k5 + (k3 - 1) * l5;
					j4 = j2 + l3;
					k4 = k2 + (k3 - 1) * l2;

					world.notifyNeighborsOfStateChange(blockpos.setPos(i4, j4, k4), world.getBlockState(blockpos).getBlock(), false);
				}
			}
		}

		return true;
	}
}