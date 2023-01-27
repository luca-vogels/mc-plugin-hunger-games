package com.lupcode.mc.hungergames.maps;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import com.lupcode.mc.hungergames.Game;

public class VolcanoGenerator extends HungerGamesMapGenerator {

	private static int MAP_RADIUS = 500;
	
	private final static int SEA_LEVEL = 50;
	private final static int LOWEST_HEIGHT = 10;
	
	private final static int ISLAND_RADIUS = 400;
	private final static int SUBMERGE_ISLAND_RELATIVE = 0; // moves island 10 blocks below sea level
	private final static int ISLAND_HEIGHT = 300 - SEA_LEVEL + SUBMERGE_ISLAND_RELATIVE;
	
	
	private final static double TERRAIN_ROUGHNESS_1_WIDTH = 0.5 * ISLAND_RADIUS; // blocks
	private final static double TERRAIN_ROUGHNESS_1_HEIGHT = 0.125 * ISLAND_HEIGHT; // blocks
	private final static double TERRAIN_ROUGHNESS_2_WIDTH = 0.5 * TERRAIN_ROUGHNESS_1_WIDTH; // blocks
	private final static double TERRAIN_ROUGHNESS_2_HEIGHT = 1.0 * TERRAIN_ROUGHNESS_1_HEIGHT; // blocks
	private final static double TERRAIN_ROUGHNESS_3_WIDTH = 20; // blocks
	private final static double TERRAIN_ROUGHNESS_3_HEIGHT = 4; // blocks
	
	private final static int CRATER_OPENING_RADIUS = 20;
	private final static int CRATER_CHAMBER_RADIUS = 50;
	private final static int CRATER_DEPTH = 100;
	
	
	
	
	// helpers
	private final static int MAP_RADIUS_2 = MAP_RADIUS * MAP_RADIUS;
	private final static int ISLAND_RADIUS_2 = ISLAND_RADIUS * ISLAND_RADIUS;
	private final static int ISLAND_HIGHEST_Y = ISLAND_HEIGHT + SEA_LEVEL - SUBMERGE_ISLAND_RELATIVE;
	
	private final static int CRATER_OPENING_RADIUS_2 = CRATER_OPENING_RADIUS*CRATER_OPENING_RADIUS;
	private final static int CRATER_FLOOR_HEIGHT = ISLAND_HIGHEST_Y - CRATER_DEPTH;
	private final static int SLOPE_RADIUS_2 = ISLAND_RADIUS_2 - CRATER_OPENING_RADIUS_2;
	private final static double ROUGHNESS_RADIUS_2 = MAP_RADIUS_2 - CRATER_OPENING_RADIUS_2;
	
	private final static double SEA_SLOPE_WIDTH_2 = MAP_RADIUS_2 - ISLAND_RADIUS_2;
	private final static int SEA_DEPTH = SEA_LEVEL - LOWEST_HEIGHT - SUBMERGE_ISLAND_RELATIVE;
	
	
	
	
	
	private int getTerrainHeight(double dis2ToCenter, double x, double z) {
		int surfaceY = LOWEST_HEIGHT;
		
		if(dis2ToCenter < CRATER_OPENING_RADIUS_2) {
			// opening
			surfaceY = CRATER_FLOOR_HEIGHT;
			
			
		} else {
			double dis2ToCrater = Math.max(0, dis2ToCenter - CRATER_OPENING_RADIUS_2);
			
			// sqrt( 1 - (1 - 2*(1-x)^3 )^2 )
			double mapDis = 1 - dis2ToCrater / ROUGHNESS_RADIUS_2;
			double complexMapDis = 1 - 2*mapDis*mapDis*mapDis;
			double terrainMult = Math.sqrt(1 - complexMapDis*complexMapDis);
			
			// Rougness
			surfaceY += (int)((noise1.noise(x/TERRAIN_ROUGHNESS_1_WIDTH, z/TERRAIN_ROUGHNESS_1_WIDTH)*0.5+0.5) * 
								TERRAIN_ROUGHNESS_1_HEIGHT * terrainMult);
			surfaceY += (int)((noise2.noise(x/TERRAIN_ROUGHNESS_2_WIDTH, z/TERRAIN_ROUGHNESS_2_WIDTH)*0.5+0.5) * 
								TERRAIN_ROUGHNESS_2_HEIGHT * terrainMult);
			surfaceY += (int)((noise3.noise(x/TERRAIN_ROUGHNESS_3_WIDTH, z/TERRAIN_ROUGHNESS_3_WIDTH)*0.5+0.5) * 
								TERRAIN_ROUGHNESS_3_HEIGHT);
			
			
			
			if(dis2ToCenter < ISLAND_RADIUS_2) {
				// generate slope
				
				surfaceY += SEA_DEPTH; // island should be on top of sea
				
				// 1 - sqrt( 1 - x^2 )
				double islandDis = 1 - dis2ToCrater / SLOPE_RADIUS_2;
				surfaceY += (int) ((1 - Math.sqrt(1 - islandDis*islandDis)) * ISLAND_HEIGHT);
				
				
			} else if(dis2ToCenter < MAP_RADIUS_2) {
				// generate slope from shallow ocean to deep ocean
				
				double dis2ToIsland = dis2ToCenter - ISLAND_RADIUS_2;
				surfaceY += (int) ((0.5 + Math.cos(Math.PI * dis2ToIsland / SEA_SLOPE_WIDTH_2)*0.5) * SEA_DEPTH);
			
			} else {
				// generate ocean around island 
				
			}
			
		}
		return surfaceY;
	}
	
	
	
	
	
