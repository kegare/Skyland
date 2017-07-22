package skyland.item;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import skyland.core.Skyland;

public class ItemArmorSkyrite extends ItemArmor
{
	private final String renderName;

	public ItemArmorSkyrite(ArmorMaterial material, String name, String renderName, EntityEquipmentSlot slot)
	{
		super(material, 3, slot);
		this.setUnlocalizedName(name);
		this.setCreativeTab(Skyland.TAB_SKYLAND);
		this.renderName = renderName;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type)
	{
		return String.format("skyland:textures/models/armor/%s_layer_%d.png", renderName, slot == EntityEquipmentSlot.LEGS ? 2 : 1);
	}
}