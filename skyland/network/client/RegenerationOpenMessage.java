package skyland.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.client.gui.GuiRegeneration;

public class RegenerationOpenMessage implements IClientMessage<RegenerationOpenMessage, IMessage>
{
	@SideOnly(Side.CLIENT)
	public static GuiScreen gui;

	private boolean type;

	public RegenerationOpenMessage() {}

	public RegenerationOpenMessage(boolean type)
	{
		this.type = type;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		type = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(type);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage process(Minecraft mc)
	{
		mc.displayGuiScreen(new GuiRegeneration(type));

		return null;
	}
}