/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.skyland.client;

import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.kegare.skyland.core.CommonProxy;
import com.kegare.skyland.core.Config;
import com.kegare.skyland.item.SkyItems;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void registerModels()
	{
		ItemModelMesher mesher = FMLClientHandler.instance().getClient().getRenderItem().getItemModelMesher();

		if (Config.recordSkyland)
		{
			mesher.register(SkyItems.record_skyland, 0, new ModelResourceLocation("skyland:record_skyland", "inventory"));
		}
	}
}