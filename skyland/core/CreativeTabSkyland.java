package skyland.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.item.SkyItems;

public class CreativeTabSkyland extends CreativeTabs
{
	public CreativeTabSkyland()
	{
		super("skyland");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Item getTabIconItem()
	{
		return SkyItems.SKYRITE;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTabLabel()
	{
		return "Skyland";
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTranslatedTabLabel()
	{
		return getTabLabel();
	}
}