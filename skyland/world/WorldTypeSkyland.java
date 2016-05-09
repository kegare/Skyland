package skyland.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkGenerator;

public class WorldTypeSkyland extends WorldType
{
	public WorldTypeSkyland()
	{
		super("skyland");
	}

	@Override
	public IChunkGenerator getChunkGenerator(World world, String generatorOptions)
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
	public int getSpawnFuzz(WorldServer world, MinecraftServer server)
	{
		return Math.min(2, server.getSpawnRadius(world));
	}

	@Override
	public float getCloudHeight()
	{
		return 1.5F;
	}
}