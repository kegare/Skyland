package skyland.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import skyland.core.Skyland;

public class SkyNetworkRegistry
{
	public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(Skyland.MODID);

	public static int messageId;

	public static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side)
	{
		network.registerMessage(messageHandler, requestMessageType, messageId++, side);
	}

	public static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType)
	{
		registerMessage(messageHandler, requestMessageType, Side.CLIENT);
		registerMessage(messageHandler, requestMessageType, Side.SERVER);
	}

	public static Packet<?> getPacket(IMessage message)
	{
		return network.getPacketFrom(message);
	}

	public static void sendToAll(IMessage message)
	{
		network.sendToAll(message);
	}

	public static void sendToOthers(IMessage message, EntityPlayerMP player)
	{
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

		if (server != null && server.isDedicatedServer())
		{
			for (EntityPlayerMP thePlayer : server.getPlayerList().getPlayerList())
			{
				if (player == thePlayer)
				{
					sendTo(message, thePlayer);
				}
			}
		}
	}

	public static void sendTo(IMessage message, EntityPlayerMP player)
	{
		network.sendTo(message, player);
	}

	public static void sendToDimension(IMessage message, int dimensionId)
	{
		network.sendToDimension(message, dimensionId);
	}

	public static void sendToServer(IMessage message)
	{
		network.sendToServer(message);
	}

	public static void registerMessages()
	{
		registerMessage(DisplayGuiMessage.class, DisplayGuiMessage.class, Side.CLIENT);
		registerMessage(PlayMusicMessage.class, PlayMusicMessage.class, Side.CLIENT);
		registerMessage(FallTeleportMessage.class, FallTeleportMessage.class, Side.CLIENT);
		registerMessage(RegenerationMessage.class, RegenerationMessage.class, Side.SERVER);
		registerMessage(RegenerationGuiMessage.class, RegenerationGuiMessage.class, Side.CLIENT);
	}
}