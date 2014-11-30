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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.kegare.skyland.handler.SkyEventHooks;

public class FallTeleportMessage implements IMessage, IMessageHandler<FallTeleportMessage, IMessage>
{
	private String uuid;

	public FallTeleportMessage() {}

	public FallTeleportMessage(EntityPlayer player)
	{
		this.uuid = player.getUniqueID().toString();
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		uuid = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, uuid);
	}

	@Override
	public IMessage onMessage(FallTeleportMessage message, MessageContext ctx)
	{
		SkyEventHooks.fallTeleportPlayers.get().add(message.uuid);

		return null;
	}
}