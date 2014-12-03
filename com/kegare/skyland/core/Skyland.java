/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.skyland.core;

import static com.kegare.skyland.core.Skyland.*;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Metadata;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import com.kegare.skyland.api.SkylandAPI;
import com.kegare.skyland.handler.SkyEventHooks;
import com.kegare.skyland.handler.SkylandAPIHandler;
import com.kegare.skyland.network.DimSyncMessage;
import com.kegare.skyland.network.FallTeleportMessage;
import com.kegare.skyland.network.PlaySoundMessage;
import com.kegare.skyland.network.RegenerateMessage;
import com.kegare.skyland.network.RegenerateProgressMessage;
import com.kegare.skyland.util.Version;

@Mod
(
	modid = MODID,
	acceptedMinecraftVersions = "[1.8,)",
	guiFactory = MOD_PACKAGE + ".client.config.SkyGuiFactory"
)
public class Skyland
{
	public static final String
	MODID = "kegare.skyland",
	MOD_PACKAGE = "com.kegare.skyland",
	CONFIG_LANG = "skyland.config.";

	@Metadata(MODID)
	public static ModMetadata metadata;

	@SidedProxy(modId = MODID, clientSide = MOD_PACKAGE + ".client.ClientProxy", serverSide = MOD_PACKAGE + ".core.CommonProxy")
	public static CommonProxy proxy;

	public static final SimpleNetworkWrapper network = new SimpleNetworkWrapper(MODID);

	public static WorldType SKYLAND;

	@EventHandler
	public void construct(FMLConstructionEvent event)
	{
		SkylandAPI.instance = new SkylandAPIHandler();

		Version.versionCheck();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Config.syncConfig();

		int id = 0;
		network.registerMessage(Config.class, Config.class, id++, Side.CLIENT);
		network.registerMessage(DimSyncMessage.class, DimSyncMessage.class, id++, Side.CLIENT);
		network.registerMessage(PlaySoundMessage.class, PlaySoundMessage.class, id++, Side.CLIENT);
		network.registerMessage(RegenerateMessage.class, RegenerateMessage.class, id++, Side.CLIENT);
		network.registerMessage(RegenerateMessage.class, RegenerateMessage.class, id++, Side.SERVER);
		network.registerMessage(RegenerateProgressMessage.class, RegenerateProgressMessage.class, id++, Side.CLIENT);
		network.registerMessage(FallTeleportMessage.class, FallTeleportMessage.class, id++, Side.CLIENT);
		network.registerMessage(FallTeleportMessage.class, FallTeleportMessage.class, id++, Side.SERVER);

//		SkyItems.registerItems();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
//		proxy.registerModels();

		FMLCommonHandler.instance().bus().register(SkyEventHooks.instance);

		MinecraftForge.EVENT_BUS.register(SkyEventHooks.instance);
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandSkyland());

		if (event.getSide().isServer() && (Version.DEV_DEBUG || Config.versionNotify && Version.isOutdated()))
		{
			event.getServer().logInfo(String.format(StatCollector.translateToLocal("skyland.version.message"), "Skyland") + ": " + Version.getLatest());
		}
	}

	@EventHandler
	public void serverStopping(FMLServerStoppedEvent event)
	{
		SkyEventHooks.firstJoinPlayers.clear();
		SkyEventHooks.fallTeleportPlayers.get().clear();
	}
}