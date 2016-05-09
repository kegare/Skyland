package skyland.capability;

import java.util.concurrent.Callable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import skyland.stats.IPortalCache;
import skyland.stats.PortalCache;

public class CapabilityPortalCache implements ICapabilitySerializable<NBTTagCompound>
{
	private final PortalCache cache;

	public CapabilityPortalCache()
	{
		this.cache = new PortalCache();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == SkyCapabilities.PORTAL_CACHE;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (capability == SkyCapabilities.PORTAL_CACHE)
		{
			return SkyCapabilities.PORTAL_CACHE.cast(cache);
		}

		return null;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		if (SkyCapabilities.PORTAL_CACHE != null)
		{
			return (NBTTagCompound)SkyCapabilities.PORTAL_CACHE.getStorage().writeNBT(SkyCapabilities.PORTAL_CACHE, cache, null);
		}

		return new NBTTagCompound();
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		if (SkyCapabilities.PORTAL_CACHE != null)
		{
			SkyCapabilities.PORTAL_CACHE.getStorage().readNBT(SkyCapabilities.PORTAL_CACHE, cache, null, nbt);
		}
	}

	public static void register()
	{
		CapabilityManager.INSTANCE.register(IPortalCache.class,
			new Capability.IStorage<IPortalCache>()
			{
				@Override
				public NBTBase writeNBT(Capability<IPortalCache> capability, IPortalCache instance, EnumFacing side)
				{
					NBTTagCompound nbt = new NBTTagCompound();

					instance.writeToNBT(nbt);

					return nbt;
				}

				@Override
				public void readNBT(Capability<IPortalCache> capability, IPortalCache instance, EnumFacing side, NBTBase nbt)
				{
					instance.readFromNBT((NBTTagCompound)nbt);
				}
			},
			new Callable<PortalCache>()
			{
				@Override
				public PortalCache call() throws Exception
				{
					return new PortalCache();
				}
			}
		);
	}
}