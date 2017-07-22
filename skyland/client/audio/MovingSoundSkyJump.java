package skyland.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.core.SkySounds;

@SideOnly(Side.CLIENT)
public class MovingSoundSkyJump extends MovingSound
{
	public static MovingSoundSkyJump prevSound;

	public MovingSoundSkyJump()
	{
		super(SkySounds.FALLING, SoundCategory.PLAYERS);
		this.repeat = true;
		this.repeatDelay = 0;
	}

	@Override
	public void update()
	{
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.player;

		if (player == null || player.isDead || player.onGround || player.getEntityBoundingBox().minY >= 348.0D)
		{
			donePlaying = true;
		}
		else
		{
			xPosF = (float)player.posX;
			yPosF = (float)player.posY;
			zPosF = (float)player.posZ;

			if (MathHelper.sqrt(player.motionY * player.motionY) >= 0.01D)
			{
				volume = MathHelper.clamp((348.0F - yPosF) * 0.05F, 0.0F, 1.0F);
			}
			else
			{
				volume = 0.0F;
			}
		}
	}
}