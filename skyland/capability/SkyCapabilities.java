package skyland.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skyland.core.Skyland;
import skyland.stats.PortalCache;

public class SkyCapabilities
{
	@CapabilityInject(PortalCache.class)
	public static Capability<PortalCache> PORTAL_CACHE = null;

	public static final ResourceLocation PORTAL_CACHE_ID = new ResourceLocation(Skyland.MODID, "PortalCache");

	public static void registerCapabilities()
	{
		CapabilityPortalCache.register();

		MinecraftForge.EVENT_BUS.register(new SkyCapabilities());
	}

	public static <T> boolean isValid(Capability<T> capability)
	{
		return capability != null;
	}

	public static <T> boolean hasCapability(ICapabilityProvider entry, Capability<T> capability)
	{
		return entry != null && isValid(capability) && entry.hasCapability(capability, null);
	}

	public static <T> T getCapability(ICapabilityProvider entry, Capability<T> capability)
	{
		return hasCapability(entry, capability) ? entry.getCapability(capability, null) : null;
	}

	@SubscribeEvent
	public void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		event.addCapability(PORTAL_CACHE_ID, new CapabilityPortalCache());
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event)
	{
		EntityPlayer player = event.getEntityPlayer();
		EntityPlayer original = event.getOriginal();

		PortalCache originalPortalCache = getCapability(original, PORTAL_CACHE);
		PortalCache portalCache = getCapability(player, PORTAL_CACHE);

		if (originalPortalCache != null && portalCache != null)
		{
			NBTTagCompound nbt = new NBTTagCompound();

			originalPortalCache.writeToNBT(nbt);
			portalCache.readFromNBT(nbt);
		}
	}
}