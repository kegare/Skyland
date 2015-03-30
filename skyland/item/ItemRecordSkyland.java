/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.item;

import net.minecraft.item.ItemRecord;
import net.minecraft.util.ResourceLocation;

public class ItemRecordSkyland extends ItemRecord
{
	public ItemRecordSkyland()
	{
		super("skyland");
		this.setUnlocalizedName("record");
	}

	@Override
	public ResourceLocation getRecordResource(String name)
	{
		return new ResourceLocation("skyland", "record.skyland");
	}
}