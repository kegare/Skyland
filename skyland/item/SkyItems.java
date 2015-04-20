/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.item;

import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import skyland.core.Skyland;
import skyland.util.SkyUtils;

public class SkyItems
{
	public static final ItemSkyFeather sky_feather = new ItemSkyFeather();
	public static final ItemRecordSkyland record_skyland = new ItemRecordSkyland();
	public static final Item skyrite = new Item().setUnlocalizedName("skyrite").setCreativeTab(Skyland.tabSkyland);

	public static final ToolMaterial SKYRITE = EnumHelper.addToolMaterial("SKYRITE", 3, 3000, 6.0F, 2.5F, 12).setRepairItem(new ItemStack(skyrite));

	public static final ItemSword skyrite_sword = (ItemSword)new ItemSword(SKYRITE).setUnlocalizedName("swordSkyrite").setCreativeTab(Skyland.tabSkyland);
	public static final ItemSpade skyrite_shovel = (ItemSpade)new ItemSpade(SKYRITE).setUnlocalizedName("shovelSkyrite").setCreativeTab(Skyland.tabSkyland);
	public static final ItemPickaxe skyrite_pickaxe = (ItemPickaxe)new ItemPickaxeSkyland(SKYRITE).setUnlocalizedName("pickaxeSkyrite").setCreativeTab(Skyland.tabSkyland);
	public static final ItemAxe skyrite_axe = (ItemAxe)new ItemAxeSkyland(SKYRITE).setUnlocalizedName("axeSkyrite").setCreativeTab(Skyland.tabSkyland);
	public static final ItemHoe skyrite_hoe = (ItemHoe)new ItemHoe(SKYRITE).setUnlocalizedName("hoeSkyrite").setCreativeTab(Skyland.tabSkyland);

	public static void register()
	{
		GameRegistry.registerItem(sky_feather, "sky_feather");
		GameRegistry.registerItem(record_skyland, "record_skyland");
		GameRegistry.registerItem(skyrite, "skyrite");
		GameRegistry.registerItem(skyrite_sword, "skyrite_sword");
		GameRegistry.registerItem(skyrite_shovel, "skyrite_shovel");
		GameRegistry.registerItem(skyrite_pickaxe, "skyrite_pickaxe");
		GameRegistry.registerItem(skyrite_axe, "skyrite_axe");
		GameRegistry.registerItem(skyrite_hoe, "skyrite_hoe");

		SkyUtils.registerOreDict(sky_feather, "feather", "skyFeather");
		SkyUtils.registerOreDict(record_skyland, "record");
		SkyUtils.registerOreDict(skyrite, "skyrite", "gemSkyrite");
		SkyUtils.registerOreDict(skyrite_sword, "sword", "swordSkyrite");
		SkyUtils.registerOreDict(skyrite_shovel, "shovel", "shovelSkyrite");
		SkyUtils.registerOreDict(skyrite_pickaxe, "pickaxe", "pickaxeSkyrite");
		SkyUtils.registerOreDict(skyrite_axe, "axe", "axeSkyrite");
		SkyUtils.registerOreDict(skyrite_hoe, "hoe", "hoeSkyrite");
	}
}