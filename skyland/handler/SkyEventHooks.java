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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeVersion.Status;
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
import skyland.util.SkyUtils;
import skyland.util.Version;
import skyland.world.WorldProviderSkyland;

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
		Minecraft mc = FMLClientHandler.instance().getClient();

		if (Version.DEV_DEBUG || Version.getStatus() == Status.AHEAD || Version.getStatus() == Status.BETA || Config.versionNotify && Version.isOutdated())
		{
			ITextComponent name = new TextComponentString(Skyland.metadata.name);
			name.getChatStyle().setColor(TextFormatting.AQUA);
			ITextComponent latest = new TextComponentString(Version.getLatest().toString());
			latest.getChatStyle().setColor(TextFormatting.YELLOW);

			ITextComponent message;

			message = new TextComponentTranslation("skyland.version.message", name);
			message.appendText(" : ").appendSibling(latest);
			message.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Skyland.metadata.url));

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
				WorldServer world = player.getServerForPlayer();
				boolean result = false;

				if (world.getWorldInfo().getTerrainType() == Skyland.SKYLAND)
				{
					result = SkyUtils.teleportPlayer(player, player.dimension, 0, 254.0D, 0, player.rotationYaw, player.rotationPitch, true);
				}
				else if (Config.skyborn)
				{
					result = SkyUtils.teleportPlayer(player, Config.dimension);
				}

				if (result)
				{
					player.setSpawnChunk(player.getPosition(), true, player.dimension);

					SkyNetworkRegistry.sendTo(new PlayMusicMessage(SkySounds.skyland), player);
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

			if (SkyUtils.isEntityInSkyland(player))
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

			if (event.toDim == Config.dimension)
			{
				WorldServer world = player.getServerForPlayer();
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
					SkyUtils.teleportPlayer(player, Config.dimension);

					player.getEntityData().removeTag("Skyland:SkyJump");
				}
			}
			else if (SkyUtils.isEntityInSkyland(player))
			{
				if (!player.onGround && player.getEntityBoundingBox().minY <= -20.0D)
				{
					boolean result = false;
					ItemStack[] helds = new ItemStack[] {player.getHeldItemMainhand(), player.getHeldItemOffhand()};

					for (ItemStack current : helds)
					{
						if (current != null && current.getItem() instanceof ItemSkyFeather)
						{
							BlockPos pos = player.getBedLocation(0);

							if (pos != null)
							{
								result = SkyUtils.teleportPlayer(player, 0, pos.getX() + 0.5D, 254.0D, pos.getZ() + 0.5D, player.rotationYaw, player.rotationPitch, false);
								break;
							}
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

						SkyNetworkRegistry.sendTo(new FallTeleportMessage(player), player);
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

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		if (Skyland.SKYLAND != null)
		{
			return;
		}

		World world = event.getWorld();

		if (!world.isRemote)
		{
			if (world.provider.getDimension() == Config.dimension)
			{
				WorldProviderSkyland.saveDimData();
			}
		}
	}
}