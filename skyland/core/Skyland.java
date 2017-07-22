package skyland.core;

import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Metadata;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import skyland.block.SkyBlocks;
import skyland.capability.SkyCapabilities;
import skyland.client.handler.ClientEventHooks;
import skyland.handler.SkyEventHooks;
import skyland.handler.TerrainEventHooks;
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
	updateJSON = "https://raw.githubusercontent.com/kegare/Skyland/master/skyland.json"
)
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

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event)
	{
		IForgeRegistry<Block> registry = event.getRegistry();

		SkyBlocks.registerBlocks(registry);
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();

		SkyBlocks.registerItemBlocks(registry);
		SkyItems.registerItems(registry);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event)
	{
		SkyBlocks.registerModels();

		SkyItems.registerModels();
	}

	@SubscribeEvent
	public void registerSounds(RegistryEvent.Register<SoundEvent> event)
	{
		IForgeRegistry<SoundEvent> registry = event.getRegistry();

		SkySounds.registerSounds(registry);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Config.syncConfig();

		SkyCapabilities.registerCapabilities();

		if (event.getSide().isClient())
		{
			MinecraftForge.EVENT_BUS.register(new ClientEventHooks());
		}

		MinecraftForge.EVENT_BUS.register(new SkyEventHooks());
		MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainEventHooks());
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		SkyBlocks.registerOreDicts();
		SkyItems.registerOreDicts();

		SkyBlocks.registerSmeltingRecipes();

		SkyNetworkRegistry.registerMessages();

		int id = Config.dimension;

		if (id == 0)
		{
			if (SKYLAND == null)
			{
				try
				{
					SKYLAND = new WorldTypeSkyland();

					SkyLog.info("Register the world type of Skyland (" + SKYLAND.getId() + ")");
				}
				catch (IllegalArgumentException e)
				{
					SkyLog.log(Level.ERROR, e, "An error occurred trying to register the world type of Skyland");
				}
			}
		}
		else
		{
			DIM_SKYLAND = DimensionType.register("skyland", "_skyland", id, WorldProviderSkyland.class, false);

			DimensionManager.registerDimension(id, DIM_SKYLAND);
		}
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
		SkyEventHooks.FALL_CANCELABLE_PLAYERS.clear();
	}
}