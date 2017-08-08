package skyland.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import skyland.core.Skyland;
import skyland.world.TeleporterPersonal;

public class SkyUtils
{
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

	public static boolean archiveDirectory(File dir, File dest)
	{
		Path dirPath = dir.toPath();
		String parent = dir.getName();
		Map<String, String> env = Maps.newHashMap();
		env.put("create", "true");
		URI uri = dest.toURI();

		try
		{
			uri = new URI("jar:" + uri.getScheme(), uri.getPath(), null);
		}
		catch (URISyntaxException e)
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
		catch (IOException e)
		{
			SkyLog.log(Level.WARN, e, "An error occurred archiving the " + parent + "directory.");
		}

		return false;
	}

	public static int compareWithNull(Object o1, Object o2)
	{
		return (o1 == null ? 1 : 0) - (o2 == null ? 1 : 0);
	}

	public static WorldInfo getWorldInfo(World world)
	{
		WorldInfo worldInfo = world.getWorldInfo();

		if (worldInfo instanceof DerivedWorldInfo)
		{
			worldInfo = ObfuscationReflectionHelper.getPrivateValue(DerivedWorldInfo.class, (DerivedWorldInfo)worldInfo, "delegate", "field_76115_a");
		}

		return worldInfo;
	}

	public static String getDimensionName(DimensionType type)
	{
		String key = "dimension." + type.getName() + ".name";

		return I18n.canTranslate(key) ? I18n.translateToLocal(key) : type.getName().replaceAll(" ", "").toUpperCase(Locale.ENGLISH);
	}

	public static boolean isSkyland(@Nullable World world)
	{
		if (world == null)
		{
			return false;
		}

		DimensionType type = world.provider.getDimensionType();

		if (Skyland.DIM_SKYLAND != null)
		{
			return type == Skyland.DIM_SKYLAND;
		}

		if (Skyland.SKYLAND != null && world.getWorldInfo().getTerrainType() == Skyland.SKYLAND)
		{
			return type == DimensionType.OVERWORLD;
		}

		return false;
	}

	public static boolean isEntityInSkyland(@Nullable Entity entity)
	{
		if (entity == null || entity.isDead)
		{
			return false;
		}

		return isSkyland(entity.world);
	}

	public static void setDimensionChange(EntityPlayerMP player)
	{
		if (!player.capabilities.isCreativeMode)
		{
			ObfuscationReflectionHelper.setPrivateValue(EntityPlayerMP.class, player, true, "invulnerableDimensionChange", "field_184851_cj");
		}
	}

	public static void teleportToDimension(EntityPlayerMP player, @Nullable DimensionType type)
	{
		if (type == null)
		{
			return;
		}

		MinecraftServer server = player.mcServer;
		WorldServer worldNew = server.getWorld(type.getId());

		server.getPlayerList().transferPlayerToDimension(player, type.getId(), new TeleporterPersonal(worldNew));

		player.addExperienceLevel(0);
	}
}