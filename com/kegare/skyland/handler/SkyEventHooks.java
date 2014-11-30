/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.skyland.handler;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Sets;
import com.kegare.skyland.api.SkylandAPI;
import com.kegare.skyland.core.Config;
import com.kegare.skyland.core.Skyland;
import com.kegare.skyland.network.DimSyncMessage;
import com.kegare.skyland.network.FallTeleportMessage;
import com.kegare.skyland.util.SkyUtils;
import com.kegare.skyland.util.Version;
import com.kegare.skyland.util.Version.Status;
import com.kegare.skyland.world.WorldProviderSkyland;

public class SkyEventHooks
{
	public static final SkyEventHooks instance = new SkyEventHooks();

	public static final Set<String> firstJoinPlayers = Sets.newHashSet();
	public static final ThreadLocal<Set<String>> fallTeleportPlayers = new ThreadLocal()
	{
		@Override
		protected Set<String> initialValue()
		{
			return Sets.newHashSet();
		}
	};

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent event)
	{
		if (event.modID.equals(Skyland.MODID))
		{
			Config.syncConfig();
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderGameTextOverlay(RenderGameOverlayEvent.Text event)
	{
		if (SkylandAPI.getWorldType() != null)
		{
			return;
		}

		Minecraft mc = FMLClientHandler.instance().getClient();

		if (SkylandAPI.isEntityInSkyland(mc.thePlayer))
		{
			if (mc.gameSettings.showDebugInfo)
			{
				event.left.add("Dim: Skyland");
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientConnected(ClientConnectedToServerEvent event)
	{
		if (Version.getStatus() == Status.PENDING || Version.getStatus() == Status.FAILED)
		{
			Version.versionCheck();
		}
		else if (Version.DEV_DEBUG || Config.versionNotify && Version.isOutdated())
		{
			IChatComponent component = new ChatComponentTranslation("skyland.version.message", EnumChatFormatting.AQUA + "Skyland" + EnumChatFormatting.RESET);
			component.appendText(" : " + EnumChatFormatting.YELLOW + Version.getLatest());
			component.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Skyland.metadata.url));

			event.handler.handleChat(new S02PacketChat(component));
		}
	}

	@SubscribeEvent
	public void onServerConnected(ServerConnectionFromClientEvent event)
	{
		event.manager.sendPacket(Skyland.network.getPacketFrom(new DimSyncMessage(SkylandAPI.getDimension(), WorldProviderSkyland.getDimData())));
	}

	@SubscribeEvent
	public void onPlayerLoadFromFile(PlayerEvent.LoadFromFile event)
	{
		for (String str : event.playerDirectory.list())
		{
			if (str.startsWith(event.playerUUID))
			{
				return;
			}
		}

		firstJoinPlayers.add(event.playerUUID);
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event)
	{
		if (event.player instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)event.player;

			if (firstJoinPlayers.contains(player.getUniqueID().toString()))
			{
				WorldServer world = player.getServerForPlayer();
				boolean result = false;

				if (world.getWorldInfo().getTerrainType() == SkylandAPI.getWorldType())
				{
					result = SkyUtils.teleportPlayer(player, player.dimension, 0, 254.0D, 0, player.rotationYaw, player.rotationPitch, true);
				}
				else if (Config.skyborn && !SkylandAPI.isEntityInSkyland(player))
				{
					result = SkyUtils.teleportPlayer(player, SkylandAPI.getDimension());
				}

				if (result)
				{
					player.setSpawnChunk(player.getPosition(), true, player.dimension);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerLoggedOutEvent event)
	{
		EntityPlayer player = event.player;
		String uuid = player.getUniqueID().toString();

		firstJoinPlayers.remove(uuid);
		fallTeleportPlayers.get().remove(uuid);
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if (event.player instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)event.player;

			if (SkylandAPI.isEntityInSkyland(player))
			{
				if (player.getServerForPlayer().isAirBlock(player.getPosition().offsetDown()))
				{
					SkyUtils.teleportPlayer(player, player.dimension);
				}
			}
		}
	}

	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{
		if (event.phase != Phase.END)
		{
			return;
		}

		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		WorldServer world = server.worldServerForDimension(0);

		if (world.getWorldInfo().getTerrainType() == SkylandAPI.getWorldType())
		{
			world.prevRainingStrength = 0.0F;
			world.rainingStrength = 0.0F;
			world.prevThunderingStrength = 0.0F;
			world.thunderingStrength = 0.0F;
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		if (SkylandAPI.getWorldType() != null)
		{
			return;
		}

		if (event.entityLiving instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)event.entityLiving;
			ItemStack current = player.getCurrentEquippedItem();
			boolean feather = current != null && current.getItem() == Items.feather;

			if (player.dimension == 0)
			{
				if (player.isPlayerSleeping() && player.getSleepTimer() >= 75 && feather)
				{
					if (!player.capabilities.isCreativeMode && --current.stackSize <= 0)
					{
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					}

					BlockPos pos = player.getBedLocation(SkylandAPI.getDimension());
					boolean result;

					if (pos == null)
					{
						result = SkyUtils.teleportPlayer(player, SkylandAPI.getDimension());
					}
					else
					{
						result = SkyUtils.teleportPlayer(player, SkylandAPI.getDimension(), pos, player.rotationYaw, player.rotationPitch, true);
					}

					if (result && player.mcServer.getConfigurationManager().getCurrentPlayerCount() <= 1)
					{
						player.getServerForPlayer().provider.resetRainAndThunder();
					}
				}
			}
			else if (SkylandAPI.isEntityInSkyland(player))
			{
				if (!player.onGround && player.getEntityBoundingBox().minY <= -30.0D)
				{
					boolean result = false;

					if (feather)
					{
						BlockPos pos = player.getBedLocation(0);

						if (pos != null)
						{
							result = SkyUtils.teleportPlayer(player, 0, pos.getX() + 0.5D, 254.0D, pos.getZ() + 0.5D, player.rotationYaw, player.rotationPitch, false);
						}
					}

					if (!result)
					{
						result = SkyUtils.teleportPlayer(player, 0, player.posX, 254.0D, player.posZ, player.rotationYaw, player.rotationPitch, false);
					}

					if (result)
					{
						SkyUtils.setPlayerLocation(player, player.posX, 300.5D, player.posZ);

						fallTeleportPlayers.get().add(player.getUniqueID().toString());

						Skyland.network.sendTo(new FallTeleportMessage(player), player);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingFall(LivingFallEvent event)
	{
		if (event.entityLiving instanceof EntityPlayer && fallTeleportPlayers.get().remove(event.entityLiving.getUniqueID().toString()))
		{
			EntityPlayer player = (EntityPlayer)event.entityLiving;
			ItemStack current = player.getCurrentEquippedItem();

			if (current != null && current.getItem() == Items.feather)
			{
				if (!player.capabilities.isCreativeMode && --current.stackSize <= 0)
				{
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}

				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		if (SkylandAPI.getWorldType() != null)
		{
			return;
		}

		World world = event.world;

		if (!world.isRemote)
		{
			if (world.provider.getDimensionId() == SkylandAPI.getDimension())
			{
				WorldProviderSkyland.saveDimData();
			}
		}
	}
}