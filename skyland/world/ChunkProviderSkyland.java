/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.world;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate;
import net.minecraftforge.event.terraingen.TerrainGen;
import skyland.core.Config;
import skyland.world.gen.MapGenCavesSkyland;

public class ChunkProviderSkyland implements IChunkProvider
{
	private final World worldObj;
	private final Random rand;

	private NoiseGeneratorOctaves noiseGen1;
	private NoiseGeneratorOctaves noiseGen2;
	private NoiseGeneratorOctaves noiseGen3;
	private NoiseGeneratorOctaves noiseGen4;
	private NoiseGeneratorOctaves noiseGen5;

	private double[] densities;
	private BiomeGenBase[] biomesForGeneration;
	private double[] noise1;
	private double[] noise2;
	private double[] noise3;
	private double[] noise4;
	private double[] noise5;

	private final MapGenBase caveGenerator = new MapGenCavesSkyland();
	private final WorldGenerator lakeWaterGen = new WorldGenLakes(Blocks.water);
	private final WorldGenerator lakeLavaGen = new WorldGenLakes(Blocks.lava);
	private final WorldGenerator worldGenIron = new WorldGenMinable(Blocks.iron_ore.getDefaultState(), 7);
	private final WorldGenerator worldGenEmerald = new WorldGenMinable(Blocks.emerald_ore.getDefaultState(), 4);
	private final WorldGenerator worldGenDiamond = new WorldGenMinable(Blocks.diamond_ore.getDefaultState(), 3);

	public ChunkProviderSkyland(World world)
	{
		this.worldObj = world;
		this.rand = new Random(world.getSeed());
		this.noiseGen1 = new NoiseGeneratorOctaves(rand, 16);
		this.noiseGen2 = new NoiseGeneratorOctaves(rand, 16);
		this.noiseGen3 = new NoiseGeneratorOctaves(rand, 8);
		this.noiseGen4 = new NoiseGeneratorOctaves(rand, 10);
		this.noiseGen5 = new NoiseGeneratorOctaves(rand, 16);
	}

	public void generateTerrain(int chunkX, int chunkZ, ChunkPrimer data)
	{
		byte b0 = 2;
		byte b1 = 1;
		int sizeX = b0 + b1;
		byte sizeY = 35;
		int sizeZ = b0 + b1;
		densities = initializeNoiseField(densities, chunkX * b0, 0, chunkZ * b0, sizeX, sizeY, sizeZ);

		for (int i = 0; i < b0; ++i)
		{
			for (int j = 0; j < b0; ++j)
			{
				for (int k = 0; k < 32; ++k)
				{
					double d0 = 0.25D;
					double d1 = densities[((i + 0) * sizeZ + j + 0) * sizeY + k + 0];
					double d2 = densities[((i + 0) * sizeZ + j + 1) * sizeY + k + 0];
					double d3 = densities[((i + 1) * sizeZ + j + 0) * sizeY + k + 0];
					double d4 = densities[((i + 1) * sizeZ + j + 1) * sizeY + k + 0];
					double d5 = (densities[((i + 0) * sizeZ + j + 0) * sizeY + k + 1] - d1) * d0;
					double d6 = (densities[((i + 0) * sizeZ + j + 1) * sizeY + k + 1] - d2) * d0;
					double d7 = (densities[((i + 1) * sizeZ + j + 0) * sizeY + k + 1] - d3) * d0;
					double d8 = (densities[((i + 1) * sizeZ + j + 1) * sizeY + k + 1] - d4) * d0;

					for (int l = 0; l < 4; ++l)
					{
						double d9 = 0.15D;
						double d10 = d1;
						double d11 = d2;
						double d12 = (d3 - d1) * d9;
						double d13 = (d4 - d2) * d9;

						for (int m = 0; m < 8; ++m)
						{
							double d14 = 0.125D;
							double d15 = d10;
							double d16 = (d11 - d10) * d14;

							for (int n = 0; n < 8; ++n)
							{
								IBlockState state = null;

								if (d15 > 0.0D)
								{
									state = Blocks.stone.getDefaultState();
								}

								int x = m + i * 8;
								int y = l + k * 4;
								int z = n + j * 8;
								data.setBlockState(x, y, z, state);
								d15 += d16;
							}

							d10 += d12;
							d11 += d13;
						}

						d1 += d5;
						d2 += d6;
						d3 += d7;
						d4 += d8;
					}
				}
			}
		}
	}

