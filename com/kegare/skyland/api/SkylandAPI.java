package com.kegare.skyland.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;

/**
 * NOTE: Do NOT access to this class fields.
 * You should use this API from this class methods.
 */
public final class SkylandAPI
{
	public static ISkylandAPI instance;

	private SkylandAPI() {}

	/**
	 * Returns the current mod version of Skyland mod.
	 */
	public static String getVersion()
	{
		return instance == null ? "" : instance.getVersion();
	}

	/**
	 * Returns the configuration of Skyland mod.
	 */
	public static Configuration getConfig()
	{
		return instance == null ? null : instance.getConfig();
	}

	/**
	 * Returns the dimension id of the Skyland dimension.
	 */
	public static int getDimension()
	{
		return instance == null ? DimensionManager.getNextFreeDimId() : instance.getDimension();
	}

	/**
	 * Returns the world type of Skyland.
	 * @return <tt>null</tt> if the world type is not enabled.
	 */
	public static WorldType getWorldType()
	{
		return instance == null ? null : instance.getWorldType();
	}

	/**
	 * Checks if entity is in Skyland.
	 * @param entity The entity
	 * @return <tt>true</tt> if the entity is in Skyland.
	 */
	public static boolean isEntityInSkyland(Entity entity)
	{
		return instance != null && instance.isEntityInSkyland(entity);
	}

	/**
	 * Teleports the player to Skyland.
	 * @param player The player
	 */
	public static void teleportToSkyland(EntityPlayerMP player)
	{
		if (instance != null)
		{
			instance.teleportToSkyland(player);
		}
	}
}