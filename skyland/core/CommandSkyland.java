package skyland.core;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import skyland.network.SkyNetworkRegistry;
import skyland.network.client.RegenerationGuiMessage;
import skyland.network.server.RegenerationMessage;

public class CommandSkyland extends CommandBase
{
	@Override
	public String getName()
	{
		return "skyland";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return String.format("/%s <%s>", getName(), String.join("|", getCommands()));
	}

	public String[] getCommands()
	{
		return new String[] {"regenerate"};
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, final String[] args) throws CommandException
	{
		if (args[0].equalsIgnoreCase("regenerate") && Skyland.SKYLAND == null)
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
					SkyNetworkRegistry.sendTo(new RegenerationGuiMessage(RegenerationGuiMessage.EnumType.OPEN), player);
				}
				else
				{
					throw new CommandException("commands.generic.permission");
				}
			}
			else
			{
				new RegenerationMessage(backup).regenerateDimension(Skyland.DIM_SKYLAND);
			}
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return sender instanceof MinecraftServer || sender instanceof EntityPlayerMP;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, getCommands()) : null;
	}
}