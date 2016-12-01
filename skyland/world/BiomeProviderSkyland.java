package skyland.world;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Biomes;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class BiomeProviderSkyland extends BiomeProvider
{
	private GenLayer genBiomes;
	private GenLayer biomeIndexLayer;

	private final BiomeCache biomeCache;
	private final List<Biome> biomesToSpawnIn;

	protected BiomeProviderSkyland()
	{
		this.biomeCache = new BiomeCache(this);
		this.biomesToSpawnIn = Lists.newArrayList(allowedBiomes);
	}

	public BiomeProviderSkyland(long seed, WorldType worldType, String options)
	{
		this();
		GenLayer[] layers = GenLayer.initializeAllBiomeGenerators(seed, worldType, options);
		layers = getModdedBiomeGenerators(worldType, seed, layers);
		this.genBiomes = layers[0];
		this.biomeIndexLayer = layers[1];
	}

	@Override
	public List<Biome> getBiomesToSpawnIn()
	{
		return biomesToSpawnIn;
	}

	@Override
	public Biome getBiomeGenerator(BlockPos pos, Biome biome)
	{
		return biomeCache.getBiome(pos.getX(), pos.getZ(), biome);
	}

	@Override
	public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height)
	{
		IntCache.resetIntCache();

		if (biomes == null || biomes.length < width * height)
		{
			biomes = new Biome[width * height];
		}

		int[] aint = genBiomes.getInts(x, z, width, height);

		try
		{
			for (int i = 0; i < width * height; ++i)
			{
				biomes[i] = Biome.getBiome(aint[i], Biomes.DEFAULT);
			}

			return biomes;
		}
		catch (Throwable e)
		{
			CrashReport report = CrashReport.makeCrashReport(e, "Invalid Biome id");
			CrashReportCategory category = report.makeCategory("RawBiomeBlock");

			category.addCrashSection("biomes[] size", Integer.valueOf(biomes.length));
			category.addCrashSection("x", Integer.valueOf(x));
			category.addCrashSection("z", Integer.valueOf(z));
			category.addCrashSection("w", Integer.valueOf(width));
			category.addCrashSection("h", Integer.valueOf(height));

			throw new ReportedException(report);
		}
	}

	@Override
	public Biome[] getBiomeGenAt(@Nullable Biome[] biomes, int x, int z, int width, int length, boolean cache)
	{
		IntCache.resetIntCache();

		if (biomes == null || biomes.length < width * length)
		{
			biomes = new Biome[width * length];
		}

		if (cache && width == 16 && length == 16 && (x & 15) == 0 && (z & 15) == 0)
		{
			Biome[] cachedBiomes = biomeCache.getCachedBiomes(x, z);
			System.arraycopy(cachedBiomes, 0, biomes, 0, width * length);

			return biomes;
		}
		else
		{
			int[] aint = biomeIndexLayer.getInts(x, z, width, length);

			for (int i = 0; i < width * length; ++i)
			{
				biomes[i] = Biome.getBiome(aint[i], Biomes.DEFAULT);
			}

			return biomes;
		}
	}

	@Override
	public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed)
	{
		IntCache.resetIntCache();

		int i = x - radius >> 2;
		int j = z - radius >> 2;
		int k = x + radius >> 2;
		int l = z + radius >> 2;
		int i1 = k - i + 1;
		int j1 = l - j + 1;
		int[] aint = genBiomes.getInts(i, j, i1, j1);

		try
		{
			for (int k1 = 0; k1 < i1 * j1; ++k1)
			{
				Biome biome = Biome.getBiome(aint[k1]);

				if (!allowed.contains(biome))
				{
					return false;
				}
			}

			return true;
		}
		catch (Throwable throwable)
		{
			CrashReport report = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
			CrashReportCategory category = report.makeCategory("Layer");

			category.addCrashSection("Layer", genBiomes.toString());
			category.addCrashSection("x", Integer.valueOf(x));
			category.addCrashSection("z", Integer.valueOf(z));
			category.addCrashSection("radius", Integer.valueOf(radius));
			category.addCrashSection("allowed", allowed);

			throw new ReportedException(report);
		}
	}

	@Override
	@Nullable
	public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random)
	{
		IntCache.resetIntCache();

		int i = x - range >> 2;
		int j = z - range >> 2;
		int k = x + range >> 2;
		int l = z + range >> 2;
		int i1 = k - i + 1;
		int j1 = l - j + 1;
		int[] aint = genBiomes.getInts(i, j, i1, j1);
		BlockPos blockpos = null;
		int k1 = 0;

		for (int l1 = 0; l1 < i1 * j1; ++l1)
		{
			int blockX = i + l1 % i1 << 2;
			int blockZ = j + l1 / i1 << 2;
			Biome biome = Biome.getBiome(aint[l1]);

			if (biomes.contains(biome) && (blockpos == null || random.nextInt(k1 + 1) == 0))
			{
				blockpos = new BlockPos(blockX, 0, blockZ);
				++k1;
			}
		}

		return blockpos;
	}

	@Override
	public void cleanupCache()
	{
		biomeCache.cleanupCache();
	}
}