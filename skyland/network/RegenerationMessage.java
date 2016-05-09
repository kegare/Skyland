package skyland.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import skyland.world.WorldProviderSkyland;

public class RegenerationMessage implements IMessage, IMessageHandler<RegenerationMessage, IMessage>
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
	public IMessage onMessage(RegenerationMessage message, MessageContext ctx)
	{
		WorldProviderSkyland.regenerate(message.backup);

		return null;
	}
}