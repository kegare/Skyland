/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.skyland.core;

import java.io.File;
import java.util.List;

import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Lists;
import com.kegare.skyland.util.SkyLog;

public class Config
{
	public static Configuration config;

	public static boolean versionNotify;

	public static int dimensionSkyland;
	public static boolean generateCaves;
	public static boolean generateLakes;

	public static boolean skyborn;

	public static void syncConfig()
	{
		if (config == null)
		{
			File file = new File(Loader.instance().getConfigDir(), "Skyland.cfg");
			config = new Configuration(file);

			try
			{
				config.load();
			}
			catch (Exception e)
			{
				File dest = new File(file.getParentFile(), file.getName() + ".bak");

				if (dest.exists())
				{
					dest.delete();
				}

				file.renameTo(dest);

				SkyLog.log(Level.ERROR, e, "A critical error occured reading the " + file.getName() + " file, defaults will be used - the invalid file is backed up at " + dest.getName());
			}
		}

		String category = Configuration.CATEGORY_GENERAL;
		Property prop;
		List<String> propOrder = Lists.newArrayList();

		prop = config.get(category, "versionNotify", true);
		prop.setLanguageKey(Skyland.CONFIG_LANG + category + "." + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		versionNotify = prop.getBoolean(versionNotify);

		config.setCategoryPropertyOrder(category, propOrder);

		category = "skyland";
		prop = config.get(category, "dimensionSkyland", -4);
		prop.setRequiresMcRestart(true).setLanguageKey(Skyland.CONFIG_LANG + category + "." + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + ", default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		dimensionSkyland = MathHelper.clamp_int(prop.getInt(dimensionSkyland), Integer.parseInt(prop.getMinValue()), Integer.parseInt(prop.getMaxValue()));
		prop = config.get(category, "generateCaves", true);
		prop.setLanguageKey(Skyland.CONFIG_LANG + category + "." + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		generateCaves = prop.getBoolean(generateCaves);
		prop = config.get(category, "generateLakes", true);
		prop.setLanguageKey(Skyland.CONFIG_LANG + category + "." + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		generateLakes = prop.getBoolean(generateLakes);

		config.setCategoryPropertyOrder(category, propOrder);

		category = "options";
		prop = config.get(category, "skyborn", false);
		prop.setLanguageKey(Skyland.CONFIG_LANG + category + "." + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		skyborn = prop.getBoolean(skyborn);

		config.setCategoryPropertyOrder(category, propOrder);

		if (config.hasChanged())
		{
			config.save();
		}
	}
}