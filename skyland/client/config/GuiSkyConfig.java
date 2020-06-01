package skyland.client.config;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.core.Config;
import skyland.core.Skyland;

@SideOnly(Side.CLIENT)
public class GuiSkyConfig extends GuiConfig
{
	public GuiSkyConfig(GuiScreen parent)
	{
		super(parent, getConfigElements(), Skyland.MODID, false, false, I18n.format(Config.LANG_KEY + "title"));
	}

	private static List<IConfigElement> getConfigElements()
	{
		List<IConfigElement> list = Lists.newArrayList();
		Configuration config = Config.config;

		list.addAll(new ConfigElement(config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
		list.addAll(new ConfigElement(config.getCategory(Config.CATEGORY_DIMENSION)).getChildElements());

		return list;
	}
}