package skyland.stats;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import skyland.capability.SkyCapabilities;

public class PortalCache
{
	private final Map<ResourceLocation, DimensionType> lastDim = Maps.newHashMap();
	private final Table<ResourceLocation, DimensionType, BlockPos> lastPos = HashBasedTable.create();

	private Vec3d lastPortalVec;
	private EnumFacing teleportDirection;

	public DimensionType getLastDim(ResourceLocation key)
	{
		return getLastDim(key, DimensionType.OVERWORLD);
	}

	public DimensionType getLastDim(ResourceLocation key, DimensionType nullDefault)
	{
		return lastDim.getOrDefault(key, nullDefault);
	}

	public void setLastDim(ResourceLocation key, DimensionType type)
	{
		lastDim.put(key, type);
	}

	public BlockPos getLastPos(ResourceLocation key, DimensionType type)
	{
		return lastPos.get(key, type);
	}

	public BlockPos getLastPos(ResourceLocation key, DimensionType type, BlockPos pos)
	{
		return ObjectUtils.defaultIfNull(getLastPos(key, type), ObjectUtils.defaultIfNull(pos, BlockPos.ORIGIN));
	}

	public boolean hasLastPos(ResourceLocation key, DimensionType type)
	{
		return lastPos.contains(key, type);
	}

	public void setLastPos(ResourceLocation key, DimensionType type, BlockPos pos)
	{
		if (pos == null)
		{
			lastPos.remove(key, type);
		}
		else
		{
			lastPos.put(key, type, pos);
		}
	}

	public void clearLastPos(ResourceLocation key, DimensionType type)
	{
		for (Cell<ResourceLocation, DimensionType, BlockPos> entry : lastPos.cellSet())
		{
			if ((key == null || entry.getRowKey().equals(key)) && entry.getColumnKey() == type)
			{
				lastPos.remove(entry.getRowKey(), entry.getColumnKey());
			}
		}
	}

	public Vec3d getLastPortalVec()
	{
		return lastPortalVec;
	}

	public void setLastPortalVec(Vec3d vec)
	{
		lastPortalVec = vec;
	}

	public EnumFacing getTeleportDirection()
	{
		return teleportDirection;
	}

	public void setTeleportDirection(EnumFacing direction)
	{
		teleportDirection = direction;
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList tagList = new NBTTagList();

		for (Entry<ResourceLocation, DimensionType> entry : lastDim.entrySet())
		{
			ResourceLocation key = entry.getKey();
			DimensionType type = entry.getValue();

			if (key != null && type != null)
			{
				NBTTagCompound tag = new NBTTagCompound();

				tag.setString("Key", key.toString());
				tag.setInteger("Dim", type.getId());

				tagList.appendTag(tag);
			}
		}

		nbt.setTag("LastDim", tagList);

		tagList = new NBTTagList();

		for (Cell<ResourceLocation, DimensionType, BlockPos> entry : lastPos.cellSet())
		{
			ResourceLocation key = entry.getRowKey();
			DimensionType type = entry.getColumnKey();
			BlockPos pos = entry.getValue();

			if (key != null && type != null && pos != null)
			{
				NBTTagCompound tag = NBTUtil.createPosTag(pos);

				tag.setString("Key", key.toString());
				tag.setInteger("Dim", type.getId());

				tagList.appendTag(tag);
			}
		}

		nbt.setTag("LastPos", tagList);
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		NBTTagList tagList = nbt.getTagList("LastDim", NBT.TAG_COMPOUND);

		if (tagList != null && tagList.tagCount() > 0)
		{
			for (int i = 0; i < tagList.tagCount(); ++i)
			{
				NBTTagCompound tag = tagList.getCompoundTagAt(i);
				DimensionType type = null;

				try
				{
					type = DimensionType.getById(tag.getInteger("Dim"));
				}
				catch (IllegalArgumentException e)
				{
					continue;
				}

				if (type != null && tag.hasKey("Key", NBT.TAG_STRING))
				{
					lastDim.put(new ResourceLocation(tag.getString("Key")), type);
				}
			}
		}

		tagList = nbt.getTagList("LastPos", NBT.TAG_COMPOUND);

		for (int i = 0; i < tagList.tagCount(); ++i)
		{
			NBTTagCompound tag = tagList.getCompoundTagAt(i);
			DimensionType type = null;

			try
			{
				type = DimensionType.getById(tag.getInteger("Dim"));
			}
			catch (IllegalArgumentException e)
			{
				continue;
			}

			if (type != null && tag.hasKey("Key", NBT.TAG_STRING))
			{
				lastPos.put(new ResourceLocation(tag.getString("Key")), type, NBTUtil.getPosFromTag(tag));
			}
		}
	}

	public static PortalCache get(ICapabilityProvider provider)
	{
		return ObjectUtils.defaultIfNull(SkyCapabilities.getCapability(provider, SkyCapabilities.PORTAL_CACHE), new PortalCache());
	}
}