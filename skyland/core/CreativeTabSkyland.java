package skyland.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
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
	public ItemStack getTabIconItem()
	{
		return new ItemStack(SkyItems.SKY_FEATHER);
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