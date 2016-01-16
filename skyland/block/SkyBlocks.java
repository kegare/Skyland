/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.registry.GameRegistry;
import skyland.core.Skyland;
import skyland.item.ItemSkyPortal;
import skyland.util.SkyUtils;

public class SkyBlocks
{
	public static final BlockSkyPortal sky_portal = new BlockSkyPortal();
	public static final BlockSkyriteOre skyrite_ore = new BlockSkyriteOre();
	public static final Block skyrite_block = new Block(Material.iron, MapColor.airColor).setHardness(5.5F).setResistance(10.0F).setStepSound(Block.soundTypeMetal).setUnlocalizedName("blockSkyrite").setCreativeTab(Skyland.tabSkyland);

	public static void register()
	{
		GameRegistry.registerBlock(sky_portal, ItemSkyPortal.class, "sky_portal");
		GameRegistry.registerBlock(skyrite_ore, "skyrite_ore");
		GameRegistry.registerBlock(skyrite_block, "skyrite_block");

		SkyUtils.registerOreDict(skyrite_ore, "oreSkyrite", "skyriteOre");
		SkyUtils.registerOreDict(skyrite_block, "blockSkyrite", "skyriteBlock");
	}
}