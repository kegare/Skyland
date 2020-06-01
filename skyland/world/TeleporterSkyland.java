package skyland.world;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;
import skyland.stats.PortalCache;
import skyland.util.SkyUtils;

public class TeleporterSkyland implements ITeleporter
{
	public static final ResourceLocation KEY = new ResourceLocation("skyland", "teleporter");

	@Override
	public void placeEntity(World world, Entity entity, float rotationYaw)
	{
		if (attemptToLastPos(world, entity) || attemptRandomly(world, entity) || attemptToVoid(world, entity))
		{
			entity.motionX = 0.0D;
			entity.motionY = 0.0D;
			entity.motionZ = 0.0D;
		}
	}

	protected boolean attemptToLastPos(World world, Entity entity)
	{
		PortalCache cache = PortalCache.get(entity);
		DimensionType type = world.provider.getDimensionType();

		if (cache.hasLastPos(KEY, type))
		{
			BlockPos pos = cache.getLastPos(KEY, type);
			Material material  = world.getBlockState(pos.down()).getMaterial();

			if ((material.isSolid() || material == Material.WATER) && world.getBlockState(pos).getBlock().canSpawnInBlock() && world.getBlockState(pos.up()).getBlock().canSpawnInBlock())
			{
				entity.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D);

				return true;
			}

			cache.setLastPos(KEY, type, null);
		}

		return false;
	}

	protected boolean attemptRandomly(World world, Entity entity)
	{
		int originX = MathHelper.floor(entity.posX);
		int originZ = MathHelper.floor(entity.posZ);
		MutableBlockPos pos = new MutableBlockPos();

		for (int x = originX - 256; x < originX + 256; ++x)
		{
			for (int z = originZ - 256; z < originZ + 256; ++z)
			{
				for (int y = 100; y > 50; --y)
				{
					pos.setPos(x, y, z);

					if (world.isAirBlock(pos))
					{
						continue;
					}

					Material material = world.getBlockState(pos).getMaterial();

					if (material.isSolid() || material == Material.WATER)
					{
						entity.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 1.25D, pos.getZ() + 0.5D);

						return true;
					}
				}
			}
		}

		return false;
	}

	protected boolean attemptToVoid(World world, Entity entity)
	{
		if (!SkyUtils.isEntityInSkyland(entity))
		{
			return false;
		}

		BlockPos pos = new BlockPos(entity.posX, 0.0D, entity.posZ);
		BlockPos from = pos.add(-1, 0, -1);
		BlockPos to = pos.add(1, 0, 1);

		BlockPos.getAllInBoxMutable(from, to).forEach(blockPos -> world.setBlockState(blockPos, Blocks.SNOW.getDefaultState(), 2));

		entity.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 1.25D, pos.getZ() + 0.5D);

		return true;
	}
}