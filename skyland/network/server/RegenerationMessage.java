package skyland.network.server;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import skyland.core.Skyland;
import skyland.network.SkyNetworkRegistry;
import skyland.network.client.RegenerationGuiMessage;
import skyland.network.client.RegenerationGuiMessage.EnumType;
import skyland.stats.PortalCache;
import skyland.util.SkyUtils;

public class RegenerationMessage implements ISimpleMessage<RegenerationMessage, IMessage>
{
	private boolean backup = true;

	public RegenerationMessage() {}

	public RegenerationMessage(boolean backup)
	{
		this.backup = backup;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		backup = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(backup);
	}

	@Override
	public IMessage process()
	{
		regenerateDimension(Skyland.DIM_SKYLAND);

		return null;
	}

	public boolean regenerateDimension(@Nullable DimensionType type)
	{
		if (type == null)
		{
			return false;
		}

		File rootDir = DimensionManager.getCurrentSaveRootDirectory();

		if (rootDir == null || !rootDir.exists())
		{
			sendProgress(EnumType.FAILED);

			return false;
		}

		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		WorldServer world = server.getWorld(type.getId());

		if (!world.playerEntities.isEmpty())
		{
			sendProgress(EnumType.FAILED);

			return false;
		}

		File dimDir = world.provider.getSaveFolder() == null ? rootDir : new File(rootDir, world.provider.getSaveFolder());

		if (!dimDir.exists())
		{
			sendProgress(EnumType.FAILED);

			return false;
		}

		ITextComponent name = new TextComponentString(SkyUtils.getDimensionName(type).trim());
		name.getStyle().setBold(true);
		ITextComponent message = new TextComponentTranslation("skyland.regeneration.regenerating", name);
		message.getStyle().setColor(TextFormatting.GRAY);

		server.getPlayerList().sendMessage(message);

		if (server.isSinglePlayer())
		{
			sendProgress(EnumType.OPEN);
		}

		sendProgress(EnumType.START);

		MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(world));

		world.flush();
		world.getWorldInfo().setDimensionData(type.getId(), new NBTTagCompound());

		DimensionManager.setWorld(type.getId(), null, server);

		if (backup)
		{
			Calendar calendar = Calendar.getInstance();
			String year = Integer.toString(calendar.get(Calendar.YEAR));
			String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
			String day = String.format("%02d", calendar.get(Calendar.DATE));
			String hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
			String minute = String.format("%02d", calendar.get(Calendar.MINUTE));
			String second = String.format("%02d", calendar.get(Calendar.SECOND));
			File bak = new File(rootDir, type.getName().replaceAll(" ", "") + "_bak-" + String.join("", year, month, day) + "-" + String.join("", hour, minute, second) + ".zip");

			message = new TextComponentTranslation("skyland.regeneration.backup", name);
			message.getStyle().setColor(TextFormatting.GRAY);

			server.getPlayerList().sendMessage(message);

			sendProgress(EnumType.BACKUP);

			if (SkyUtils.archiveDirectory(dimDir, bak))
			{
				message = new TextComponentTranslation("skyland.regeneration.backup.success", name);
				message.getStyle().setColor(TextFormatting.GRAY);

				server.getPlayerList().sendMessage(message);
			}
			else
			{
				message = new TextComponentTranslation("skyland.regeneration.backup.failed", name);
				message.getStyle().setColor(TextFormatting.RED);

				server.getPlayerList().sendMessage(message);

				sendProgress(EnumType.FAILED);

				return false;
			}
		}

		try
		{
			FileUtils.deleteDirectory(dimDir);
		}
		catch (IOException e)
		{
			sendProgress(EnumType.FAILED);

			return false;
		}

		if (type.shouldLoadSpawn())
		{
			DimensionManager.initDimension(type.getId());
		}

		for (EntityPlayerMP player : server.getPlayerList().getPlayers())
		{
			PortalCache.get(player).clearLastPos(null, type);
		}

		message = new TextComponentTranslation("skyland.regeneration.regenerated", name);
		message.getStyle().setColor(TextFormatting.GRAY);

		server.getPlayerList().sendMessage(message);

		sendProgress(EnumType.SUCCESS);

		return true;
	}

	private void sendProgress(EnumType type)
	{
		SkyNetworkRegistry.sendToAll(new RegenerationGuiMessage(type));
	}
}