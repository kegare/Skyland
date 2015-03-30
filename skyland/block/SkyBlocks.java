/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.block;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class SkyBlocks
{
	public static final BlockSkyPortal sky_portal = new BlockSkyPortal();

	public static void register()
	{
		GameRegistry.registerBlock(sky_portal, "sky_portal");
	}
}