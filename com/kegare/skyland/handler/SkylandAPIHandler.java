/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.skyland.handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.config.Configuration;

import com.kegare.skyland.api.ISkylandAPI;
import com.kegare.skyland.core.Config;
import com.kegare.skyland.core.Skyland;
import com.kegare.skyland.util.SkyUtils;
import com.kegare.skyland.util.Version;

public class SkylandAPIHandler implements ISkylandAPI
{
	@Override
	public String getVersion()
	{
		return Version.getCurrent();
	}

	@Override
	public Configuration getConfig()
	{
		return Config.config;
	}

	@Override
	public int getDimension()
	{
		return Config.dimensionSkyland;
	}

	@Override
	public WorldType getWorldType()
	{
		return Skyland.SKYLAND;
	}

	@Override
	public boolean isEntityInSkyland(Entity entity)
	{
		if (entity != null)
		{
			if (getWorldType() == null)
			{
				return entity.dimension == getDimension();
			}

			if (entity.worldObj.getWorldInfo().getTerrainType() == getWorldType())
			{
				return entity.dimension == 0;
			}
		}

		return false;
	}

	@Override
	public void teleportToSkyland(EntityPlayerMP player)
	{
		SkyUtils.teleportPlayer(player, getDimension());
	}
}