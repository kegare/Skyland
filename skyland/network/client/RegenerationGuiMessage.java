package skyland.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.client.gui.GuiRegeneration;

public class RegenerationGuiMessage implements IClientMessage<RegenerationGuiMessage, IMessage>
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
	public IMessage process(Minecraft mc)
	{
		EnumType action = EnumType.get(type);

		if (mc.currentScreen != null && mc.currentScreen instanceof GuiRegeneration)
		{
			GuiRegeneration gui = (GuiRegeneration)mc.currentScreen;

			gui.updateProgress(action);

			if (action == EnumType.SUCCESS)
			{
				mc.displayGuiScreen(null);
				mc.setIngameFocus();
			}
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