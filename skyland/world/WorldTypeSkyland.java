/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldTypeSkyland extends WorldType
{
	public WorldTypeSkyland()
	{
		super("skyland");
	}

	@Override
	public IChunkProvider getChunkGenerator(World world, String generatorOptions)
	{
		return new ChunkProviderSkyland(world);
	}

	@Override
	public double getHorizon(World world)
	{
		return 0.0D;
	}

	@Override
	public double voidFadeMagnitude()
	{
		return 1.0D;
	}

	@Override
	public int getSpawnFuzz()
	{
		return 2;
	}

	@Override
	public float getCloudHeight()
	{
		return 1.5F;
	}
}