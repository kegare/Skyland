package skyland.client.config;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SkyGuiFactory implements IModGuiFactory
{
	@Override
	public void initialize(Minecraft mc) {}

	@Override
	public boolean hasConfigGui()
	{
		return true;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
	{
		return null;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen)
	{
		return new SkyConfigGui(parentScreen);
	}
}