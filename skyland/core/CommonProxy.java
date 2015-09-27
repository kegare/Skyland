/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CommonProxy
{
	public void playSoundSkyJump() {}

	public EntityPlayer getPlayerEntityFromContext(MessageContext ctx)
	{
		return ctx.getServerHandler().playerEntity;
	}
}