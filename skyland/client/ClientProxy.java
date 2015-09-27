/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.client.audio.MovingSoundSkyJump;
import skyland.core.CommonProxy;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void playSoundSkyJump()
	{
		FMLClientHandler.instance().getClient().getSoundHandler().playSound(new MovingSoundSkyJump());
	}

	@Override
	public EntityPlayer getPlayerEntityFromContext(MessageContext ctx)
	{
		return ctx.side.isClient() ? FMLClientHandler.instance().getClientPlayerEntity() : super.getPlayerEntityFromContext(ctx);
	}
}