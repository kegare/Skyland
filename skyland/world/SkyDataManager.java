package skyland.world;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

public class SkyDataManager
{
	private long worldSeed = -1L;

	public SkyDataManager(@Nullable NBTTagCompound compound)
	{
		if (compound != null)
		{
			if (compound.hasKey("Seed", NBT.TAG_ANY_NUMERIC))
			{
				worldSeed = compound.getLong("Seed");
			}
		}
	}

	public NBTTagCompound getCompound()
	{
		NBTTagCompound compound = new NBTTagCompound();

		compound.setLong("Seed", worldSeed);

		return compound;
	}

	public long getWorldSeed()
	{
		return worldSeed;
	}

	public long getWorldSeed(long defaultSeed)
	{
		if (worldSeed == -1L)
		{
			setWorldSeed(defaultSeed);
		}

		return worldSeed;
	}

	public void setWorldSeed(long seed)
	{
		worldSeed = seed;
	}
}