	private double[] initializeNoiseField(double[] densities, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ)
	{
		if (densities == null)
		{
			densities = new double[sizeX * sizeY * sizeZ];
		}

		double d0 = 684.412D;
		double d1 = 684.412D;
		noise4 = noiseGen4.generateNoiseOctaves(noise4, posX, posZ, sizeX, sizeZ, 1.121D, 1.121D, 0.5D);
		noise5 = noiseGen5.generateNoiseOctaves(noise5, posX, posZ, sizeX, sizeZ, 200.0D, 200.0D, 0.5D);
		d0 *= 4.0D;
		d1 *= 2.0D;
		noise1 = noiseGen3.generateNoiseOctaves(noise1, posX, posY, posZ, sizeX, sizeY, sizeZ, d0 / 80.0D, d1 / 160.0D, d0 / 80.0D);
		noise2 = noiseGen1.generateNoiseOctaves(noise2, posX, posY, posZ, sizeX, sizeY, sizeZ, d0, d1, d0);
		noise3 = noiseGen2.generateNoiseOctaves(noise3, posX, posY, posZ, sizeX, sizeY, sizeZ, d0, d1, d0);
		int i = 0;
		int j = 0;

		for (int x = 0; x < sizeX; ++x)
		{
			for (int z = 0; z < sizeZ; ++z)
			{
				double d2 = (noise4[j] + 256.0D) / 512.0D;

				if (d2 > 1.0D)
				{
					d2 = 1.0D;
				}

				double d3 = noise5[j] / 8000.0D;

				if (d3 < 0.0D)
				{
					d3 = -d3 * 0.3D;
				}

				d3 = d3 * 3.0D - 2.0D;
				float f = (x + posX - 0) / 1.0F;
				float f1 = (z + posZ - 0) / 1.0F;
				float f2 = 100.0F - MathHelper.sqrt_float(f * f + f1 * f1) * 8.0F;

				if (f2 > 80.0F)
				{
					f2 = 80.0F;
				}

				if (f2 < -100.0F)
				{
					f2 = -100.0F;
				}

				if (d3 > 1.0D)
				{
					d3 = 1.0D;
				}

				d3 /= 8.0D;
				d3 = 0.0D;

				if (d2 < 0.0D)
				{
					d2 = 0.0D;
				}

				d2 += 0.5D;
				d3 = d3 * sizeY / 16.0D;
				++j;
				double d4 = sizeY / 2.0D;

				for (int y = 0; y < sizeY; ++y)
				{
					double d5 = 0.0D;
					double d6 = (y - d4) * 8.0D / d2;

					if (d6 < 0.0D)
					{
						d6 *= -1.0D;
					}

					double d7 = noise2[i] / 512.0D;
					double d8 = noise3[i] / 512.0D;
					double d9 = (noise1[i] / 10.0D + 1.0D) / 2.0D;

					if (d9 < 0.0D)
					{
						d5 = d7;
					}
					else if (d9 > 1.0D)
					{
						d5 = d8;
					}
					else
					{
						d5 = d7 + (d8 - d7) * d9;
					}

					d5 -= 8.0D;
					d5 += f2;
					byte b0 = 2;
					double d10;

					if (y > sizeY / 2 - b0)
					{
						d10 = (y - (sizeY / 2 - b0)) / 64.0F;

						if (d10 < 0.0D)
						{
							d10 = 0.0D;
						}

						if (d10 > 1.0D)
						{
							d10 = 1.0D;
						}

						d5 = d5 * (1.0D - d10) + -3000.0D * d10;
					}

					b0 = 8;

					if (y < b0)
					{
						d10 = (b0 - y) / (b0 - 1.0F);
						d5 = d5 * (1.0D - d10) + -30.0D * d10;
					}

					densities[i] = d5;
					++i;
				}
			}
		}

		return densities;
	}

