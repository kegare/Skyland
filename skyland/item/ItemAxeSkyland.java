package skyland.item;

import net.minecraft.item.ItemAxe;

public class ItemAxeSkyland extends ItemAxe
{
	public ItemAxeSkyland(ToolMaterial material)
	{
		super(ToolMaterial.DIAMOND);
		this.toolMaterial = material;
	}
}