/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MovingSoundSkyJump extends MovingSound
{
	public MovingSoundSkyJump()
	{
		super(new ResourceLocation("skyland:falling"));
		this.repeat = true;
		this.repeatDelay = 0;
	}

	@Override
	public void update()
	{
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.thePlayer;

		if (player == null || player.isDead || player.onGround || player.getEntityBoundingBox().minY >= 348.0D)
		{
			donePlaying = true;
		}
		else
		{
			xPosF = (float)player.posX;
			yPosF = (float)player.posY;
			zPosF = (float)player.posZ;

			if (MathHelper.sqrt_double(player.motionY * player.motionY) >= 0.01D)
			{
				volume = MathHelper.clamp_float((348.0F - yPosF) * 0.05F, 0.0F, 1.0F);
			}
			else
			{
				volume = 0.0F;
			}
		}
	}
}