/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.network;

import java.util.Iterator;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlaySoundMessage implements IMessage, IMessageHandler<PlaySoundMessage, IMessage>
{
	private String sound;

	public PlaySoundMessage() {}

	public PlaySoundMessage(String sound)
	{
		this.sound = sound;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		sound = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, sound);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(final PlaySoundMessage message, MessageContext ctx)
	{
		final Minecraft mc = FMLClientHandler.instance().getClient();

		mc.addScheduledTask(
			new Runnable()
			{
				@Override
				public void run()
				{
					SoundHandler handler = mc.getSoundHandler();
					SoundManager manager = ObfuscationReflectionHelper.getPrivateValue(SoundHandler.class, handler, "sndManager", "field_147694_f");
					Map<String, ISound> playingSounds = ObfuscationReflectionHelper.getPrivateValue(SoundManager.class, manager, "playingSounds", "field_148629_h");
					Iterator<String> iterator = playingSounds.keySet().iterator();

					while (iterator.hasNext())
					{
						PositionedSound sound = (PositionedSound)playingSounds.get(iterator.next());

						if ("music.game".equals(sound.getSoundLocation().getResourcePath()))
						{
							handler.stopSound(sound);
							break;
						}
					}

					handler.playSound(PositionedSoundRecord.create(new ResourceLocation(message.sound)));
				}
			}
		);

		return null;
	}
}