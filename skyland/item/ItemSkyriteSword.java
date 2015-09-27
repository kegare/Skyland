/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.item;

import net.minecraft.item.ItemSword;
import skyland.util.IExtendedReach;

public class ItemSkyriteSword extends ItemSword implements IExtendedReach
{
	public ItemSkyriteSword(ToolMaterial material)
	{
		super(material);
	}

	@Override
	public float getReach()
	{
		return 10.0F;
	}
}