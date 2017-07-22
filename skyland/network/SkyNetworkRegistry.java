package skyland.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import skyland.core.Skyland;
import skyland.network.client.FallTeleportMessage;
import skyland.network.client.RegenerationGuiMessage;
import skyland.network.client.RegenerationOpenMessage;
import skyland.network.server.RegenerationMessage;

public class SkyNetworkRegistry
{
	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Skyland.MODID);

	public static int messageId;

	public static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side)
	{
		NETWORK.registerMessage(messageHandler, requestMessageType, messageId++, side);
	}

	public static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType)
	{
		registerMessage(messageHandler, requestMessageType, Side.CLIENT);
		registerMessage(messageHandler, requestMessageType, Side.SERVER);
	}

	public static void sendToAll(IMessage message)
	{
		NETWORK.sendToAll(message);
	}

	public static void sendTo(IMessage message, EntityPlayerMP player)
	{
		NETWORK.sendTo(message, player);
	}

	public static void sendToDimension(IMessage message, int dimensionId)
	{
		NETWORK.sendToDimension(message, dimensionId);
	}

	public static void sendToServer(IMessage message)
	{
		NETWORK.sendToServer(message);
	}

	public static void registerMessages()
	{
		registerMessage(FallTeleportMessage.class, FallTeleportMessage.class, Side.CLIENT);
		registerMessage(RegenerationGuiMessage.class, RegenerationGuiMessage.class, Side.CLIENT);
		registerMessage(RegenerationOpenMessage.class, RegenerationOpenMessage.class, Side.CLIENT);

		registerMessage(RegenerationMessage.class, RegenerationMessage.class, Side.SERVER);
	}
}