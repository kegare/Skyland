/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.skyland.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
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
	public IMessage onMessage(PlaySoundMessage message, MessageContext ctx)
	{
		SoundHandler handler = FMLClientHandler.instance().getClient().getSoundHandler();
		ISound sound = PositionedSoundRecord.create(new ResourceLocation(message.sound));

		if (!handler.isSoundPlaying(sound))
		{
			handler.playSound(sound);
		}

		return null;
	}
}