	public void replaceBiomeBlocks(int chunkX, int chunkZ, ChunkPrimer data, BiomeGenBase[] biomes)
	{
		for (int x = 0; x < 16; ++x)
		{
			for (int z = 0; z < 16; ++z)
			{
				byte b0 = 1;
				int i = -1;
				BiomeGenBase biome = biomes[z + x * 16];
				IBlockState top = biome.topBlock;
				IBlockState filler = biome.fillerBlock;

				for (int y = 127; y >= 0; --y)
				{
					IBlockState block = data.getBlockState(x, y, z);

					if (block.getBlock().getMaterial() == Material.air)
					{
						i = -1;
					}
					else if (block.getBlock() == Blocks.stone)
					{
						if (i == -1)
						{
							if (b0 <= 0)
							{
								top = Blocks.air.getDefaultState();
								filler = biome.fillerBlock;
							}

							i = b0;

							if (y >= 0)
							{
								data.setBlockState(x, y, z, top);
							}
							else
							{
								data.setBlockState(x, y, z, filler);
							}
						}
						else if (i > 0)
						{
							--i;
							data.setBlockState(x, y, z, filler);

							if (i == 0 && filler.getBlock() == Blocks.sand)
							{
								i = rand.nextInt(4) + Math.max(0, y - 63);
								filler = Blocks.sandstone.getDefaultState();
							}
						}
					}
				}
			}
		}
	}

	@Override
	public Chunk provideChunk(BlockPos pos)
	{
		return provideChunk(pos.getX() >> 4, pos.getZ() >> 4);
	}

	@Override
	public Chunk provideChunk(int chunkX, int chunkZ)
	{
		rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);

		ChunkPrimer data = new ChunkPrimer();
		biomesForGeneration = worldObj.getWorldChunkManager().loadBlockGeneratorData(biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);
		generateTerrain(chunkX, chunkZ, data);
		replaceBiomeBlocks(chunkX, chunkZ, data, biomesForGeneration);

		if (Config.generateCaves)
		{
			caveGenerator.func_175792_a(this, worldObj, chunkX, chunkZ, data);
		}

		Chunk chunk = new Chunk(worldObj, data, chunkX, chunkZ);
		byte[] biomes = chunk.getBiomeArray();

		for (int index = 0; index < biomes.length; ++index)
		{
			biomes[index] = (byte)biomesForGeneration[index].biomeID;
		}

		chunk.generateSkylightMap();

