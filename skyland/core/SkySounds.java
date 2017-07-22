package skyland.core;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class SkySounds
{
	public static final SkySoundEvent SKYLAND = new SkySoundEvent(new ResourceLocation(Skyland.MODID, "skyland"));
	public static final SkySoundEvent FALLING = new SkySoundEvent(new ResourceLocation(Skyland.MODID, "falling"));
	public static final SkySoundEvent SKY_PORTAL = new SkySoundEvent(new ResourceLocation(Skyland.MODID, "sky_portal"));

	public static void registerSounds(IForgeRegistry<SoundEvent> registry)
	{
		registry.register(SKYLAND);
		registry.register(FALLING);
		registry.register(SKY_PORTAL);
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