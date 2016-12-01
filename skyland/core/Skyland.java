package skyland.core;

import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Metadata;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
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
@EventBusSubscriber
public class Skyland
{
	public static final String MODID = "skyland";

	@Metadata(MODID)
	public static ModMetadata metadata;

	public static final CreativeTabs TAB_SKYLAND = new CreativeTabSkyland();

	public static DimensionType DIM_SKYLAND;
	public static WorldType SKYLAND;

	@EventHandler
	public void construct(FMLConstructionEvent event)
	{
		Version.initVersion();
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		IForgeRegistry<Block> registry = event.getRegistry();

		SkyBlocks.registerBlocks(registry);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();

		SkyBlocks.registerItemBlocks(registry);
		SkyItems.registerItems(registry);
	}

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event)
	{
		IForgeRegistry<SoundEvent> registry = event.getRegistry();

		SkySounds.registerSounds(registry);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Config.syncConfig();

		if (event.getSide().isClient())
		{
			SkyBlocks.registerModels();
			SkyItems.registerModels();
		}

		SkyBlocks.registerOreDicts();
		SkyItems.registerOreDicts();

		SkyCapabilities.registerCapabilities();

		SkyNetworkRegistry.registerMessages();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		SkyBlocks.registerRecipes();
		SkyItems.registerRecipes();

		int id = Config.dimension;

		if (id == 0)
		{
			if (SKYLAND == null)
			{
				try
				{
					SKYLAND = new WorldTypeSkyland();

					SkyLog.info("Register the world type of Skyland (" + SKYLAND.getWorldTypeID() + ")");
				}
				catch (IllegalArgumentException e)
				{
					SkyLog.log(Level.ERROR, e, "An error occurred trying to register the world type of Skyland");
				}
			}
		}
		else
		{
			DIM_SKYLAND = DimensionType.register("Skyland", "_skyland", id, WorldProviderSkyland.class, false);

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
		SkyEventHooks.FIRST_PLAYERS.clear();
		SkyEventHooks.FALL_TELEPORT_PLAYERS.get().clear();
	}
}