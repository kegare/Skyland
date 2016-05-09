package skyland.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.client.gui.GuiRegeneration;

public class DisplayGuiMessage implements IMessage, IMessageHandler<DisplayGuiMessage, IMessage>
{
	@SideOnly(Side.CLIENT)
	public static GuiScreen gui;

	private int type;

	public DisplayGuiMessage() {}

	public DisplayGuiMessage(int type)
	{
		this.type = type;
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
	public IMessage onMessage(DisplayGuiMessage message, MessageContext ctx)
	{
		switch (message.type)
		{
			case 0:
				gui = new GuiRegeneration(true);
				break;
			case 1:
				gui = new GuiRegeneration(false);
				break;
		}

		return null;
	}
}