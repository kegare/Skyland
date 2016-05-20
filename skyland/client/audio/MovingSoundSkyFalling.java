package skyland.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.core.SkySounds;

@SideOnly(Side.CLIENT)
public class MovingSoundSkyFalling extends MovingSound
{
	public MovingSoundSkyFalling()
	{
		super(SkySounds.falling, SoundCategory.PLAYERS);
		this.repeat = true;
		this.repeatDelay = 0;
	}

	@Override
	public void update()
	{
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.thePlayer;

		if (player == null || player.isDead || player.onGround)
		{
			donePlaying = true;

			if (player != null && player.onGround)
			{
				ISound sound = PositionedSoundRecord.getRecordSoundRecord(SoundEvents.ENTITY_HOSTILE_SMALL_FALL, (float)player.posX, (float)player.posY - (float)player.getYOffset(), (float)player.posZ);

				mc.getSoundHandler().playSound(sound);
			}
		}
		else
		{
			xPosF = (float)player.posX;
			yPosF = (float)player.posY;
			zPosF = (float)player.posZ;

			if (MathHelper.sqrt_double(player.motionY * player.motionY) >= 0.01D)
			{
				volume = MathHelper.clamp_float((yPosF - player.worldObj.getPrecipitationHeight(player.getPosition()).getY()) * 0.05F, 0.0F, 1.0F);
			}
			else
			{
				volume = 0.0F;
			}
		}
	}
}