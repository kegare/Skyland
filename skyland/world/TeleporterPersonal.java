package skyland.world;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import skyland.stats.IPortalCache;
import skyland.stats.PortalCache;
import skyland.util.SkyUtils;

public class TeleporterPersonal extends Teleporter
{
	public static final ResourceLocation KEY_PERSONAL = new ResourceLocation("skyland", "teleporter_personal");

	public TeleporterPersonal(WorldServer world)
	{
		super(world);
	}

	@Override
	public void placeInPortal(Entity entity, float rotationYaw)
	{
		if (entity instanceof EntityPlayerMP)
		{
			SkyUtils.setDimensionChange((EntityPlayerMP)entity);
		}

		entity.motionX = 0.0D;
		entity.motionY = 0.0D;
		entity.motionZ = 0.0D;

		if (attemptToLastPos(entity) || attemptCurrentLocation(entity) || attemptRandomly(entity))
		{
			return;
		}

		entity.setLocationAndAngles(0.5D, world.getHeight(0, 0) + 0.5D, 0.5D, rotationYaw, 0.0F);
	}

	protected boolean attemptToLastPos(Entity entity)
	{
		IPortalCache cache = PortalCache.get(entity);
		DimensionType type = world.provider.getDimensionType();

		if (cache.hasLastPos(KEY_PERSONAL, type))
		{
			BlockPos pos = cache.getLastPos(KEY_PERSONAL, type);

			if (!world.isAirBlock(pos) && world.isAirBlock(pos.up(2)))
			{
				entity.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, entity.rotationYaw, 0.0F);

				return true;
			}

			cache.setLastPos(KEY_PERSONAL, type, null);
		}

		return false;
	}

	protected boolean attemptCurrentLocation(Entity entity)
	{
		BlockPos pos = world.getTopSolidOrLiquidBlock(entity.getPosition());

		if (!world.isAirBlock(pos) && !world.getBlockState(pos).getMaterial().isLiquid())
		{
			entity.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, entity.rotationYaw, 0.0F);

			return true;
		}

		return false;
	}

	protected boolean attemptChunk(Entity entity, @Nullable Chunk chunk)
	{
		if (chunk == null)
		{
			return false;
		}

		for (int i = 0; i < 16; ++i)
		{
			for (int j = 0; j < 16; ++j)
			{
				int y = chunk.getHeightValue(i, j);
				IBlockState state = chunk.getBlockState(i, y, j);

				if (state.getMaterial() != Material.AIR && !state.getMaterial().isLiquid())
				{
					BlockPos pos = new BlockPos((chunk.x << 4) + i, y + 1, (chunk.z << 4) + j);

					entity.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, entity.rotationYaw, 0.0F);

					return true;
				}
			}
		}

		return false;
	}

	protected boolean attemptRandomly(Entity entity)
	{
		int originX = MathHelper.floor(entity.posX);
		int originZ = MathHelper.floor(entity.posZ);
		Chunk originChunk = world.getChunkFromChunkCoords(originX >> 4, originZ >> 4);

		if (attemptChunk(entity, originChunk))
		{
			return true;
		}

		for (int i = -8; i <= 8; ++i)
		{
			for (int j = -8; j <= 8; ++j)
			{
				if (attemptChunk(entity, world.getChunkFromChunkCoords(originChunk.x + i, originChunk.z + j)))
				{
					return true;
				}
			}
		}

		return false;
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