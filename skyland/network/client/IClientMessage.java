package skyland.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import skyland.network.SkyNetworkRegistry;

public interface IClientMessage<REQ extends IClientMessage<REQ, REPLY>, REPLY extends IMessage> extends IMessage, IMessageHandler<REQ, REPLY>
{
	@Override
	public default void fromBytes(ByteBuf buf) {}

	@Override
	public default void toBytes(ByteBuf buf) {}

	public REPLY process(Minecraft mc);

	@Override
	public default REPLY onMessage(REQ message, MessageContext ctx)
	{
		IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
		Minecraft mc = FMLClientHandler.instance().getClient();

		if (thread.isCallingFromMinecraftThread())
		{
			return message.process(mc);
		}

		thread.addScheduledTask(() ->
		{
			REPLY result = message.process(mc);

			if (result != null)
			{
				SkyNetworkRegistry.sendToServer(result);
			}
		});

		return null;
	}
}