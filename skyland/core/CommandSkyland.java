package skyland.core;

import java.util.List;

import com.google.common.base.Joiner;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.common.Loader;
import skyland.network.DisplayGuiMessage;
import skyland.network.SkyNetworkRegistry;
import skyland.util.Version;
import skyland.world.WorldProviderSkyland;

public class CommandSkyland extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "skyland";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return String.format("/%s <%s>", getCommandName(), Joiner.on('|').join(getCommands()));
	}

	public String[] getCommands()
	{
		return new String[] {"version", "regenerate"};
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, final String[] args) throws CommandException
	{
		if (args.length <= 0 || args[0].equalsIgnoreCase("version"))
		{
			ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, Skyland.metadata.url);
			ITextComponent component;
			ITextComponent message = new TextComponentString(" ");

			component = new TextComponentString("Skyland");
			component.getStyle().setColor(TextFormatting.AQUA);
			message.appendSibling(component);
			message.appendText(" " + Version.getCurrent());

			if (Version.DEV_DEBUG)
			{
				message.appendText(" ");
				component = new TextComponentString("dev");
				component.getStyle().setColor(TextFormatting.RED);
				message.appendSibling(component);
			}

			message.appendText(" for " + Loader.instance().getMCVersionString() + " ");
			component = new TextComponentString("(Latest: " + Version.getLatest() + ")");
			component.getStyle().setColor(TextFormatting.GRAY);
			message.appendSibling(component);
			message.getStyle().setClickEvent(click);
			sender.addChatMessage(message);

			message = new TextComponentString("  ");
			component = new TextComponentString(Skyland.metadata.description);
			component.getStyle().setClickEvent(click);
			message.appendSibling(component);
			sender.addChatMessage(message);

			message = new TextComponentString("  ");
			component = new TextComponentString(Skyland.metadata.url);
			component.getStyle().setColor(TextFormatting.DARK_GRAY).setClickEvent(click);
			message.appendSibling(component);
			sender.addChatMessage(message);
		}
		else if (args[0].equalsIgnoreCase("regenerate") && Skyland.SKYLAND == null)
		{
			boolean backup = true;

			if (args.length > 1)
			{
				try
				{
					backup = parseBoolean(args[1]);
				}
				catch (CommandException e)
				{
					backup = true;
				}
			}

			if (sender instanceof EntityPlayerMP)
			{
				EntityPlayerMP player = (EntityPlayerMP)sender;

				if (player.mcServer.isSinglePlayer() || player.mcServer.getPlayerList().canSendCommands(player.getGameProfile()))
				{
					SkyNetworkRegistry.sendTo(new DisplayGuiMessage(backup ? 0 : 1), player);
				}
				else
				{
					throw new CommandException("commands.generic.permission");
				}
			}
			else
			{
				WorldProviderSkyland.regenerate(backup);
			}
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return sender instanceof MinecraftServer || sender instanceof EntityPlayerMP;
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, getCommands()) : null;
	}
}