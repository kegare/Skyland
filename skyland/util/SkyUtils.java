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

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;
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

	public static void registerOreDict(Item item, String... names)
	{
		for (String name : names)
		{
			OreDictionary.registerOre(name, item);
		}
	}

	public static void registerOreDict(Block block, String... names)
	{
		for (String name : names)
		{
			OreDictionary.registerOre(name, block);
		}
	}

	public static void registerOreDict(ItemStack item, String... names)
	{
		for (String name : names)
		{
			OreDictionary.registerOre(name, item);
		}
	}

	public static void setPlayerLocation(EntityPlayerMP player, double posX, double posY, double posZ)
	{
		setPlayerLocation(player, posX, posY, posZ, player.rotationYaw, player.rotationPitch);
	}

	public static void setPlayerLocation(EntityPlayerMP player, double posX, double posY, double posZ, float yaw, float pitch)
	{
		player.dismountRidingEntity();
		player.playerNetServerHandler.setPlayerLocation(posX, posY, posZ, yaw, pitch);
	}

	public static boolean transferPlayer(EntityPlayerMP player, int dim)
	{
		if (dim != player.dimension)
		{
			if (!DimensionManager.isDimensionRegistered(dim))
			{
				return false;
			}

			player.isDead = false;
			player.forceSpawn = true;
			player.timeUntilPortal = player.getPortalCooldown();
			player.mcServer.getPlayerList().transferPlayerToDimension(player, dim, new TeleporterDummy(player.mcServer.worldServerForDimension(dim)));
			player.addExperienceLevel(0);

			return true;
		}

		return false;
	}

	public static boolean teleportPlayer(EntityPlayerMP player, int dim)
	{
		transferPlayer(player, dim);

		WorldServer world = player.getServerForPlayer();
		BlockPos pos = null;
		boolean flag = false;

		if (player.getBedLocation(dim) != null)
		{
			pos = EntityPlayer.getBedSpawnLocation(world, player.getBedLocation(dim), true);
			flag = true;
		}

		if (pos == null)
		{
			pos = BlockPos.ORIGIN.up(64);
			flag = false;
		}

		if (flag && world.isAirBlock(pos) && world.isAirBlock(pos.up()))
		{
			do
			{
				pos = pos.down();
			}
			while (pos.getY() > 0 && world.isAirBlock(pos.down()));

			BlockPos pos2 = pos;
			pos = pos.up();

			if (!world.isAirBlock(pos2) && !world.getBlockState(pos2).getMaterial().isLiquid())
			{
				setPlayerLocation(player, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);

				return true;
			}
		}
		else
		{
			int range = 32;

			for (int x = pos.getX() - range; x < pos.getX() + range; ++x)
			{
				for (int z = pos.getZ() - range; z < pos.getZ() + range; ++z)
				{
					for (int y = world.getActualHeight(); y > 0; --y)
					{
						BlockPos pos2 = new BlockPos(x, y, z);

						if (world.isAirBlock(pos2) && world.isAirBlock(pos2.up()))
						{
							do
							{
								pos2 = pos2.down();
							}
							while (pos2.getY() > 0 && world.isAirBlock(pos2.down()));

							BlockPos pos3 = pos2;
							pos2 = pos2.up();

							if (!world.isAirBlock(pos3) && !world.getBlockState(pos3).getMaterial().isLiquid())
							{
								setPlayerLocation(player, pos2.getX() + 0.5D, pos2.getY() + 0.5D, pos2.getZ() + 0.5D);

								return true;
							}
						}
					}
				}
			}

			pos = BlockPos.ORIGIN.up(64);
			setPlayerLocation(player, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
			world.setBlockToAir(pos);
			world.setBlockToAir(pos.up());
			world.setBlockState(pos, Blocks.dirt.getDefaultState());
		}

		return false;
	}

	public static boolean teleportPlayer(EntityPlayerMP player, int dim, double posX, double posY, double posZ, float yaw, float pitch, boolean safe)
	{
		transferPlayer(player, dim);

		if (safe)
		{
			WorldServer world = player.getServerForPlayer();
			BlockPos pos = new BlockPos(posX, posY, posZ);

			if (world.isAirBlock(pos) && world.isAirBlock(pos.up()))
			{
				while (pos.getY() > 1 && world.isAirBlock(pos.down()))
				{
					pos = pos.down();
				}

				BlockPos pos2 = pos.down();

				if (!world.isAirBlock(pos2) && !world.getBlockState(pos2).getMaterial().isLiquid())
				{
					setPlayerLocation(player, posX, pos.getY() + 0.5D, posZ, yaw, pitch);

					return true;
				}
			}
		}
		else
		{
			setPlayerLocation(player, posX, posY, posZ, yaw, pitch);

			return true;
		}

		return teleportPlayer(player, dim);
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
}