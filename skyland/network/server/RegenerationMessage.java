package skyland.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import skyland.world.WorldProviderSkyland;

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
		WorldProviderSkyland.regenerate(backup);

		return null;
	}
}