package com.kegare.skyland.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.config.Configuration;

public interface ISkylandAPI
{
	public String getVersion();

	public Configuration getConfig();

	public int getDimension();

	public WorldType getWorldType();

	public boolean isEntityInSkyland(Entity entity);

	public void teleportToSkyland(EntityPlayerMP player);
}