/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.core;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.util.List;

import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import org.apache.logging.log4j.Level;

import skyland.api.SkylandAPI;
import skyland.util.SkyLog;
import skyland.world.WorldProviderSkyland;
import skyland.world.WorldTypeSkyland;

import com.google.common.collect.Lists;

public class Config implements IMessage, IMessageHandler<Config, IMessage>
{
	public static Configuration config;

	public static boolean versionNotify;

	public static int dimensionSkyland;
	public static boolean generateCaves;
	public static boolean generateLakes;

	public static boolean skyborn;

	public static void refreshDimension(int id)
	{
		int old = dimensionSkyland;
		dimensionSkyland = id;

		if (old != 0 && old != id && DimensionManager.isDimensionRegistered(old))
		{
			DimensionManager.unregisterProviderType(old);
			DimensionManager.unregisterDimension(old);

			SkyLog.fine("Unregister the dimension (" + old + ")");
		}

		if (id == 0)
		{
			if (SkylandAPI.getWorldType() == null)
			{
				try
				{
					Skyland.SKYLAND = new WorldTypeSkyland();

					SkyLog.fine("Register the world type of Skyland (" + SkylandAPI.getWorldType().getWorldTypeID() + ")");
				}
				catch (IllegalArgumentException e)
				{
					SkyLog.log(Level.ERROR, e, "An error occurred trying to register the world type of Skyland");
				}
			}
		}
		else if (old != id)
		{
			if (old != 0 && DimensionManager.isDimensionRegistered(id))
			{
				id = old;
			}

			if (DimensionManager.registerProviderType(id, WorldProviderSkyland.class, true))
			{
				DimensionManager.registerDimension(id, id);

				SkyLog.fine("Register the Skyland dimension (" + id + ")");
			}

			if (SkylandAPI.getWorldType() != null)
			{
				id = SkylandAPI.getWorldType().getWorldTypeID();
				Skyland.SKYLAND = null;
				WorldType.worldTypes[id] = null;

				SkyLog.fine("Unregister the world type of Skyland (" + id + ")");
			}
		}
	}

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
		propOrder = Lists.newArrayList();

		prop = config.get(category, "dimensionSkyland", -4);
		prop.setLanguageKey(Skyland.CONFIG_LANG + category + "." + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + ", default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		refreshDimension(MathHelper.clamp_int(prop.getInt(dimensionSkyland), Integer.parseInt(prop.getMinValue()), Integer.parseInt(prop.getMaxValue())));
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
		propOrder = Lists.newArrayList();

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

	@Override
	public void fromBytes(ByteBuf buf)
	{
		dimensionSkyland = buf.readInt();
		generateCaves = buf.readBoolean();
		generateLakes = buf.readBoolean();
		skyborn = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(dimensionSkyland);
		buf.writeBoolean(generateCaves);
		buf.writeBoolean(generateLakes);
		buf.writeBoolean(skyborn);
	}

	@Override
	public IMessage onMessage(Config message, MessageContext ctx)
	{
		refreshDimension(dimensionSkyland);

		return null;
	}
}