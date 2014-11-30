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
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.kegare.skyland.client.gui.GuiRegeneration;

public class RegenerateProgressMessage implements IMessage, IMessageHandler<RegenerateProgressMessage, IMessage>
{
	private int task = -1;

	public RegenerateProgressMessage() {}

	public RegenerateProgressMessage(int task)
	{
		this.task = task;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		task = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(task);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(RegenerateProgressMessage message, MessageContext ctx)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();

		if (mc.currentScreen != null && mc.currentScreen instanceof GuiRegeneration)
		{
			((GuiRegeneration)mc.currentScreen).updateProgress(message.task);
		}

		return null;
	}
}