		return chunk;
	}

	@Override
	public boolean chunkExists(int chunkX, int chunkZ)
	{
		return true;
	}

	@Override
	public void populate(IChunkProvider chunkProvider, int chunkX, int chunkZ)
	{
		BlockFalling.fallInstantly = true;

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(chunkProvider, worldObj, rand, chunkX, chunkZ, false));

		BlockPos pos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
		BiomeGenBase biome = worldObj.getBiomeGenForCoords(pos.add(16, 0, 16));
		rand.setSeed(worldObj.getSeed());
		long xSeed = rand.nextLong() / 2L * 2L + 1L;
		long zSeed = rand.nextLong() / 2L * 2L + 1L;
		rand.setSeed(chunkX * xSeed + chunkZ * zSeed ^ worldObj.getSeed());
		int i, genX, genY, genZ;

		if (Config.generateLakes)
		{
			if (!BiomeDictionary.isBiomeOfType(biome, Type.SANDY) && rand.nextInt(4) == 0 && TerrainGen.populate(chunkProvider, worldObj, rand, chunkX, chunkZ, false, Populate.EventType.LAKE))
			{
				genX = rand.nextInt(16) + 8;
				genZ = rand.nextInt(16) + 8;
				genY = rand.nextInt(Math.max(worldObj.getHorizon(pos.add(genX, 0, genZ)).getY() - 10, 1)) + 10;

				lakeWaterGen.generate(worldObj, rand, pos.add(genX, genY, genZ));
			}

			if (rand.nextInt(8) == 0 && TerrainGen.populate(chunkProvider, worldObj, rand, chunkX, chunkZ, false, Populate.EventType.LAVA))
			{
				genX = rand.nextInt(16) + 8;
				genZ = rand.nextInt(16) + 8;
				i = worldObj.getHorizon(pos.add(genX, 0, genZ)).getY();
				genY = rand.nextInt(Math.max(i - 10, 1)) + 10;

				if (genY < i || rand.nextInt(10) == 0)
				{
					lakeLavaGen.generate(worldObj, rand, pos.add(genX, genY, genZ));
				}
			}
		}

		boolean doGen = TerrainGen.generateOre(worldObj, rand, worldGenIron, pos, EventType.IRON);
		for (i = 0; doGen && i < 15; ++i)
		{
			genX = rand.nextInt(16);
			genY = rand.nextInt(50) + 20;
			genZ = rand.nextInt(16);

			worldGenIron.generate(worldObj, rand, pos.add(genX, genY, genZ));
		}

		doGen = TerrainGen.generateOre(worldObj, rand, worldGenEmerald, pos, EventType.CUSTOM);
		for (i = 0; doGen && i < 6; ++i)
		{
			genX = rand.nextInt(16);
			genY = rand.nextInt(50) + 10;
			genZ = rand.nextInt(16);

			worldGenEmerald.generate(worldObj, rand, pos.add(genX, genY, genZ));
		}

		doGen = TerrainGen.generateOre(worldObj, rand, worldGenDiamond, pos, EventType.DIAMOND);
		for (i = 0; doGen && i < 7; ++i)
		{
			genX = rand.nextInt(16);
			genY = rand.nextInt(50) + 10;
			genZ = rand.nextInt(16);

			worldGenDiamond.generate(worldObj, rand, pos.add(genX, genY, genZ));
		}

		biome.decorate(worldObj, rand, pos);

		if (TerrainGen.populate(chunkProvider, worldObj, rand, chunkX, chunkZ, false, Populate.EventType.ANIMALS))
		{
			SpawnerAnimals.performWorldGenSpawning(worldObj, biome, pos.getX() + 8, pos.getZ() + 8, 16, 16, rand);
		}

		pos = pos.add(8, 0, 8);

		doGen = TerrainGen.populate(chunkProvider, worldObj, rand, chunkX, chunkZ, false, Populate.EventType.ICE);
		for (genX = 0; doGen && genX < 16; ++genX)
		{
			for (genZ = 0; genZ < 16; ++ genZ)
			{
				BlockPos pos1 = worldObj.getPrecipitationHeight(pos.add(genX, 0, genZ)).down();

				if (worldObj.func_175675_v(pos1))
				{
					worldObj.setBlockState(pos1, Blocks.ice.getDefaultState(), 2);
				}
			}
		}

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(chunkProvider, worldObj, rand, chunkX, chunkZ, false));

		BlockFalling.fallInstantly = false;
	}

	@Override
	public boolean func_177460_a(IChunkProvider provider, Chunk chunk, int chunkX, int chunkZ)
	{
		return false;
	}

	@Override
	public boolean saveChunks(boolean flag, IProgressUpdate progress)
	{
		return true;
	}

	@Override
	public void saveExtraData() {}

	@Override
	public boolean unloadQueuedChunks()
	{
		return false;
	}

	@Override
	public boolean canSave()
	{
		return true;
	}

	@Override
	public String makeString()
	{
		return "SkylandRandomLevelSource";
	}

	@Override
	public List func_177458_a(EnumCreatureType creature, BlockPos pos)
	{
		BiomeGenBase biome = worldObj.getBiomeGenForCoords(pos);

		return biome == null ? null : biome.getSpawnableList(creature);
	}

	@Override
	public BlockPos getStrongholdGen(World world, String name, BlockPos pos)
	{
		return null;
	}

	@Override
	public int getLoadedChunkCount()
	{
		return 0;
	}

	@Override
	public void recreateStructures(Chunk chunk, int chunkX, int chunkZ) {}
}