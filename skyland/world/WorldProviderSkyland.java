package skyland.world;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;

import com.google.common.base.Joiner;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.client.renderer.EmptyRenderer;
import skyland.core.Config;
import skyland.core.Skyland;
import skyland.network.DisplayGuiMessage;
import skyland.network.RegenerationGuiMessage;
import skyland.network.RegenerationGuiMessage.EnumType;
import skyland.network.SkyNetworkRegistry;
import skyland.util.SkyLog;
import skyland.util.SkyUtils;

public class WorldProviderSkyland extends WorldProviderSurface
{
	private static final Random rand = new Random();

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

	public static void regenerate(boolean backup)
	{
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

		for (EntityPlayerMP player : server.getPlayerList().getPlayerList())
		{
			if (SkyUtils.isEntityInSkyland(player))
			{
				SkyNetworkRegistry.sendToAll(new RegenerationGuiMessage(EnumType.FAILED));

				return;
			}
		}

		ITextComponent component;

		try
		{
			component = new TextComponentTranslation("skyland.regenerate.regenerating");
			component.getStyle().setColor(TextFormatting.GRAY).setItalic(true);
			server.getPlayerList().sendChatMsg(component);

			if (server.isSinglePlayer())
			{
				SkyNetworkRegistry.sendToAll(new DisplayGuiMessage(backup ? 0 : 1));
			}

			SkyNetworkRegistry.sendToAll(new RegenerationGuiMessage(EnumType.START));

			int dim = Config.dimension;
			WorldServer world = DimensionManager.getWorld(dim);

			if (world != null)
			{
				world.saveAllChunks(true, null);
				world.flush();

				if (world.provider instanceof WorldProviderSkyland)
				{
					((WorldProviderSkyland)world.provider).setSeed(rand.nextLong());
				}

				MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(world));

				DimensionManager.setWorld(dim, null, server);
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

					SkyNetworkRegistry.sendToAll(new RegenerationGuiMessage(EnumType.BACKUP));

					component = new TextComponentTranslation("skyland.regenerate.backingup");
					component.getStyle().setColor(TextFormatting.GRAY).setItalic(true);
					server.getPlayerList().sendChatMsg(component);

					if (SkyUtils.archiveDirZip(dir, bak))
					{
						ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_FILE, FilenameUtils.normalize(bak.getParentFile().getPath()));

						component = new TextComponentTranslation("skyland.regenerate.backedup");
						component.getStyle().setColor(TextFormatting.GRAY).setItalic(true).setClickEvent(click);
						server.getPlayerList().sendChatMsg(component);
					}
					else
					{
						component = new TextComponentTranslation("skyland.regenerate.backup.failed");
						component.getStyle().setColor(TextFormatting.RED).setItalic(true);
						server.getPlayerList().sendChatMsg(component);
					}
				}

				FileUtils.deleteDirectory(dir);
			}

			DimensionManager.initDimension(dim);

			world = DimensionManager.getWorld(dim);

			if (world != null)
			{
				world.saveAllChunks(true, null);
				world.flush();
			}

			SkyNetworkRegistry.sendToAll(new RegenerationGuiMessage(EnumType.SUCCESS));

			component = new TextComponentTranslation("skyland.regenerate.regenerated");
			component.getStyle().setColor(TextFormatting.GRAY).setItalic(true);
			server.getPlayerList().sendChatMsg(component);
		}
		catch (Exception e)
		{
			component = new TextComponentTranslation("skyland.regenerate.failed");
			component.getStyle().setColor(TextFormatting.RED).setItalic(true);
			server.getPlayerList().sendChatMsg(component);

			SkyNetworkRegistry.sendToAll(new RegenerationGuiMessage(EnumType.FAILED));

			SkyLog.log(Level.ERROR, e, component.getUnformattedText());
		}
	}

	public WorldProviderSkyland()
	{
		this.setDimension(Config.dimension);
	}

	@Override
	public IChunkGenerator createChunkGenerator()
	{
		return new ChunkProviderSkyland(worldObj);
	}

	@Override
	protected void createBiomeProvider()
	{
		biomeProvider = new BiomeProviderSkyland(getSeed(), worldObj.getWorldType(), worldObj.getWorldInfo().getGeneratorOptions());
	}

	@Override
	public boolean canCoordinateBeSpawn(int x, int z)
	{
		return !worldObj.isAirBlock(worldObj.getHeight(new BlockPos(x, 0, z)));
	}

	@Override
	public DimensionType getDimensionType()
	{
		return Skyland.DIM_SKYLAND;
	}

	public NBTTagCompound getDimensionData()
	{
		return worldObj.getWorldInfo().getDimensionData(getDimensionType());
	}

	public void saveDimensionData()
	{
		worldObj.getWorldInfo().setDimensionData(getDimensionType(), getDimensionData());
	}

	@Override
	public String getWelcomeMessage()
	{
		return "Entering the " + getDimensionType().getName();
	}

	@Override
	public String getDepartMessage()
	{
		return "Leaving the " + getDimensionType().getName();
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

	@Override
	public BlockPos getRandomizedSpawnPoint()
	{
		return getSpawnPoint();
	}

	@Override
	public boolean shouldMapSpin(String entity, double x, double y, double z)
	{
		return false;
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

		if (worldObj.getGameRules().getBoolean("doDaylightCycle"))
		{
			WorldInfo worldInfo = SkyUtils.getWorldInfo(worldObj);
			long i = worldInfo.getWorldTime() + 24000L;

			worldInfo.setWorldTime(i - i % 24000L);
		}
	}

	@Override
	public long getSeed()
	{
		long seed = super.getSeed();

		if (!worldObj.isRemote)
		{
			NBTTagCompound data = getDimensionData();

			if (!data.hasKey("Seed", NBT.TAG_ANY_NUMERIC))
			{
				data.setLong("Seed", rand.nextLong());

				saveDimensionData();
			}

			seed = data.getLong("Seed");
		}

		return seed;
	}

	public void setSeed(long seed)
	{
		getDimensionData().setLong("Seed", seed);

		saveDimensionData();
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