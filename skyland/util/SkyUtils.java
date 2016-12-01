package skyland.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import skyland.core.Config;
import skyland.core.Skyland;
import skyland.world.TeleporterDummy;

public class SkyUtils
{
	private static ForkJoinPool pool;

	public static ForkJoinPool getPool()
	{
		if (pool == null || pool.isShutdown())
		{
			pool = new ForkJoinPool();
		}

		return pool;
	}

	public static ModContainer getModContainer()
	{
		ModContainer mod = Loader.instance().getIndexedModList().get(Skyland.MODID);

		if (mod == null)
		{
			mod = Loader.instance().activeModContainer();

			if (mod == null || mod.getModId() != Skyland.MODID)
			{
				return new DummyModContainer(Skyland.metadata);
			}
		}

		return mod;
	}

	public static boolean archiveDirZip(final File dir, final File dest)
	{
		final Path dirPath = dir.toPath();
		final String parent = dir.getName();
		Map<String, String> env = Maps.newHashMap();
		env.put("create", "true");
		URI uri = dest.toURI();

		try
		{
			uri = new URI("jar:" + uri.getScheme(), uri.getPath(), null);
		}
		catch (Exception e)
		{
			return false;
		}

		try (FileSystem zipfs = FileSystems.newFileSystem(uri, env))
		{
			Files.createDirectory(zipfs.getPath(parent));

			for (File file : dir.listFiles())
			{
				if (file.isDirectory())
				{
					Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>()
					{
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
						{
							Files.copy(file, zipfs.getPath(parent, dirPath.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);

							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
						{
							Files.createDirectory(zipfs.getPath(parent, dirPath.relativize(dir).toString()));

							return FileVisitResult.CONTINUE;
						}
					});
				}
				else
				{
					Files.copy(file.toPath(), zipfs.getPath(parent, file.getName()), StandardCopyOption.REPLACE_EXISTING);
				}
			}

			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return false;
		}
	}

	public static int compareWithNull(Object o1, Object o2)
	{
		return (o1 == null ? 1 : 0) - (o2 == null ? 1 : 0);
	}

	public static boolean transferPlayer(EntityPlayerMP player, int dim)
	{
		if (dim != player.dimension && DimensionManager.isDimensionRegistered(dim))
		{
			player.timeUntilPortal = player.getPortalCooldown();
			player.mcServer.getPlayerList().transferPlayerToDimension(player, dim, new TeleporterDummy(player.mcServer.worldServerForDimension(dim)));
			player.addExperienceLevel(0);

			return true;
		}

		return false;
	}

	public static void teleportPlayer(final EntityPlayerMP player, final int dim)
	{
		transferPlayer(player, dim);

		final WorldServer world = player.getServerWorld();

		world.addScheduledTask(new Runnable()
		{
			@Override
			public void run()
			{
				BlockPos originPos = player.getBedLocation(dim);
				boolean hasSpawn = false;

				if (originPos != null)
				{
					BlockPos blockpos = EntityPlayer.getBedSpawnLocation(world, originPos, true);

					if (blockpos != null)
					{
						originPos = blockpos;
						hasSpawn = true;
					}
					else
					{
						originPos = null;
						hasSpawn = false;
					}
				}

				if (originPos == null)
				{
					originPos = BlockPos.ORIGIN;
				}

				player.setLocationAndAngles(originPos.getX() + 0.5D, originPos.getY() + 0.5D, originPos.getZ() + 0.5D, player.rotationYaw, player.rotationPitch);

				if (hasSpawn)
				{
					while (!world.getCollisionBoxes(player, player.getEntityBoundingBox()).isEmpty() && player.posY < 256.0D)
					{
						player.setPosition(player.posX, player.posY + 1.0D, player.posZ);
					}

					player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);

					return;
				}
				else
				{
					int range = 64;
					BlockPos blockpos = null;

					for (BlockPos pos : BlockPos.getAllInBoxMutable(originPos.add(range, 0, range), originPos.add(-range, 0, -range)))
					{
						blockpos = world.getHeight(pos);

						if (!world.isAirBlock(blockpos) && !world.getBlockState(blockpos).getMaterial().isLiquid())
						{
							break;
						}

						blockpos = null;
					}

					if (blockpos != null && !world.isAirBlock(blockpos))
					{
						player.setLocationAndAngles(blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D, player.rotationYaw, player.rotationPitch);

						while (!world.getCollisionBoxes(player, player.getEntityBoundingBox()).isEmpty() && player.posY < 256.0D)
						{
							player.setPosition(player.posX, player.posY + 1.0D, player.posZ);
						}

						player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);

						return;
					}
				}

				int x = MathHelper.floor_double(player.posX);
				int y = MathHelper.floor_double(player.posY) - 1;
				int z = MathHelper.floor_double(player.posZ);
				int i = 1;
				int j = 0;
				MutableBlockPos pos = new MutableBlockPos();

				for (int i1 = -2; i1 <= 2; ++i1)
				{
					for (int j1 = -2; j1 <= 2; ++j1)
					{
						for (int k1 = -1; k1 < 3; ++k1)
						{
							boolean flag = k1 < 0;

							world.setBlockState(pos.setPos(x + j1 * i + i1 * j, y + k1, z + j1 * j - i1 * i), flag ? Blocks.GRASS.getDefaultState() : Blocks.AIR.getDefaultState());
						}
					}
				}

				player.connection.setPlayerLocation(x, y, z, player.rotationYaw, 0.0F);
				player.motionX = player.motionY = player.motionZ = 0.0D;
			}
		});
	}

	public static void teleportPlayer(final EntityPlayerMP player, final int dim, final double posX, final double posY, final double posZ)
	{
		teleportPlayer(player, dim, posX, posY, posZ, false);
	}

	public static void teleportPlayer(final EntityPlayerMP player, final int dim, final double posX, final double posY, final double posZ, final boolean force)
	{
		transferPlayer(player, dim);

		final WorldServer world = player.getServerWorld();

		world.addScheduledTask(new Runnable()
		{
			@Override
			public void run()
			{
				BlockPos originPos = new BlockPos(posX, posY, posZ);

				player.setLocationAndAngles(posX, posY, posZ, player.rotationYaw, player.rotationPitch);

				BlockPos blockpos = force ? BlockPos.ORIGIN : world.getHeight(originPos);

				if (force || !world.isAirBlock(blockpos))
				{
					while (!world.getCollisionBoxes(player, player.getEntityBoundingBox()).isEmpty() && player.posY < 256.0D)
					{
						player.setPosition(player.posX, player.posY + 1.0D, player.posZ);
					}

					player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);

					return;
				}
				else
				{
					int range = 64;

					for (BlockPos pos : BlockPos.getAllInBoxMutable(originPos.add(range, 0, range), originPos.add(-range, 0, -range)))
					{
						blockpos = world.getHeight(pos);

						if (!world.isAirBlock(blockpos) && !world.getBlockState(blockpos).getMaterial().isLiquid())
						{
							break;
						}

						blockpos = null;
					}

					if (blockpos != null && !world.isAirBlock(blockpos))
					{
						player.setLocationAndAngles(blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D, player.rotationYaw, player.rotationPitch);

						while (!world.getCollisionBoxes(player, player.getEntityBoundingBox()).isEmpty() && player.posY < 256.0D)
						{
							player.setPosition(player.posX, player.posY + 1.0D, player.posZ);
						}

						player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);

						return;
					}
				}

				int x = MathHelper.floor_double(player.posX);
				int y = MathHelper.floor_double(player.posY) - 1;
				int z = MathHelper.floor_double(player.posZ);
				int i = 1;
				int j = 0;
				MutableBlockPos pos = new MutableBlockPos();

				for (int i1 = -2; i1 <= 2; ++i1)
				{
					for (int j1 = -2; j1 <= 2; ++j1)
					{
						for (int k1 = -1; k1 < 3; ++k1)
						{
							boolean flag = k1 < 0;

							world.setBlockState(pos.setPos(x + j1 * i + i1 * j, y + k1, z + j1 * j - i1 * i), flag ? Blocks.GRASS.getDefaultState() : Blocks.AIR.getDefaultState());
						}
					}
				}

				player.connection.setPlayerLocation(x, y, z, player.rotationYaw, 0.0F);
				player.motionX = player.motionY = player.motionZ = 0.0D;
			}
		});
	}

	public static WorldInfo getWorldInfo(World world)
	{
		WorldInfo worldInfo = world.getWorldInfo();

		if (worldInfo instanceof DerivedWorldInfo)
		{
			worldInfo = ObfuscationReflectionHelper.getPrivateValue(DerivedWorldInfo.class, (DerivedWorldInfo)worldInfo, "theWorldInfo", "field_76115_a");
		}

		return worldInfo;
	}

	public static boolean isEntityInSkyland(Entity entity)
	{
		if (entity != null)
		{
			if (Skyland.SKYLAND == null)
			{
				return entity.dimension == Config.dimension;
			}

			if (entity.worldObj.getWorldInfo().getTerrainType() == Skyland.SKYLAND)
			{
				return entity.dimension == 0;
			}
		}

		return false;
	}

	public static void setDimensionChange(EntityPlayerMP player)
	{
		if (!player.capabilities.isCreativeMode)
		{
			ObfuscationReflectionHelper.setPrivateValue(EntityPlayerMP.class, player, true, "invulnerableDimensionChange", "field_184851_cj");
		}
	}
}