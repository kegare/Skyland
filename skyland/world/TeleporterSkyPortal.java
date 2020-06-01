package skyland.world;

import org.apache.commons.lang3.ObjectUtils;

import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;
import skyland.block.SkyBlocks;
import skyland.stats.PortalCache;

public class TeleporterSkyPortal implements ITeleporter
{
	public static final ResourceLocation KEY = new ResourceLocation("skyland", "teleporter_portal");

	@Override
	public void placeEntity(World world, Entity entity, float yaw)
	{
		PortalCache cache = PortalCache.get(entity);
		DimensionType type = world.provider.getDimensionType();

		if (cache.hasLastPos(KEY, type))
		{
			BlockPos pos = cache.getLastPos(KEY, type);

			if (placeInPortal(world, entity, yaw, 8, pos))
			{
				return;
			}
		}

		BlockPos pos = entity.getPosition();
		int range = 128;

		if (!placeInPortal(world, entity, yaw, range, pos))
		{
			placeInPortal(world, entity, yaw, range, makePortal(world, entity, range));
		}
	}

	public boolean placeInPortal(World world, Entity entity, float yaw, int checkRange, final BlockPos checkPos)
	{
		BlockPos pos = null;

		if (world.getBlockState(checkPos).getBlock() == SkyBlocks.SKY_PORTAL)
		{
			pos = checkPos;
		}
		else
		{
			int max = world.getActualHeight() - 1;
			MutableBlockPos findPos = new MutableBlockPos();

			outside: for (int range = 1; range <= checkRange; ++range)
			{
				for (int i = -range; i <= range; ++i)
				{
					for (int j = -range; j <= range; ++j)
					{
						if (Math.abs(i) < range && Math.abs(j) < range) continue;

						for (int y = checkPos.getY(); y < max; ++y)
						{
							if (world.getBlockState(findPos.setPos(checkPos.getX() + i, y, checkPos.getZ() + j)).getBlock() == SkyBlocks.SKY_PORTAL)
							{
								pos = findPos.toImmutable();

								break outside;
							}
						}

						for (int y = checkPos.getY(); y > 1; --y)
						{
							if (world.getBlockState(findPos.setPos(checkPos.getX() + i, y, checkPos.getZ() + j)).getBlock() == SkyBlocks.SKY_PORTAL)
							{
								pos = findPos.toImmutable();

								break outside;
							}
						}
					}
				}
			}

			if (pos == null)
			{
				pos = world.getSpawnPoint();
			}

			if (world.getBlockState(pos).getBlock() != SkyBlocks.SKY_PORTAL)
			{
				return false;
			}
		}

		PortalCache cache = PortalCache.get(entity);
		Vec3d portalVec = ObjectUtils.defaultIfNull(cache.getLastPortalVec(), Vec3d.ZERO);
		EnumFacing teleportDirection = ObjectUtils.defaultIfNull(cache.getTeleportDirection(), EnumFacing.NORTH);
		double posX = pos.getX() + 0.5D;
		double posZ = pos.getZ() + 0.5D;
		BlockPattern.PatternHelper pattern = SkyBlocks.SKY_PORTAL.createPatternHelper(world, pos);
		boolean flag = pattern.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
		double d1 = pattern.getForwards().getAxis() == EnumFacing.Axis.X ? (double)pattern.getFrontTopLeft().getZ() : (double)pattern.getFrontTopLeft().getX();
		double posY = pattern.getFrontTopLeft().getY() + 1 - portalVec.y * pattern.getHeight();

		if (flag)
		{
			++d1;
		}

		if (pattern.getForwards().getAxis() == EnumFacing.Axis.X)
		{
			posZ = d1 + (1.0D - portalVec.x) * pattern.getWidth() * pattern.getForwards().rotateY().getAxisDirection().getOffset();
		}
		else
		{
			posX = d1 + (1.0D - portalVec.x) * pattern.getWidth() * pattern.getForwards().rotateY().getAxisDirection().getOffset();
		}

		float f1 = 0.0F;
		float f2 = 0.0F;
		float f3 = 0.0F;
		float f4 = 0.0F;

		if (pattern.getForwards().getOpposite() == teleportDirection)
		{
			f1 = 1.0F;
			f2 = 1.0F;
		}
		else if (pattern.getForwards().getOpposite() == teleportDirection.getOpposite())
		{
			f1 = -1.0F;
			f2 = -1.0F;
		}
		else if (pattern.getForwards().getOpposite() == teleportDirection.rotateY())
		{
			f3 = 1.0F;
			f4 = -1.0F;
		}
		else
		{
			f3 = -1.0F;
			f4 = 1.0F;
		}

		double mx = entity.motionX;
		double mz = entity.motionZ;

		entity.motionX = mx * f1 + mz * f4;
		entity.motionZ = mx * f3 + mz * f2;
		entity.rotationYaw = yaw - teleportDirection.getOpposite().getHorizontalIndex() * 90 + pattern.getForwards().getHorizontalIndex() * 90;

		entity.setPositionAndUpdate(posX, posY, posZ);

		return true;
	}

