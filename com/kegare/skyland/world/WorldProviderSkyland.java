/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.skyland.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.kegare.skyland.api.SkylandAPI;
import com.kegare.skyland.client.renderer.EmptyRenderer;
import com.kegare.skyland.core.Config;
import com.kegare.skyland.core.Skyland;
import com.kegare.skyland.network.DimSyncMessage;
import com.kegare.skyland.network.RegenerateMessage;
import com.kegare.skyland.network.RegenerateProgressMessage;
import com.kegare.skyland.util.SkyLog;
import com.kegare.skyland.util.SkyUtils;

public class WorldProviderSkyland extends WorldProviderSurface
{
	private static NBTTagCompound dimData;
	private static long dimensionSeed;

	public static NBTTagCompound getDimData()
	{
		if (dimData == null)
		{
			dimData = readDimData();
		}

		return dimData;
	}

	public static File getDimDir()
	{
		File root = DimensionManager.getCurrentSaveRootDirectory();

		if (root == null || !root.exists() || root.isFile())
		{
			return null;
		}

		File dir = new File(root, new WorldProviderSkyland().getSaveFolder());

		if (!dir.exists())
		{
			dir.mkdirs();
		}

		return dir.isDirectory() ? dir : null;
	}

	private static NBTTagCompound readDimData()
	{
		NBTTagCompound data;
		File dir = getDimDir();

		if (dir == null)
		{
			data = null;
		}
		else
		{
			File file = new File(dir, "skyland.dat");

			if (!file.exists() || !file.isFile() || !file.canRead())
			{
				data = null;
			}
			else try (FileInputStream input = new FileInputStream(file))
			{
				data = CompressedStreamTools.readCompressed(input);
			}
			catch (Exception e)
			{
				SkyLog.log(Level.ERROR, e, "An error occurred trying to reading Skyland dimension data");

				data = null;
			}
		}

		return data == null ? new NBTTagCompound() : data;
	}

	private static void writeDimData()
	{
		File dir = getDimDir();

		if (dir == null)
		{
			return;
		}

		try (FileOutputStream output = new FileOutputStream(new File(dir, "skyland.dat")))
		{
			CompressedStreamTools.writeCompressed(getDimData(), output);
		}
		catch (Exception e)
		{
			SkyLog.log(Level.ERROR, e, "An error occurred trying to writing Skyland dimension data");
		}
	}

	public static void loadDimData(NBTTagCompound data)
	{
		if (!data.hasKey("Seed"))
		{
			data.setLong("Seed", new SecureRandom().nextLong());
		}

		dimensionSeed = data.getLong("Seed");
	}

	public static void saveDimData()
	{
		if (dimData != null)
		{
			writeDimData();

			dimData = null;
		}
	}

