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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.kegare.skyland.core.Config;
import com.kegare.skyland.world.WorldProviderSkyland;

public class DimSyncMessage implements IMessage, IMessageHandler<DimSyncMessage, IMessage>
{
	private int dimensionId;
	private NBTTagCompound dimensionData;

	public DimSyncMessage() {}

	public DimSyncMessage(int dim, NBTTagCompound data)
	{
		this.dimensionId = dim;
		this.dimensionData = data;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		dimensionId = buf.readInt();
		dimensionData = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(dimensionId);
		ByteBufUtils.writeTag(buf, dimensionData);
	}

	@Override
	public IMessage onMessage(DimSyncMessage message, MessageContext ctx)
	{
		Config.dimensionSkyland = message.dimensionId;
		WorldProviderSkyland.loadDimData(message.dimensionData);

		return null;
	}
}