	public BlockPos makePortal(World world, Entity entity, int findRange)
	{
		double portalDist = -1.0D;
		int max = world.getActualHeight() - 1;
		int x = MathHelper.floor(entity.posX);
		int y = MathHelper.floor(entity.posY);
		int z = MathHelper.floor(entity.posZ);
		int x1 = x;
		int y1 = y;
		int z1 = z;
		int i = 0;
		int j = world.rand.nextInt(4);
		MutableBlockPos pos = new MutableBlockPos();

		for (int range = 1; range <= findRange; ++range)
		{
			for (int ix = -range; ix <= range; ++ix)
			{
				for (int iz = -range; iz <= range; ++iz)
				{
					if (Math.abs(ix) < range && Math.abs(iz) < range) continue;

					int px = x + ix;
					int pz = z + iz;
					double xSize = px + 0.5D - entity.posX;
					double zSize = pz + 0.5D - entity.posZ;

					outside: for (int py = max; py > 1; --py)
					{
						if (world.isAirBlock(pos.setPos(px, py, pz)))
						{
							while (py > 0 && world.isAirBlock(pos.setPos(px, py - 1, pz)))
							{
								--py;
							}

							for (int k = j; k < j + 4; ++k)
							{
								int i1 = k % 2;
								int j1 = 1 - i1;

								if (k % 4 >= 2)
								{
									i1 = -i1;
									j1 = -j1;
								}

								for (int size1 = 0; size1 < 3; ++size1)
								{
									for (int size2 = 0; size2 < 4; ++size2)
									{
										for (int height = -1; height < 4; ++height)
										{
											int checkX = px + (size2 - 1) * i1 + size1 * j1;
											int checkY = py + height;
											int checkZ = pz + (size2 - 1) * j1 - size1 * i1;

											pos.setPos(checkX, checkY, checkZ);

											if (height < 0 && !world.getBlockState(pos).getMaterial().isSolid() || height >= 0 && !world.isAirBlock(pos))
											{
												continue outside;
											}
										}
									}
								}

								double ySize = py + 0.5D - entity.posY;
								double size = xSize * xSize + ySize * ySize + zSize * zSize;

								if (portalDist < 0.0D || size < portalDist)
								{
									portalDist = size;
									x1 = px;
									y1 = py;
									z1 = pz;
									i = k % 4;
								}
							}
						}
					}
				}
			}

			if (portalDist >= 0.0D)
			{
				break;
			}
		}

		if (portalDist < 0.0D)
		{
			for (int range = 1; range <= findRange; ++range)
			{
				for (int ix = -range; ix <= range; ++ix)
				{
					for (int iz = -range; iz <= range; ++iz)
					{
						if (Math.abs(ix) < range && Math.abs(iz) < range) continue;

						int px = x + ix;
						int pz = z + iz;
						double xSize = px + 0.5D - entity.posX;
						double zSize = pz + 0.5D - entity.posZ;

						outside: for (int py = max; py > 1; --py)
						{
							if (world.isAirBlock(pos.setPos(px, py, pz)))
							{
								while (py > 0 && world.isAirBlock(pos.setPos(px, py - 1, pz)))
								{
									--py;
								}

								for (int k = j; k < j + 2; ++k)
								{
									int i1 = k % 2;
									int j1 = 1 - i1;

									for (int width = 0; width < 4; ++width)
									{
										for (int height = -1; height < 4; ++height)
										{
											int px1 = px + (width - 1) * i1;
											int py1 = py + height;
											int pz1 = pz + (width - 1) * j1;

											pos.setPos(px1, py1, pz1);

											if (height < 0 && !world.getBlockState(pos).getMaterial().isSolid() || height >= 0 && !world.isAirBlock(pos))
											{
												continue outside;
											}
										}
									}

									double ySize = py + 0.5D - entity.posY;
									double size = xSize * xSize + ySize * ySize + zSize * zSize;

									if (portalDist < 0.0D || size < portalDist)
									{
										portalDist = size;
										x1 = px;
										y1 = py;
										z1 = pz;
										i = k % 2;
									}
								}
							}
						}
					}
				}

				if (portalDist >= 0.0D)
				{
					break;
				}
			}
		}

		int x2 = x1;
		int y2 = y1;
		int z2 = z1;
		int i1 = i % 2;
		int j1 = 1 - i1;

		if (i % 4 >= 2)
		{
			i1 = -i1;
			j1 = -j1;
		}

		if (portalDist < 0.0D)
		{
			y1 = MathHelper.clamp(y1, 1, max - 10);
			y2 = y1;

			for (int size1 = -1; size1 <= 1; ++size1)
			{
				for (int size2 = 1; size2 < 3; ++size2)
				{
					for (int height = -1; height < 3; ++height)
					{
						int blockX = x2 + (size2 - 1) * i1 + size1 * j1;
						int blockY = y2 + height;
						int blockZ = z2 + (size2 - 1) * j1 - size1 * i1;
						boolean isFloor = height < 0;

						world.setBlockState(pos.setPos(blockX, blockY, blockZ), isFloor ? Blocks.QUARTZ_BLOCK.getDefaultState() : Blocks.AIR.getDefaultState());
					}
				}
			}
		}

		IBlockState portalState = SkyBlocks.SKY_PORTAL.getDefaultState().withProperty(BlockPortal.AXIS, i1 != 0 ? EnumFacing.Axis.X : EnumFacing.Axis.Z);
		BlockPos portalPos = null;

		for (int width = 0; width < 4; ++width)
		{
			for (int height = -1; height < 4; ++height)
			{
				int blockX = x2 + (width - 1) * i1;
				int blockY = y2 + height;
				int blockZ = z2 + (width - 1) * j1;
				boolean isFrame = width == 0 || width == 3 || height == -1 || height == 3;

				world.setBlockState(pos.setPos(blockX, blockY, blockZ), isFrame ? Blocks.QUARTZ_BLOCK.getDefaultState() : portalState, 2);

				if (width == 1 && height == 0)
				{
					portalPos = new BlockPos(blockX + 0.5D, blockY + 0.5D, blockZ + 0.5D);
				}
			}
		}

		return portalPos;
	}
}