	public static void regenerate(final boolean backup)
	{
		final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		Set<EntityPlayerMP> target = Sets.newHashSet();

		for (Object obj : server.getConfigurationManager().playerEntityList.toArray())
		{
			EntityPlayerMP player = (EntityPlayerMP)obj;

			if (SkylandAPI.isEntityInSkyland(player))
			{
				SkyUtils.teleportPlayer(player, 0);

				target.add(player);
			}
		}

		boolean result = SkyUtils.getPool().invoke(new RecursiveTask<Boolean>()
		{
			@Override
			protected Boolean compute()
			{
				IChatComponent component;

				try
				{
					component = new ChatComponentText(StatCollector.translateToLocal("skyland.regenerate.regenerating"));
					component.getChatStyle().setColor(EnumChatFormatting.GRAY).setItalic(true);
					server.getConfigurationManager().sendChatMsg(component);

					if (server.isSinglePlayer())
					{
						Skyland.network.sendToAll(new RegenerateMessage(backup));
					}

					Skyland.network.sendToAll(new RegenerateProgressMessage(0));

					int dim = SkylandAPI.getDimension();
					WorldServer world = DimensionManager.getWorld(dim);

					if (world != null)
					{
						world.saveAllChunks(true, null);
						world.flush();

						MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(world));

						DimensionManager.setWorld(dim, null);
					}

					File dir = getDimDir();

					if (dir != null)
					{
						if (backup)
						{
							File parent = dir.getParentFile();
							final Pattern pattern = Pattern.compile("^" + dir.getName() + "_bak-..*\\.zip$");
							File[] files = parent.listFiles(new FilenameFilter()
							{
								@Override
								public boolean accept(File dir, String name)
								{
									return pattern.matcher(name).matches();
								}
							});

							if (files != null && files.length >= 5)
							{
								Arrays.sort(files, new Comparator<File>()
								{
									@Override
									public int compare(File o1, File o2)
									{
										int i = SkyUtils.compareWithNull(o1, o2);

										if (i == 0 && o1 != null && o2 != null)
										{
											try
											{
												i = Files.getLastModifiedTime(o1.toPath()).compareTo(Files.getLastModifiedTime(o2.toPath()));
											}
											catch (IOException e) {}
										}

										return i;
									}
								});

								FileUtils.forceDelete(files[0]);
							}

							Calendar calendar = Calendar.getInstance();
							String year = Integer.toString(calendar.get(Calendar.YEAR));
							String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
							String day = String.format("%02d", calendar.get(Calendar.DATE));
							String hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
							String minute = String.format("%02d", calendar.get(Calendar.MINUTE));
							String second = String.format("%02d", calendar.get(Calendar.SECOND));
							File bak = new File(parent, dir.getName() + "_bak-" + Joiner.on("").join(year, month, day) + "-" + Joiner.on("").join(hour, minute, second) + ".zip");

							if (bak.exists())
							{
								FileUtils.deleteQuietly(bak);
							}

							component = new ChatComponentText(StatCollector.translateToLocal("skyland.regenerate.backingup"));
							component.getChatStyle().setColor(EnumChatFormatting.GRAY).setItalic(true);
							server.getConfigurationManager().sendChatMsg(component);

							Skyland.network.sendToAll(new RegenerateProgressMessage(1));

							if (SkyUtils.archiveDirZip(dir, bak))
							{
								ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_FILE, FilenameUtils.normalize(bak.getParentFile().getPath()));

								component = new ChatComponentText(StatCollector.translateToLocal("skyland.regenerate.backedup"));
								component.getChatStyle().setColor(EnumChatFormatting.GRAY).setItalic(true).setChatClickEvent(click);
								server.getConfigurationManager().sendChatMsg(component);
							}
							else
							{
								component = new ChatComponentText(StatCollector.translateToLocal("skyland.regenerate.backup.failed"));
								component.getChatStyle().setColor(EnumChatFormatting.RED).setItalic(true);
								server.getConfigurationManager().sendChatMsg(component);
							}
						}

						FileUtils.deleteDirectory(dir);
					}

					if (DimensionManager.shouldLoadSpawn(dim))
					{
						DimensionManager.initDimension(dim);

						world = DimensionManager.getWorld(dim);

						if (world != null)
						{
							world.saveAllChunks(true, null);
							world.flush();
						}
					}

					component = new ChatComponentText(StatCollector.translateToLocal("skyland.regenerate.regenerated"));
					component.getChatStyle().setColor(EnumChatFormatting.GRAY).setItalic(true);
					server.getConfigurationManager().sendChatMsg(component);

					Skyland.network.sendToAll(new RegenerateProgressMessage(2));

					return true;
				}
				catch (Exception e)
				{
					component = new ChatComponentText(StatCollector.translateToLocal("skyland.regenerate.failed"));
					component.getChatStyle().setColor(EnumChatFormatting.RED).setItalic(true);
					server.getConfigurationManager().sendChatMsg(component);

					Skyland.network.sendToAll(new RegenerateProgressMessage(3));

					SkyLog.log(Level.ERROR, e, component.getUnformattedText());
				}

				return false;
			}
		});

		if (result && Config.skyborn)
		{
			for (EntityPlayerMP player : target)
			{
				if (!SkylandAPI.isEntityInSkyland(player))
				{
					SkyUtils.teleportPlayer(player, SkylandAPI.getDimension());
				}
			}
		}
	}

	public WorldProviderSkyland()
	{
		this.dimensionId = SkylandAPI.getDimension();
	}

	@Override
	public IChunkProvider createChunkGenerator()
	{
		return new ChunkProviderSkyland(worldObj);
	}

	@Override
	public boolean canCoordinateBeSpawn(int x, int z)
	{
		return !worldObj.isAirBlock(worldObj.getHorizon(new BlockPos(x, 0, z)));
	}

	@Override
	public String getDimensionName()
	{
		return "Skyland";
	}

	@Override
	public String getInternalNameSuffix()
	{
		return "_skyland";
	}

	@Override
	public String getSaveFolder()
	{
		if (SkyUtils.mcpc)
		{
			return "DIM" + dimensionId;
		}

		return "DIM-" + getDimensionName();
	}

	@Override
	public String getWelcomeMessage()
	{
		return "Entering the " + getDimensionName();
	}

	@Override
	public String getDepartMessage()
	{
		return "Leaving the " + getDimensionName();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IRenderHandler getWeatherRenderer()
	{
		if (super.getWeatherRenderer() == null)
		{
			setWeatherRenderer(EmptyRenderer.instance);
		}

		return super.getWeatherRenderer();
	}

	@Override
	public BlockPos getSpawnPoint()
	{
		return new BlockPos(0, 64, 0);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getCloudHeight()
	{
		return 1.5F;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public double getVoidFogYFactor()
	{
		return 1.0D;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getSunBrightness(float ticks)
	{
		return super.getSunBrightness(ticks) * 1.25F;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getStarBrightness(float ticks)
	{
		return super.getStarBrightness(ticks) * 1.5F;
	}

	@Override
	public void calculateInitialWeather()
	{
		updateWeather();
	}

	@Override
	public void updateWeather()
	{
		worldObj.prevRainingStrength = 0.0F;
		worldObj.rainingStrength = 0.0F;
		worldObj.prevThunderingStrength = 0.0F;
		worldObj.thunderingStrength = 0.0F;
	}

	@Override
	public void resetRainAndThunder()
	{
		super.resetRainAndThunder();

		if (worldObj.getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
		{
			WorldInfo worldInfo = SkyUtils.getWorldInfo(worldObj);
			long i = worldInfo.getWorldTime() + 24000L;

			worldInfo.setWorldTime(i - i % 24000L);
		}
	}

	@Override
	public long getSeed()
	{
		if (!worldObj.isRemote && dimData == null)
		{
			loadDimData(getDimData());

			Skyland.network.sendToAll(new DimSyncMessage(getDimData()));
		}

		return dimensionSeed;
	}

	@Override
	public boolean isBlockHighHumidity(BlockPos pos)
	{
		return false;
	}

	@Override
	public double getHorizon()
	{
		return 0.0D;
	}

	@Override
	public boolean canSnowAt(BlockPos pos, boolean checkLight)
	{
		return false;
	}

	@Override
	public boolean canDoLightning(Chunk chunk)
	{
		return false;
	}

	@Override
	public boolean canDoRainSnowIce(Chunk chunk)
	{
		return false;
	}
}