package skyland.handler;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import skyland.core.Config;
import skyland.core.Skyland;
import skyland.item.ItemSkyFeather;
import skyland.item.SkyItems;
import skyland.network.SkyNetworkRegistry;
import skyland.network.client.FallTeleportMessage;
import skyland.stats.PortalCache;
import skyland.util.SkyUtils;
import skyland.world.TeleporterSkyland;

public class SkyEventHooks
{
	public static final Set<String> FALL_CANCELABLE_PLAYERS = Sets.newHashSet();

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

		if (!Config.skyborn || Skyland.DIM_SKYLAND == null)
		{
			return;
		}

		EntityPlayer player = event.getEntityPlayer();

		player.dimension = Skyland.DIM_SKYLAND.getId();
		player.setLocationAndAngles(0.5D, 128.0D, 0.5D, player.rotationYaw, player.rotationPitch);

		FALL_CANCELABLE_PLAYERS.add(uuid);
	}

	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event)
	{
		if (event.phase != Phase.END || Skyland.SKYLAND == null)
		{
			return;
		}

		World world = event.world;

		if (world.provider.getDimensionType() == DimensionType.OVERWORLD && world.getWorldInfo().getTerrainType() == Skyland.SKYLAND)
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
		if (Skyland.DIM_SKYLAND == null)
		{
			return;
		}

		EntityLivingBase entity = event.getEntityLiving();

		if (entity instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)entity;
			WorldServer world = player.getServerWorld();
			DimensionType type = world.provider.getDimensionType();

			if (type == DimensionType.OVERWORLD)
			{
				if (!player.onGround && player.getEntityBoundingBox().minY > 350.0D)
				{
					PortalCache.get(player).setLastPos(TeleporterSkyland.KEY, type, world.getHeight(player.getPosition()));

					SkyUtils.teleportToDimension(player, Skyland.DIM_SKYLAND);

					return;
				}
			}
			else if (type == Skyland.DIM_SKYLAND)
			{
				if (!player.onGround && player.getEntityBoundingBox().minY <= -20.0D)
				{
					SkyUtils.teleportToDimension(player, DimensionType.OVERWORLD);

					player.connection.setPlayerLocation(player.posX, 305.0D, player.posZ, player.rotationYaw, 60.0F);

					FALL_CANCELABLE_PLAYERS.add(player.getCachedUniqueIdString());

					SkyNetworkRegistry.sendTo(new FallTeleportMessage(), player);

					return;
				}
			}

			if ((type == DimensionType.OVERWORLD || type == Skyland.DIM_SKYLAND) && player.isPlayerSleeping())
			{
				ItemStack stack = ItemStack.EMPTY;

				for (ItemStack held : player.getHeldEquipment())
				{
					if (held.getItem() instanceof ItemSkyFeather)
					{
						stack = held;

						break;
					}
				}

				if (!stack.isEmpty())
				{
					int time = ObfuscationReflectionHelper.getPrivateValue(EntityPlayer.class, player, "sleepTimer", "field_71076_b");

					if (time >= 75)
					{
						player.wakeUpPlayer(false, false, true);

						if (!player.capabilities.isCreativeMode)
						{
							stack.shrink(1);
						}

						SkyUtils.teleportToDimension(player, type == Skyland.DIM_SKYLAND ? DimensionType.OVERWORLD : Skyland.DIM_SKYLAND);

						if (player.getServer().getPlayerList().getCurrentPlayerCount() <= 1)
						{
							world = player.getServerWorld();
							world.provider.resetRainAndThunder();

							if (type == DimensionType.OVERWORLD && world.getGameRules().getBoolean("doDaylightCycle"))
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

		if (entity instanceof EntityPlayer && FALL_CANCELABLE_PLAYERS.remove(entity.getCachedUniqueIdString()))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onLivingDrops(LivingDropsEvent event)
	{
		if (Skyland.DIM_SKYLAND == null)
		{
			return;
		}

		EntityLivingBase entity = event.getEntityLiving();
		boolean isChicken = entity instanceof EntityChicken;
		World world = entity.world;
		DimensionType type = world.provider.getDimensionType();
		double chance = 0.0D;

		if (type == DimensionType.OVERWORLD)
		{
			chance = isChicken ? 0.15D : 0.0D;
		}
		else if (type == Skyland.DIM_SKYLAND)
		{
			chance = isChicken ? 1.0D : 0.45D;
		}

		if (chance <= 0.0D)
		{
			return;
		}

		DamageSource source = event.getSource();

		if (source.getTrueSource() == null || !(source.getTrueSource() instanceof EntityPlayer))
		{
			return;
		}

		if (source.getImmediateSource() == null || !(source.getImmediateSource() instanceof EntityPlayer))
		{
			return;
		}

		int looting = event.getLootingLevel();

		if (looting > 0)
		{
			chance = Math.min(chance * looting, 1.0D);
		}

		if (Math.random() < chance)
		{
			double posX = entity.posX;
			double posY = entity.posY + 0.5D;
			double posZ = entity.posZ;

			event.getDrops().add(new EntityItem(world, posX, posY, posZ, new ItemStack(SkyItems.SKY_FEATHER)));
		}
	}
}