	private class VolcanoBiomeGenerator extends BiomeProvider {

		@Override
		public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
			double dis2ToCenter = x*x + z*z;
			int surfaceY = getTerrainHeight(dis2ToCenter, x, z);
			
			if(surfaceY < SEA_LEVEL - 15) return Biome.DEEP_LUKEWARM_OCEAN;
			if(surfaceY <= SEA_LEVEL) return Biome.WARM_OCEAN;
			if(surfaceY <= SEA_LEVEL + 3) return Biome.BEACH;
			
			if(surfaceY > ISLAND_HIGHEST_Y - 0.1*ISLAND_HEIGHT) return Biome.JAGGED_PEAKS;
			if(surfaceY > ISLAND_HIGHEST_Y - 0.25*ISLAND_HEIGHT) return Biome.STONY_PEAKS;
			
			return Biome.MEADOW;
		}

		@Override
		public List<Biome> getBiomes(WorldInfo worldInfo) {
			return Arrays.asList(
					Biome.BEACH,
					Biome.BIRCH_FOREST,
					Biome.DARK_FOREST,
					Biome.DEEP_LUKEWARM_OCEAN,
					Biome.FLOWER_FOREST,
					Biome.FOREST,
					Biome.JAGGED_PEAKS,
					Biome.JUNGLE,
					Biome.MANGROVE_SWAMP,
					Biome.MEADOW,
					Biome.PLAINS,
					Biome.SUNFLOWER_PLAINS,
					Biome.WARM_OCEAN,
					Biome.STONY_PEAKS
			);
		}
		
	}
	
	
	
	
	
	protected final BiomeProvider biomeProvider;
	protected SimplexNoiseGenerator noise1 = new SimplexNoiseGenerator(this.seed + 12348047);
	protected SimplexNoiseGenerator noise2 = new SimplexNoiseGenerator(this.seed + 50848275);
	protected SimplexNoiseGenerator noise3 = new SimplexNoiseGenerator(this.seed + 91234872);
	
	public VolcanoGenerator(Game game, long seed) {
		super(game, seed);
		biomeProvider = new VolcanoBiomeGenerator();
	}
	
	
	@Override
	public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
		return biomeProvider;
	}
	
	
	@Override
	public Location getFixedSpawnLocation(World world, Random random) {
		return new Location(world, 0, LOWEST_HEIGHT+ISLAND_HEIGHT, 0);
	}
	
	
	@Override
	public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
		for(int dx = 0; dx < 16; dx++) {
			double x = chunkX*16 + dx;
			for(int dz = 0; dz < 16; dz++) {
				double z = chunkZ*16 + dz;
				double dis2ToCenter = x*x + z*z;
				
				int surfaceY = getTerrainHeight(dis2ToCenter, x, z);
				
				for(int y = worldInfo.getMinHeight(); y < worldInfo.getMaxHeight(); y++)
					chunkData.setBlock(dx, y, dz, y <= surfaceY ? Material.STONE : 
												(y <= SEA_LEVEL ? Material.WATER : Material.AIR));
			}
		}
	}
	
	
	
	
	@Override
	public boolean shouldGenerateBedrock() {
		return true;
	}
	
	@Override
	public boolean shouldGenerateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
		int x = (Math.abs(chunkX)-1) * 16, z = (Math.abs(chunkZ)-1) * 16;
		return x*x + z*z < ISLAND_RADIUS_2;
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
