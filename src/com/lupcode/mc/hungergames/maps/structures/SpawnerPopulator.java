package com.lupcode.mc.hungergames.maps.structures;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

public class SpawnerPopulator extends HungerGamesPopulator {

	
	private static Material[] JUNGLE_MATERIALS = new Material[] {
			Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, Material.BASALT
	};
	
	private static EntityType[] JUNGLE_ENTITIES = new EntityType[] {
			EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.ILLUSIONER, 
			EntityType.PHANTOM, EntityType.PILLAGER, EntityType.SLIME, 
			EntityType.SPIDER, EntityType.WITCH, EntityType.CREEPER
	};
	
	
	public SpawnerPopulator(double probabilityPerChunk, int minDistanceFromCenter) {
		super(912376028, probabilityPerChunk, minDistanceFromCenter);
	}
	
	
	@Override
	public void populate(WorldInfo worldInfo, Random rand, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
		if(!generateInThisChunk(rand)) return;
		int X = rand.nextInt(16) + chunkX*16, Z = rand.nextInt(16) + chunkZ*16;
		int Y = getHighestBlockY(worldInfo, X, Z, limitedRegion);
		if(limitedRegion.getType(X, Y+1, Z) == Material.WATER || 
				X*X + Z*Z < this.minDistanceFromCenter_2 || 
				!isAreaInRegion(X-5, Y-5, Z-5, X+5, Y, Z+5, limitedRegion)) return;

		Material[] materials = JUNGLE_MATERIALS; // select depending on biome
		EntityType[] entities = JUNGLE_ENTITIES; // select depending on biome
		
		Y -= 8;
		for(int i=0; i < 9; i++)
			fillCircle(X, Y++, Z, 5,5, rand, limitedRegion, materials);
		fillCircle(X, Y++, Z, 4,4, rand, limitedRegion, Material.GRASS_BLOCK);
		fillCircle(X, Y++, Z, 1.5,1.5, rand, limitedRegion, Material.GRASS_BLOCK);
		
		Y-=2;
		for(int i=0; i < 5; i++)
			limitedRegion.setType(X, Y++, Z, Material.BASALT);
		limitedRegion.setType(X, Y, Z, Material.REDSTONE_TORCH);
		
		Y -= 5;
		limitedRegion.setType(X, Y, Z, Material.SPAWNER);
		CreatureSpawner spawner = (CreatureSpawner)limitedRegion.getBlockState(X, Y, Z);
		spawner.setMaxNearbyEntities(10);
		spawner.setMinSpawnDelay(20);
		spawner.setMaxSpawnDelay(60);
		spawner.setRequiredPlayerRange(20);
		spawner.setSpawnCount(3);
		spawner.setSpawnRange(8);
		spawner.setSpawnedType(entities[rand.nextInt(entities.length)]);
		spawner.update(true, true);
	}
	
}
