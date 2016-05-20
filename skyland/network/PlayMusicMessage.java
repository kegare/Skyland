package skyland.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayMusicMessage implements IMessage, IMessageHandler<PlayMusicMessage, IMessage>
{
	private String music;

	public PlayMusicMessage() {}

	public PlayMusicMessage(SoundEvent music)
	{
		this.music = music.getRegistryName().toString();
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		music = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, music);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(final PlayMusicMessage message, MessageContext ctx)
	{
		final Minecraft mc = FMLClientHandler.instance().getClient();

		mc.addScheduledTask(
			new Runnable()
			{
				@Override
				public void run()
				{
					SoundHandler handler = mc.getSoundHandler();

					handler.stopSounds();
					handler.playSound(PositionedSoundRecord.getMusicRecord(SoundEvent.REGISTRY.getObject(new ResourceLocation(message.music))));
				}
			}
		);

		return null;
	}
}
