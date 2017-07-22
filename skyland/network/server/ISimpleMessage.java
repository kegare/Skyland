package skyland.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import skyland.network.SkyNetworkRegistry;

public interface ISimpleMessage<REQ extends ISimpleMessage<REQ, REPLY>, REPLY extends IMessage> extends IMessage, IMessageHandler<REQ, REPLY>
{
	@Override
	public default void fromBytes(ByteBuf buf) {}

	@Override
	public default void toBytes(ByteBuf buf) {}

	public REPLY process();

	@Override
	public default REPLY onMessage(REQ message, MessageContext ctx)
	{
		IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
		EntityPlayerMP player = ctx.getServerHandler().player;

		if (thread.isCallingFromMinecraftThread())
		{
			return message.process();
		}

		thread.addScheduledTask(() ->
		{
			REPLY result = message.process();

			if (result != null)
			{
				SkyNetworkRegistry.sendTo(result, player);
			}
		});

		return null;
	}
}