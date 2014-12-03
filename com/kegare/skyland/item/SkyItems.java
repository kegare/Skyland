/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.skyland.item;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import com.kegare.skyland.core.Config;

public class SkyItems
{
	public static final ItemRecordSkyland record_skyland = new ItemRecordSkyland();

	public static void registerItems()
	{
		if (Config.recordSkyland)
		{
			GameRegistry.registerItem(record_skyland, "record_skyland");

			OreDictionary.registerOre("record", record_skyland);
		}
	}
}