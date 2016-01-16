/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.core;

import java.awt.Desktop;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.Loader;
import skyland.api.SkylandAPI;
import skyland.network.RegenerateMessage;
import skyland.util.Version;
import skyland.world.WorldProviderSkyland;

public class CommandSkyland implements ICommand
{
	@Override
	public int compareTo(ICommand command)
	{
		return getCommandName().compareTo(command.getCommandName());
	}

	@Override
	public String getCommandName()
	{
		return "skyland";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "commands.generic.notFound";
	}

	@Override
	public List getCommandAliases()
	{
		return Collections.emptyList();
	}

	@Override
	public void processCommand(ICommandSender sender, final String[] args)
	{
		if (args.length <= 0 || args[0].equalsIgnoreCase("version"))
		{
			ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, Skyland.metadata.url);
			IChatComponent component;
			IChatComponent message = new ChatComponentText(" ");

			component = new ChatComponentText("Skyland");
			component.getChatStyle().setColor(EnumChatFormatting.AQUA);
			message.appendSibling(component);
			message.appendText(" " + Version.getCurrent());

			if (Version.DEV_DEBUG)
			{
				message.appendText(" ");
				component = new ChatComponentText("dev");
				component.getChatStyle().setColor(EnumChatFormatting.RED);
				message.appendSibling(component);
			}

			message.appendText(" for " + Loader.instance().getMCVersionString() + " ");
			component = new ChatComponentText("(Latest: " + Version.getLatest() + ")");
			component.getChatStyle().setColor(EnumChatFormatting.GRAY);
			message.appendSibling(component);
			message.getChatStyle().setChatClickEvent(click);
			sender.addChatMessage(message);

			message = new ChatComponentText("  ");
			component = new ChatComponentText(Skyland.metadata.description);
			component.getChatStyle().setChatClickEvent(click);
			message.appendSibling(component);
			sender.addChatMessage(message);

			message = new ChatComponentText("  ");
			component = new ChatComponentText(Skyland.metadata.url);
			component.getChatStyle().setColor(EnumChatFormatting.DARK_GRAY).setChatClickEvent(click);
			message.appendSibling(component);
			sender.addChatMessage(message);
		}
		else if (args[0].equalsIgnoreCase("forum") || args[0].equalsIgnoreCase("url"))
		{
			try
			{
				Desktop.getDesktop().browse(new URI(Skyland.metadata.url));
			}
			catch (Exception e) {}
		}
		else if (args[0].equalsIgnoreCase("regenerate") && SkylandAPI.getWorldType() == null)
		{
			boolean backup = true;

			if (args.length > 1)
			{
				try
				{
					backup = CommandBase.parseBoolean(args[1]);
				}
				catch (CommandException e)
				{
					backup = true;
				}
			}

			if (sender instanceof EntityPlayerMP)
			{
				EntityPlayerMP player = (EntityPlayerMP)sender;

				if (player.mcServer.isSinglePlayer() || player.mcServer.getConfigurationManager().canSendCommands(player.getGameProfile()))
				{
					Skyland.network.sendTo(new RegenerateMessage(backup), player);
				}
				else
				{
					IChatComponent component = new ChatComponentTranslation("commands.generic.permission");
					component.getChatStyle().setColor(EnumChatFormatting.RED);
					sender.addChatMessage(component);
				}
			}
			else
			{
				WorldProviderSkyland.regenerate(backup);
			}
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return sender instanceof MinecraftServer || sender instanceof EntityPlayerMP;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
	{
		return args.length == 1 ? CommandBase.getListOfStringsMatchingLastWord(args, "version", "forum", "regenerate") : null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index)
	{
		return false;
	}
}