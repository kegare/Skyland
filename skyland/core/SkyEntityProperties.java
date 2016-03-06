/*
 * Skyland
 *
 * Copyright (c) 2016 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.core;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants.NBT;

public class SkyEntityProperties implements IExtendedEntityProperties
{
	private static final String SKY_TAG = "Skyland";
	private static final Map<String, NBTTagCompound> dataMap = Maps.newHashMap();

	private final Entity entity;

	private int lastDim;
	private final Map<Integer, BlockPos> lastPos = Maps.newHashMap();

	public SkyEntityProperties(Entity entity)
	{
		this.entity = entity;
	}

	public static SkyEntityProperties get(Entity entity)
	{
		SkyEntityProperties ret = (SkyEntityProperties)entity.getExtendedProperties(SKY_TAG);

		if (ret == null)
		{
			ret = new SkyEntityProperties(entity);

			entity.registerExtendedProperties(SKY_TAG, ret);
		}

		return ret;
	}

	@Override
	public void init(Entity entity, World world) {}

	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		NBTTagCompound data = new NBTTagCompound();

		data.setInteger("LastDim", lastDim);

		NBTTagList list = new NBTTagList();

		for (Entry<Integer, BlockPos> entry : lastPos.entrySet())
		{
			NBTTagCompound nbt = new NBTTagCompound();
			BlockPos pos = entry.getValue();

			nbt.setInteger("Dim", entry.getKey());
			nbt.setInteger("X", pos.getX());
			nbt.setInteger("Y", pos.getY());
			nbt.setInteger("Z", pos.getZ());

			list.appendTag(nbt);
		}

		data.setTag("LastPos", list);

		if (compound == null)
		{
			dataMap.put(entity.getUniqueID().toString(), data);
		}
		else
		{
			compound.setTag(SKY_TAG, data);
		}
	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		NBTTagCompound data;

		if (compound == null)
		{
			data = dataMap.remove(entity.getUniqueID().toString());
		}
		else
		{
			data = compound.getCompoundTag(SKY_TAG);
		}

		if (data != null)
		{
			lastDim = data.getInteger("LastDim");
			lastPos.clear();

			NBTTagList list = data.getTagList("LastPos", NBT.TAG_COMPOUND);

			for (int i = 0; i < list.tagCount(); ++i)
			{
				NBTTagCompound nbt = list.getCompoundTagAt(i);

				lastPos.put(nbt.getInteger("Dim"), new BlockPos(nbt.getInteger("X"), nbt.getInteger("Y"), nbt.getInteger("Z")));
			}
		}
	}

	public int getLastDim()
	{
		return lastDim;
	}

	public void setLastDim(int dim)
	{
		lastDim = dim;
	}

	public BlockPos getLastPos(int dim)
	{
		return lastPos.get(dim);
	}

	public void setLastPos(int dim, BlockPos pos)
	{
		lastPos.put(dim, pos);
	}
}