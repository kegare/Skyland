package skyland.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.client.gui.GuiRegeneration;

public class RegenerationGuiMessage implements IMessage, IMessageHandler<RegenerationGuiMessage, IMessage>
{
	private int type;

	public RegenerationGuiMessage() {}

	public RegenerationGuiMessage(EnumType type)
	{
		this.type = type.ordinal();
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		type = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(type);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(RegenerationGuiMessage message, MessageContext ctx)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();
		EnumType type = EnumType.get(message.type);

		if (mc.currentScreen != null && mc.currentScreen instanceof GuiRegeneration)
		{
			GuiRegeneration gui = (GuiRegeneration)mc.currentScreen;

			gui.updateProgress(type);
		}

		return null;
	}

	public enum EnumType
	{
		START,
		BACKUP,
		SUCCESS,
		FAILED;

		public static EnumType get(int type)
		{
			EnumType[] types = values();

			if (type < 0 || type >= types.length)
			{
				return FAILED;
			}

			return types[type];
		}
	}
}