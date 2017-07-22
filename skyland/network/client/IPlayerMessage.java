package skyland.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import skyland.network.SkyNetworkRegistry;

public interface IPlayerMessage<REQ extends IPlayerMessage<REQ, REPLY>, REPLY extends IMessage> extends IMessage, IMessageHandler<REQ, REPLY>
{
	@Override
	public default void fromBytes(ByteBuf buf) {}

	@Override
	public default void toBytes(ByteBuf buf) {}

	public REPLY process(EntityPlayerSP player);

	@Override
	public default REPLY onMessage(REQ message, MessageContext ctx)
	{
		IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
		EntityPlayerSP player = FMLClientHandler.instance().getClientPlayerEntity();

		if (thread.isCallingFromMinecraftThread())
		{
			return message.process(player);
		}

		thread.addScheduledTask(() ->
		{
			REPLY result = message.process(player);

			if (result != null)
			{
				SkyNetworkRegistry.sendToServer(result);
			}
		});

		return null;
	}
}