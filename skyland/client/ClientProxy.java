/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.client;

import skyland.client.audio.MovingSoundSkyJump;
import skyland.core.CommonProxy;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void playSoundSkyJump()
	{
		FMLClientHandler.instance().getClient().getSoundHandler().playSound(new MovingSoundSkyJump());
	}
}