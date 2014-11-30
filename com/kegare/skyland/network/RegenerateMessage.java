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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.kegare.skyland.client.gui.GuiRegeneration;
import com.kegare.skyland.world.WorldProviderSkyland;

public class RegenerateMessage implements IMessage, IMessageHandler<RegenerateMessage, IMessage>
{
	private boolean backup = true;

	public RegenerateMessage() {}

	public RegenerateMessage(boolean backup)
	{
		this.backup = backup;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		backup = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(backup);
	}

	@Override
	public IMessage onMessage(RegenerateMessage message, MessageContext ctx)
	{
		if (ctx.side.isClient())
		{
			FMLCommonHandler.instance().showGuiScreen(new GuiRegeneration(message.backup));
		}
		else
		{
			WorldProviderSkyland.regenerate(message.backup);
		}

		return null;
	}
}