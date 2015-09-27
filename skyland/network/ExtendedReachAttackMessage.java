/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import skyland.core.Skyland;
import skyland.util.IExtendedReach;

public class ExtendedReachAttackMessage implements IMessage, IMessageHandler<ExtendedReachAttackMessage, IMessage>
{
	private int entityId;

	public ExtendedReachAttackMessage() {}

	public ExtendedReachAttackMessage(int entity)
	{
		this.entityId = entity;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		entityId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(entityId);
	}

	@Override
	public IMessage onMessage(final ExtendedReachAttackMessage message, MessageContext ctx)
	{
		final EntityPlayerMP thePlayer = (EntityPlayerMP)Skyland.proxy.getPlayerEntityFromContext(ctx);

		thePlayer.getServerForPlayer().addScheduledTask(
			new Runnable()
			{
				@Override
				public void run()
				{
					Entity theEntity = thePlayer.worldObj.getEntityByID(message.entityId);

					if (thePlayer.getCurrentEquippedItem() == null)
					{
						return;
					}

					if (thePlayer.getCurrentEquippedItem().getItem() instanceof IExtendedReach)
					{
						IExtendedReach extended = (IExtendedReach)thePlayer.getCurrentEquippedItem().getItem();

						double distanceSq = thePlayer.getDistanceSqToEntity(theEntity);
						double reachSq =extended.getReach() * extended.getReach();

						if (reachSq >= distanceSq)
						{
							thePlayer.attackTargetEntityWithCurrentItem(theEntity);
						}
					}

					return;
				}
			}
		);

		return null;
	}
}