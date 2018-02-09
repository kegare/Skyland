package skyland.world;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import skyland.stats.IPortalCache;
import skyland.stats.PortalCache;
import skyland.util.SkyUtils;

public class TeleporterSkyland extends Teleporter
{
	public static final ResourceLocation KEY = new ResourceLocation("skyland", "teleporter");

	public TeleporterSkyland(WorldServer world)
	{
		super(world);
	}

	@Override
	public void placeInPortal(Entity entity, float rotationYaw)
	{
		if (attemptToLastPos(entity) || attemptRandomly(entity) || attemptToVoid(entity))
		{
			entity.motionX = 0.0D;
			entity.motionY = 0.0D;
			entity.motionZ = 0.0D;
		}
	}

	protected boolean attemptToLastPos(Entity entity)
	{
		IPortalCache cache = PortalCache.get(entity);
		DimensionType type = world.provider.getDimensionType();

		if (cache.hasLastPos(KEY, type))
		{
			BlockPos pos = cache.getLastPos(KEY, type);
			Material material  = world.getBlockState(pos.down()).getMaterial();

			if ((material.isSolid() || material == Material.WATER) && world.getBlockState(pos).getBlock().canSpawnInBlock() && world.getBlockState(pos.up()).getBlock().canSpawnInBlock())
			{
				entity.moveToBlockPosAndAngles(pos, entity.rotationYaw, entity.rotationPitch);

				return true;
			}

			cache.setLastPos(KEY, type, null);
		}

		return false;
	}

	protected boolean attemptRandomly(Entity entity)
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
						entity.moveToBlockPosAndAngles(pos.up(), entity.rotationYaw, entity.rotationPitch);

						return true;
					}
				}
			}
		}

		return false;
	}

	protected boolean attemptToVoid(Entity entity)
	{
		if (!SkyUtils.isEntityInSkyland(entity))
		{
			return false;
		}

		BlockPos pos = new BlockPos(entity.posX, 0.0D, entity.posZ);
		BlockPos from = pos.add(-1, 0, -1);
		BlockPos to = pos.add(1, 0, 1);

		BlockPos.getAllInBoxMutable(from, to).forEach(blockPos -> world.setBlockState(blockPos, Blocks.SNOW.getDefaultState(), 2));

		entity.moveToBlockPosAndAngles(pos.up(), entity.rotationYaw, entity.rotationPitch);

		return true;
	}

	@Override
	public boolean placeInExistingPortal(Entity entity, float rotationYaw)
	{
		return false;
	}

	@Override
	public boolean makePortal(Entity entity)
	{
		return false;
	}

	@Override
	public void removeStalePortalLocations(long worldTime) {}
}