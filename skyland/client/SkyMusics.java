package skyland.client;

import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraftforge.client.EnumHelperClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.core.SkySounds;

@SideOnly(Side.CLIENT)
public class SkyMusics
{
	public static MusicType SKYLAND;

	public static void registerMusics()
	{
		SKYLAND = EnumHelperClient.addMusicType("SKYLAND", SkySounds.SKYLAND, 12000, 24000);
	}
}