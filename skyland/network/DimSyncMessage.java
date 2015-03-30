/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.network;

import skyland.world.WorldProviderSkyland;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DimSyncMessage implements IMessage, IMessageHandler<DimSyncMessage, IMessage>
{
	private NBTTagCompound data;

	public DimSyncMessage() {}

	public DimSyncMessage(NBTTagCompound data)
	{
		this.data = data;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		data = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, data);
	}

	@Override
	public IMessage onMessage(DimSyncMessage message, MessageContext ctx)
	{
		WorldProviderSkyland.loadDimData(message.data);

		return null;
	}
}