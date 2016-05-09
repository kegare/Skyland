package skyland.core;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class SkySounds
{
	public static final SkySoundEvent skyland = new SkySoundEvent(new ResourceLocation(Skyland.MODID, "skyland"));
	public static final SkySoundEvent falling = new SkySoundEvent(new ResourceLocation(Skyland.MODID, "falling"));
	public static final SkySoundEvent sky_portal = new SkySoundEvent(new ResourceLocation(Skyland.MODID, "sky_portal"));

	public static void registerSounds()
	{
		GameRegistry.register(skyland);
		GameRegistry.register(falling);
		GameRegistry.register(sky_portal);
	}

	public static class SkySoundEvent extends SoundEvent
	{
		public SkySoundEvent(ResourceLocation soundName)
		{
			super(soundName);
			this.setRegistryName(soundName);
		}
	}
}