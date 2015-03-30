/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.client.config;

import java.util.List;

import skyland.api.SkylandAPI;
import skyland.core.Skyland;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;

@SideOnly(Side.CLIENT)
public class SkyConfigGui extends GuiConfig
{
	public SkyConfigGui(GuiScreen parent)
	{
		super(parent, getConfigElements(), Skyland.MODID, false, false, I18n.format(Skyland.CONFIG_LANG + "title"));
	}

	private static List<IConfigElement> getConfigElements()
	{
		List<IConfigElement> list = Lists.newArrayList();

		list.addAll(new ConfigElement(SkylandAPI.getConfig().getCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
		list.addAll(new ConfigElement(SkylandAPI.getConfig().getCategory("items")).getChildElements());
		list.addAll(new ConfigElement(SkylandAPI.getConfig().getCategory("skyland")).getChildElements());
		list.addAll(new ConfigElement(SkylandAPI.getConfig().getCategory("options")).getChildElements());

		return list;
	}
}