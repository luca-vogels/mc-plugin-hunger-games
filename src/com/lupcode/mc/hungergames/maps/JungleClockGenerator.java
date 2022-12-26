package com.lupcode.mc.hungergames.maps;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import com.lupcode.mc.hungergames.Game;
import com.lupcode.mc.hungergames.maps.structures.ExplosivePressurePlatePopulator;
import com.lupcode.mc.hungergames.maps.structures.SpawnerPopulator;

public class JungleClockGenerator extends HungerGamesMapGenerator  {
	
	
	
	
	
	// TODO SPAWN ANIMALS IN WARM_OCEAN
	
	
	
	
	
	
	
	private static int MAP_RADIUS = 400;
	
	private static int LAKE_DEPTH = 20;
	private static int LAKE_RADIUS = 80;
	private static int BEACH_WIDTH = 6;
	private static int BEACH_JUNGLE_TRANSITION = 3;
	
	private static double TERRAIN_ROUGHNESS_1_WIDTH = 200; // blocks
	private static double TERRAIN_ROUGHNESS_1_HEIGHT = 20; // blocks
	private static double TERRAIN_ROUGHNESS_2_WIDTH = 40; // blocks
	private static double TERRAIN_ROUGHNESS_2_HEIGHT = 20; // blocks
	private static double TERRAIN_ROUGHNESS_3_WIDTH = 10; // blocks
	private static double TERRAIN_ROUGHNESS_3_HEIGHT = 2; // blocks
	private static int TERRAIN_ROUGHNESS_TRANSITION = (int) (MAP_RADIUS * 0.5); // blocks between flat to full roughness
	
	
	private static int BASE_HEIGHT = 50;
	private static int EDGE_HEIGHT = (int) (260 - TERRAIN_ROUGHNESS_1_HEIGHT - TERRAIN_ROUGHNESS_2_HEIGHT - TERRAIN_ROUGHNESS_3_HEIGHT);
	
	
	// helpers
	private static int MAP_RADIUS_2 = MAP_RADIUS*MAP_RADIUS;
	private static int HEIGHT_DIFF = (int) (1.1 * (EDGE_HEIGHT - BASE_HEIGHT));
	private static int LAKE_RADIUS_2 = LAKE_RADIUS*LAKE_RADIUS;
	private static int VIEW_RADIUS_2 = (int) (1.1 * MAP_RADIUS_2) - LAKE_RADIUS_2;
	private static int TRANSITION_MIN_RADIUS_2 = (LAKE_RADIUS+BEACH_WIDTH)*(LAKE_RADIUS+BEACH_WIDTH);
	private static int JUNGLE_MIN_RADIUS = LAKE_RADIUS + BEACH_WIDTH + BEACH_JUNGLE_TRANSITION;
	private static int JUNGLE_MIN_RADIUS_2 = JUNGLE_MIN_RADIUS * JUNGLE_MIN_RADIUS;
	private int TERRAIN_ROUGHNESS_TRANSITION_2 = TERRAIN_ROUGHNESS_TRANSITION*TERRAIN_ROUGHNESS_TRANSITION;
	
	
	
	private class JungleClockBiomeGenerator extends BiomeProvider {

		@Override
		public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
			double dis2ToCenter = x*x + z*z;
			return dis2ToCenter >= JUNGLE_MIN_RADIUS_2 ? (y >= BASE_HEIGHT-3 ? Biome.JUNGLE : Biome.LUSH_CAVES) : 
				(dis2ToCenter >= TRANSITION_MIN_RADIUS_2 ? Biome.SPARSE_JUNGLE : 
				(dis2ToCenter >= LAKE_RADIUS_2 ? Biome.BEACH : Biome.WARM_OCEAN));
		}

