/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.item;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class SkyItems
{
	public static final ItemSkyFeather sky_feather = new ItemSkyFeather();
	public static final ItemRecordSkyland record_skyland = new ItemRecordSkyland();

	public static void register()
	{
		GameRegistry.registerItem(sky_feather, "sky_feather");
		GameRegistry.registerItem(record_skyland, "record_skyland");

		OreDictionary.registerOre("feather", sky_feather);
		OreDictionary.registerOre("record", record_skyland);
	}
}