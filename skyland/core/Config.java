package skyland.core;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Lists;

import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import skyland.util.SkyLog;

public class Config
{
	public static final String LANG_KEY = "skyland.config.";

	public static Configuration config;

	public static boolean versionNotify;
	public static boolean skyborn;

	public static int dimension;
	public static boolean generateCaves;
	public static boolean generateLakes;

	public static final String CATEGORY_DIMENSION = "dimension";

	public static void loadConfig()
	{
		File file = new File(Loader.instance().getConfigDir(), "Skyland.cfg");

		config = new Configuration(file, true);

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

	public static void syncConfig()
	{
		if (config == null)
		{
			loadConfig();
		}

		String category = Configuration.CATEGORY_GENERAL;
		Property prop;
		String comment;
		List<String> propOrder = Lists.newArrayList();

		prop = config.get(category, "versionNotify", true);
		prop.setLanguageKey(LANG_KEY + category + "." + prop.getName());
		comment = I18n.translateToLocal(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, does not have to match client-side and server-side.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		versionNotify = prop.getBoolean(versionNotify);

		prop = config.get(category, "skyborn", false);
		prop.setLanguageKey(LANG_KEY + category + "." + prop.getName());
		comment = I18n.translateToLocal(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		skyborn = prop.getBoolean(skyborn);

		config.setCategoryPropertyOrder(category, propOrder);

		category = CATEGORY_DIMENSION;
		propOrder = Lists.newArrayList();

		prop = config.get(category, "dimension", -4);
		prop.setRequiresMcRestart(true);
		prop.setLanguageKey(LANG_KEY + category + "." + prop.getName());
		comment = I18n.translateToLocal(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		dimension = prop.getInt(dimension);

		prop = config.get(category, "generateCaves", true);
		prop.setLanguageKey(LANG_KEY + category + "." + prop.getName());
		comment = I18n.translateToLocal(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		generateCaves = prop.getBoolean(generateCaves);

		prop = config.get(category, "generateLakes", true);
		prop.setLanguageKey(LANG_KEY + category + "." + prop.getName());
		comment = I18n.translateToLocal(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		generateLakes = prop.getBoolean(generateLakes);

		config.setCategoryPropertyOrder(category, propOrder);

		if (config.hasChanged())
		{
			config.save();
		}
	}
}