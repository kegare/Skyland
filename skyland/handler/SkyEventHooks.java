package skyland.handler;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.core.Config;
import skyland.core.SkySounds;
import skyland.core.Skyland;
import skyland.item.ItemSkyFeather;
import skyland.item.SkyItems;
import skyland.network.DisplayGuiMessage;
import skyland.network.FallTeleportMessage;
import skyland.network.PlayMusicMessage;
import skyland.network.SkyNetworkRegistry;
import skyland.stats.IPortalCache;
import skyland.stats.PortalCache;
import skyland.util.SkyUtils;
import skyland.util.Version;

public class SkyEventHooks
{
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
		if (event.getModID().equals(Skyland.MODID))
		{
			Config.syncConfig();
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderGameTextOverlay(RenderGameOverlayEvent.Text event)
	{
		if (Skyland.SKYLAND != null)
		{
			return;
		}

		Minecraft mc = FMLClientHandler.instance().getClient();

		if (SkyUtils.isEntityInSkyland(mc.thePlayer))
		{
			if (mc.gameSettings.showDebugInfo)
			{
				event.getLeft().add("Dim: Skyland");
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientConnected(ClientConnectedToServerEvent event)
	{
		final Minecraft mc = FMLClientHandler.instance().getClient();

		mc.addScheduledTask(new Runnable()
		{
			@Override
			public void run()
			{
				if (Version.DEV_DEBUG || Version.getStatus() == Status.AHEAD || Version.getStatus() == Status.BETA || Config.versionNotify && Version.isOutdated())
				{
					ITextComponent name = new TextComponentString(Skyland.metadata.name);
					name.getStyle().setColor(TextFormatting.AQUA);
					ITextComponent latest = new TextComponentString(Version.getLatest().toString());
					latest.getStyle().setColor(TextFormatting.YELLOW);

					ITextComponent message;

					message = new TextComponentTranslation("skyland.version.message", name);
					message.appendText(" : ").appendSibling(latest);
					message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Skyland.metadata.url));

					mc.ingameGUI.getChatGUI().printChatMessage(message);
					message = null;

					if (Version.isBeta())
					{
						message = new TextComponentTranslation("skyland.version.message.beta", name);
					}
					else if (Version.isAlpha())
					{
						message = new TextComponentTranslation("skyland.version.message.alpha", name);
					}

					if (message != null)
					{
						mc.ingameGUI.getChatGUI().printChatMessage(message);
					}
				}
			}
		});
	}

	@SubscribeEvent
	public void onPlayerLoadFromFile(PlayerEvent.LoadFromFile event)
	{
		String uuid = event.getPlayerUUID();

		for (String str : event.getPlayerDirectory().list())
		{
			if (StringUtils.startsWith(str, uuid))
			{
				return;
			}
		}

		firstJoinPlayers.add(uuid);
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event)
	{
		if (event.player instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)event.player;

			if (firstJoinPlayers.contains(player.getUniqueID().toString()))
			{
				WorldServer world = player.getServerWorld();

				if (world.getWorldInfo().getTerrainType() == Skyland.SKYLAND)
				{
					SkyUtils.teleportPlayer(player, player.dimension, 0.0D, 254.0D, 0.0D);
				}
				else if (Config.skyborn)
				{
					SkyUtils.teleportPlayer(player, Config.dimension);
				}

				player.setSpawnChunk(player.getPosition(), true, player.dimension);

				SkyNetworkRegistry.sendTo(new PlayMusicMessage(SkySounds.skyland), player);
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

			if (SkyUtils.isEntityInSkyland(player))
			{
				if (player.getServerWorld().isAirBlock(player.getPosition().down()))
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

			if (event.toDim == Config.dimension)
			{
				WorldServer world = player.getServerWorld();
				NBTTagCompound data = player.getEntityData();
				String key = "Skyland:LastTeleportTime";

				if (!data.hasKey(key) || data.getLong(key) + 18000L < world.getTotalWorldTime())
				{
					SkyNetworkRegistry.sendTo(new PlayMusicMessage(SkySounds.skyland), player);
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

		if (DisplayGuiMessage.gui != null)
		{
			FMLClientHandler.instance().showGuiScreen(DisplayGuiMessage.gui);

			DisplayGuiMessage.gui = null;
		}

		if (Skyland.SKYLAND != null)
		{
			World world = FMLClientHandler.instance().getWorldClient();

			if (world != null && world.provider.getDimension() == 0 && world.getWorldInfo().getTerrainType() == Skyland.SKYLAND)
			{
				world.prevRainingStrength = 0.0F;
				world.rainingStrength = 0.0F;
				world.prevThunderingStrength = 0.0F;
				world.thunderingStrength = 0.0F;
			}
		}
	}

	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event)
	{
		if (event.phase != Phase.END || Skyland.SKYLAND == null)
		{
			return;
		}

		World world = event.world;

		if (world.getWorldInfo().getTerrainType() == Skyland.SKYLAND && world.provider.getDimension() == 0)
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
		if (Skyland.SKYLAND != null)
		{
			return;
		}

		EntityLivingBase entity = event.getEntityLiving();

		if (entity instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)entity;

			if (player.dimension == 0)
			{
				if (!player.onGround && player.getEntityBoundingBox().minY > 350.0D && player.getEntityData().getBoolean("Skyland:SkyJump"))
				{
					PortalCache.get(player).setLastPos(1, 0, player.getServerWorld().getHeight(player.getPosition()));

					SkyUtils.teleportPlayer(player, Config.dimension);

					player.getEntityData().removeTag("Skyland:SkyJump");

					return;
				}
			}
			else if (SkyUtils.isEntityInSkyland(player))
			{
				if (!player.onGround && player.getEntityBoundingBox().minY <= -20.0D)
				{
					IPortalCache cache = PortalCache.get(player);

					if (cache.hasLastPos(1, 0))
					{
						BlockPos pos = cache.getLastPos(1, 0);

						SkyUtils.teleportPlayer(player, 0, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, true);
					}
					else
					{
						SkyUtils.teleportPlayer(player, 0);
					}

					player.connection.setPlayerLocation(player.posX, 350.5D, player.posZ, player.rotationYaw, player.rotationPitch);

					fallTeleportPlayers.get().add(player.getUniqueID().toString());

					SkyNetworkRegistry.sendTo(new FallTeleportMessage(player), player);

					return;
				}
			}

			if ((player.dimension == 0 || SkyUtils.isEntityInSkyland(player)) && player.isPlayerSleeping())
			{
				ItemStack itemstack = null;

				for (ItemStack held : player.getHeldEquipment())
				{
					if (held != null && held.getItem() instanceof ItemSkyFeather)
					{
						itemstack = held;

						break;
					}
				}

				if (itemstack != null)
				{
					int time = ObfuscationReflectionHelper.getPrivateValue(EntityPlayer.class, player, "sleepTimer", "field_71076_b");

					if (time >= 75)
					{
						player.wakeUpPlayer(false, false, true);

						if (!player.capabilities.isCreativeMode)
						{
							--itemstack.stackSize;
						}

						SkyUtils.teleportPlayer(player, player.dimension == 0 ? Config.dimension : 0);

						if (player.getServer().getPlayerList().getCurrentPlayerCount() <= 1)
						{
							WorldServer world = player.getServerWorld();

							world.provider.resetRainAndThunder();

							if (!SkyUtils.isEntityInSkyland(player) && world.getGameRules().getBoolean("doDaylightCycle"))
							{
								WorldInfo worldInfo = SkyUtils.getWorldInfo(world);
								long i = worldInfo.getWorldTime() + 24000L;

								worldInfo.setWorldTime(i - i % 24000L);
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingFall(LivingFallEvent event)
	{
		if (Skyland.SKYLAND != null)
		{
			return;
		}

		EntityLivingBase entity = event.getEntityLiving();

		if (entity instanceof EntityPlayer && fallTeleportPlayers.get().remove(entity.getUniqueID().toString()))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if (Skyland.SKYLAND != null)
		{
			return;
		}

		EntityLivingBase entity = event.getEntityLiving();

		if (entity instanceof EntityChicken)
		{
			if (!entity.worldObj.isRemote)
			{
				int rate = 0;

				if (entity.dimension == 0)
				{
					rate = 15;
				}
				else if (SkyUtils.isEntityInSkyland(entity))
				{
					rate = 5;
				}

				if (rate > 0 && entity.getRNG().nextInt(rate) == 0)
				{
					entity.dropItem(SkyItems.sky_feather, 1);
				}
			}
		}
	}
}