		@Override
		public List<Biome> getBiomes(WorldInfo worldInfo) {
			return Arrays.asList(
					Biome.BEACH,
					Biome.JUNGLE,
					Biome.LUSH_CAVES,
					Biome.SPARSE_JUNGLE,
					Biome.WARM_OCEAN
			);
		}
		
	}
	
	
	
	protected final Game game;
	protected final BiomeProvider biomeProvider;
	
	public JungleClockGenerator(Game game) {
		this.game = game;
		biomeProvider = new JungleClockBiomeGenerator();
	}
	
	
	@Override
	public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
		return biomeProvider;
	}
	
	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		return Arrays.asList(
			new SpawnerPopulator(1.0/40.0, JUNGLE_MIN_RADIUS + 20),
			new ExplosivePressurePlatePopulator(1.0 / 10.0, LAKE_RADIUS+2)
		);
	}
	
	
	@Override
	public Location getFixedSpawnLocation(World world, Random random) {
		return new Location(world, 0, BASE_HEIGHT, 0);
	}
	
	
	@Override
	public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
		SimplexNoiseGenerator noise1 = new SimplexNoiseGenerator(worldInfo.getSeed());
		SimplexNoiseGenerator noise2 = new SimplexNoiseGenerator(worldInfo.getSeed() + 97531);
		SimplexNoiseGenerator noise3 = new SimplexNoiseGenerator(worldInfo.getSeed() + 24680);
		
		for(int dx = 0; dx < 16; dx++) {
			double x = chunkX*16 + dx;
			for(int dz = 0; dz < 16; dz++) {
				double z = chunkZ*16 + dz;
				double dis2ToCenter = x*x + z*z;
				double dis2ToLake = dis2ToCenter - LAKE_RADIUS_2;
				double terrainMult = Math.max(0.0, Math.min(dis2ToLake / TERRAIN_ROUGHNESS_TRANSITION_2, 1.0));
				
				int surfaceY = BASE_HEIGHT ;
				
				
				
				// Rougness
				surfaceY += (int)((noise1.noise(x/TERRAIN_ROUGHNESS_1_WIDTH, z/TERRAIN_ROUGHNESS_1_WIDTH)*0.5+0.5) * 
									TERRAIN_ROUGHNESS_1_HEIGHT * terrainMult);
				surfaceY += (int)((noise2.noise(x/TERRAIN_ROUGHNESS_2_WIDTH, z/TERRAIN_ROUGHNESS_2_WIDTH)*0.5+0.5) * 
									TERRAIN_ROUGHNESS_2_HEIGHT * terrainMult);
				surfaceY += (int)((noise3.noise(x/TERRAIN_ROUGHNESS_3_WIDTH, z/TERRAIN_ROUGHNESS_3_WIDTH)*0.5+0.5) * 
									TERRAIN_ROUGHNESS_3_HEIGHT * terrainMult);
				
				
				// Basic terrain
				if(dis2ToCenter < LAKE_RADIUS_2) {
					// LAKE
					surfaceY -= (0.5 + Math.cos(Math.PI*dis2ToCenter/LAKE_RADIUS_2)*0.5) * LAKE_DEPTH;
					
					for(int y = chunkData.getMinHeight(); y < chunkData.getMaxHeight(); y++)
						chunkData.setBlock(dx, y, dz, y <= surfaceY ? Material.STONE : 
														(y <= BASE_HEIGHT ? Material.WATER : Material.AIR));
						
					
				} else {
					// LAND
					surfaceY += dis2ToLake < VIEW_RADIUS_2 ? 
								(1-Math.cos(Math.PI*dis2ToLake/VIEW_RADIUS_2))*0.5 * HEIGHT_DIFF
									+ (dis2ToCenter >= TRANSITION_MIN_RADIUS_2 ? 1 : 0)
								: HEIGHT_DIFF;
					
					
					for(int y = chunkData.getMinHeight(); y < chunkData.getMaxHeight(); y++) {
						chunkData.setBlock(dx, y, dz, y <= surfaceY ? Material.STONE : Material.AIR);
					}
				}
				
				
			}
		}
	}
	
	
	@Override
	public boolean shouldGenerateBedrock() {
		return true;
	}
	
	@Override
	public boolean shouldGenerateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
		// only if outside of spawn
		int x = (Math.abs(chunkX)+1) * 16, z = (Math.abs(chunkZ)+1) * 16;
		return x*x + z*z > LAKE_RADIUS_2;
	}
	
	@Override
	public boolean shouldGenerateDecorations() {
		return true;
	}
	
	@Override
	public boolean shouldGenerateMobs() {
		return true;
	}
	
	@Override
	public boolean shouldGenerateStructures(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
		int x = (Math.abs(chunkX)+1) * 16, z = (Math.abs(chunkZ)+1) * 16;
		return x*x + z*z > LAKE_RADIUS_2;
	}
	
	@Override
	public boolean shouldGenerateSurface() {
		return true;
	}

	
	@Override
	public int getMapRadius() {
		return MAP_RADIUS;
	}


	@Override
	public int getMapRadiusSquared() {
		return MAP_RADIUS_2;
	}

}
