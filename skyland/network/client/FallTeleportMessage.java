package skyland.network.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.client.audio.MovingSoundSkyFalling;
import skyland.client.handler.ClientEventHooks;

public class FallTeleportMessage implements IClientMessage<FallTeleportMessage, IMessage>
{
	@SideOnly(Side.CLIENT)
	@Override
	public IMessage process(Minecraft mc)
	{
		if (MovingSoundSkyFalling.prevSound == null || MovingSoundSkyFalling.prevSound.isDonePlaying())
		{
			MovingSoundSkyFalling sound = new MovingSoundSkyFalling();

			mc.getSoundHandler().playSound(sound);

			MovingSoundSkyFalling.prevSound = sound;

			ClientEventHooks.fallCancelable = true;
		}

		return null;
	}
}