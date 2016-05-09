package skyland.core;

import org.apache.logging.log4j.Level;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Metadata;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import skyland.block.SkyBlocks;
import skyland.capability.SkyCapabilities;
import skyland.handler.SkyEventHooks;
import skyland.item.SkyItems;
import skyland.network.SkyNetworkRegistry;
import skyland.util.SkyLog;
import skyland.util.Version;
import skyland.world.WorldProviderSkyland;
import skyland.world.WorldTypeSkyland;

@Mod
(
	modid = Skyland.MODID,
	guiFactory = "skyland.client.config.SkyGuiFactory",
	updateJSON = "https://dl.dropboxusercontent.com/u/51943112/versions/skyland.json"
)
public class Skyland
{
	public static final String
	MODID = "skyland",
	CONFIG_LANG = "skyland.config.";

	@Metadata(MODID)
	public static ModMetadata metadata;

	public static final CreativeTabs tabSkyland = new CreativeTabSkyland();

	public static DimensionType DIM_SKYLAND;
	public static WorldType SKYLAND;

	@EventHandler
	public void construct(FMLConstructionEvent event)
	{
		Version.initVersion();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Config.syncConfig();

		SkyBlocks.registerBlocks();
		SkyItems.registerItems();

		if (event.getSide().isClient())
		{
			SkyBlocks.registerModels();
			SkyItems.registerModels();
		}

		SkyBlocks.registerRecipes();
		SkyItems.registerRecipes();

		SkySounds.registerSounds();

		SkyCapabilities.registerCapabilities();

		SkyNetworkRegistry.registerMessages();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		int id = Config.dimension;

		if (id == 0)
		{
			if (SKYLAND == null)
			{
				try
				{
					SKYLAND = new WorldTypeSkyland();

					SkyLog.fine("Register the world type of Skyland (" + SKYLAND.getWorldTypeID() + ")");
				}
				catch (IllegalArgumentException e)
				{
					SkyLog.log(Level.ERROR, e, "An error occurred trying to register the world type of Skyland");
				}
			}
		}
		else
		{
			DIM_SKYLAND = DimensionType.register("Skyland", "_skyland", id, WorldProviderSkyland.class, true);

			DimensionManager.registerDimension(id, DIM_SKYLAND);
		}

		MinecraftForge.EVENT_BUS.register(new SkyEventHooks());
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandSkyland());
	}

	@EventHandler
	public void serverStopping(FMLServerStoppedEvent event)
	{
		SkyEventHooks.firstJoinPlayers.clear();
		SkyEventHooks.fallTeleportPlayers.get().clear();
	}
}