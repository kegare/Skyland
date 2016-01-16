/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.handler;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.api.SkylandAPI;
import skyland.core.Config;
import skyland.core.Skyland;
import skyland.item.ItemSkyFeather;
import skyland.item.SkyItems;
import skyland.network.DimSyncMessage;
import skyland.network.ExtendedReachAttackMessage;
import skyland.network.FallTeleportMessage;
import skyland.network.PlaySoundMessage;
import skyland.util.IExtendedReach;
import skyland.util.SkyUtils;
import skyland.util.Version;
import skyland.util.Version.Status;
import skyland.world.WorldProviderSkyland;

public class SkyEventHooks
{
	public static final SkyEventHooks instance = new SkyEventHooks();

	public static final Set<String> firstJoinPlayers = Sets.newHashSet();
	public static final ThreadLocal<Set<String>> fallTeleportPlayers = new ThreadLocal<Set<String>>()
	{
		@Override
		protected Set<String> initialValue()
		{
			return Sets.newHashSet();
		}
	};

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event)
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
	@SubscribeEvent(receiveCanceled = true)
	public void onMouse(MouseEvent event)
	{
		if (event.button == 0 && event.buttonstate)
		{
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer thePlayer = mc.thePlayer;

			if (thePlayer != null)
			{
				ItemStack itemstack = thePlayer.getCurrentEquippedItem();
				IExtendedReach extended;

				if (itemstack != null)
				{
					if (itemstack.getItem() instanceof IExtendedReach)
					{
						extended = (IExtendedReach)itemstack.getItem();
					}
					else
					{
						extended = null;
					}

					if (extended != null)
					{
						float reach = extended.getReach();
						MovingObjectPosition mov = SkyUtils.getMouseOverExtended(reach);

						if (mov != null)
						{
							if (mov.entityHit != null && mov.entityHit.hurtResistantTime == 0)
							{
								if (mov.entityHit != thePlayer )
								{
									Skyland.network.sendToServer(new ExtendedReachAttackMessage(mov.entityHit.getEntityId()));
								}
							}
						}
					}
				}
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

			FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(component);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientDisconnected(ClientDisconnectionFromServerEvent event)
	{
		Config.syncConfig();
	}

	@SubscribeEvent
	public void onServerConnected(ServerConnectionFromClientEvent event)
	{
		event.manager.sendPacket(Skyland.network.getPacketFrom(new Config()));
		event.manager.sendPacket(Skyland.network.getPacketFrom(new DimSyncMessage(WorldProviderSkyland.getDimData())));
	}

	@SubscribeEvent
	public void onPlayerLoadFromFile(PlayerEvent.LoadFromFile event)
	{
		for (String str : event.playerDirectory.list())
		{
			if (StringUtils.startsWith(str, event.playerUUID))
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

					Skyland.network.sendTo(new PlaySoundMessage("skyland:skyland"), player);
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
				if (player.getServerForPlayer().isAirBlock(player.getPosition().down()))
				{
					SkyUtils.teleportPlayer(player, player.dimension);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent event)
	{
		if (event.player instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)event.player;

			if (event.toDim == SkylandAPI.getDimension())
			{
				WorldServer world = player.getServerForPlayer();
				NBTTagCompound data = player.getEntityData();
				String key = "Skyland:LastTeleportTime";

				if (!data.hasKey(key) || data.getLong(key) + 18000L < world.getTotalWorldTime())
				{
					Skyland.network.sendTo(new PlaySoundMessage("skyland:skyland"), player);
				}

				data.setLong(key, world.getTotalWorldTime());
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event)
	{
		if (event.phase != Phase.END)
		{
			return;
		}

		World world = FMLClientHandler.instance().getWorldClient();

		if (world != null && world.provider.getDimensionId() == 0 && world.getWorldInfo().getTerrainType() == SkylandAPI.getWorldType())
		{
			world.prevRainingStrength = 0.0F;
			world.rainingStrength = 0.0F;
			world.prevThunderingStrength = 0.0F;
			world.thunderingStrength = 0.0F;
		}
	}

	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event)
	{
		if (event.phase != Phase.END)
		{
			return;
		}

		World world = event.world;

		if (world.provider.getDimensionId() == 0 && world.getWorldInfo().getTerrainType() == SkylandAPI.getWorldType())
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
		EntityLivingBase entity = event.entityLiving;

		if (SkylandAPI.getWorldType() != null)
		{
			return;
		}

		if (entity instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)entity;

			if (player.dimension == 0)
			{
				if (!player.onGround && player.getEntityBoundingBox().minY > 350.0D && player.getEntityData().getBoolean("Skyland:SkyJump"))
				{
					SkyUtils.teleportPlayer(player, SkylandAPI.getDimension());

					player.getEntityData().removeTag("Skyland:SkyJump");
				}
			}
			else if (SkylandAPI.isEntityInSkyland(player))
			{
				if (!player.onGround && player.getEntityBoundingBox().minY <= -20.0D)
				{
					ItemStack current = player.getCurrentEquippedItem();
					boolean result = false;

					if (current != null && current.getItem() instanceof ItemSkyFeather)
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
						SkyUtils.setPlayerLocation(player, player.posX, 350.5D, player.posZ);

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
		if (SkylandAPI.getWorldType() != null)
		{
			return;
		}

		if (event.entityLiving instanceof EntityPlayer && fallTeleportPlayers.get().remove(event.entityLiving.getUniqueID().toString()))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		EntityLivingBase entity = event.entityLiving;

		if (entity instanceof EntityChicken)
		{
			if (SkylandAPI.getWorldType() != null)
			{
				return;
			}

			if (!entity.worldObj.isRemote)
			{
				int rate = 0;

				if (entity.dimension == 0)
				{
					rate = 15;
				}
				else if (SkylandAPI.isEntityInSkyland(entity))
				{
					rate = 5;
				}

				if (rate > 0 && entity.getRNG().nextInt(rate) == 0)
				{
					entity.dropItem(SkyItems.sky_feather, 1);
				}
			}
		}
		else if (entity instanceof EntityCreeper)
		{
			if (!entity.worldObj.isRemote && SkylandAPI.isEntityInSkyland(entity) && entity.getRNG().nextInt(20) == 0)
			{
				entity.dropItem(SkyItems.record_skyland, 1);
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