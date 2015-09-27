/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;
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
		player.mountEntity(null);
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
			player.mcServer.getConfigurationManager().transferPlayerToDimension(player, dim, new TeleporterDummy(player.mcServer.worldServerForDimension(dim)));
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

			if (!world.isAirBlock(pos2) && !world.getBlockState(pos2).getBlock().getMaterial().isLiquid())
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

							if (!world.isAirBlock(pos3) && !world.getBlockState(pos3).getBlock().getMaterial().isLiquid())
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

				if (!world.isAirBlock(pos2) && !world.getBlockState(pos2).getBlock().getMaterial().isLiquid())
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

	public static MovingObjectPosition getMouseOverExtended(float dist)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();
		Entity renderEntity = mc.getRenderViewEntity();
		AxisAlignedBB boundingBox = new AxisAlignedBB(renderEntity.posX - 0.5D, renderEntity.posY - 0.0D, renderEntity.posZ - 0.5D, renderEntity.posX + 0.5D, renderEntity.posY + 1.5D, renderEntity.posZ + 0.5D);
		MovingObjectPosition mop = null;

		if (mc.theWorld != null)
		{
			double var1 = dist;
			mop = renderEntity.rayTrace(var1, 0);
			double calcdist = var1;
			Vec3 pos = renderEntity.getPositionEyes(0);
			var1 = calcdist;

			if (mop != null)
			{
				calcdist = mop.hitVec.distanceTo(pos);
			}

			Vec3 lookvec = renderEntity.getLook(0);
			Vec3 var2 = pos.addVector(lookvec.xCoord * var1, lookvec.yCoord * var1, lookvec.zCoord * var1);
			Entity pointedEntity = null;
			float range = 1.0F;
			@SuppressWarnings("unchecked")
			List<Entity> list = mc.theWorld.getEntitiesWithinAABBExcludingEntity(renderEntity, boundingBox.addCoord(lookvec.xCoord * var1, lookvec.yCoord * var1, lookvec.zCoord * var1).expand(range, range, range));
			double d = calcdist;

			for (Entity entity : list)
			{
				if (entity.canBeCollidedWith())
				{
					float size = entity.getCollisionBorderSize();
					AxisAlignedBB axis = new AxisAlignedBB(entity.posX - entity.width / 2, entity.posY, entity.posZ - entity.width / 2, entity.posX + entity.width / 2, entity.posY + entity.height, entity.posZ + entity.width / 2);
					axis.expand(size, size, size);
					MovingObjectPosition mop1 = axis.calculateIntercept(pos, var2);

					if (axis.isVecInside(pos))
					{
						if (0.0D < d || d == 0.0D)
						{
							pointedEntity = entity;
							d = 0.0D;
						}
					}
					else if (mop1 != null)
					{
						double d1 = pos.distanceTo(mop1.hitVec);

						if (d1 < d || d == 0.0D)
						{
							pointedEntity = entity;
							d = d1;
						}
					}
				}
			}

			if (pointedEntity != null && (d < calcdist || mop == null))
			{
				mop = new MovingObjectPosition(pointedEntity);
			}
		}

		return mop;
	}
}