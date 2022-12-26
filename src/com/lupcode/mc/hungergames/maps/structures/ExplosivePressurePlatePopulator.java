package com.lupcode.mc.hungergames.maps.structures;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

public class ExplosivePressurePlatePopulator extends HungerGamesPopulator {

	private static int MIN_TNT_COUNT = 3;
	private static int MAX_TNT_COUNT = 15;
	

	public ExplosivePressurePlatePopulator(double probabilityPerChunk, int minDistanceFromCenter) {
		super(53185239, probabilityPerChunk, minDistanceFromCenter);
	}
	
	
	@Override
	public void populate(WorldInfo worldInfo, Random rand, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
		if(!generateInThisChunk(rand)) return;
		int X = rand.nextInt(16) + chunkX*16, Z = rand.nextInt(16) + chunkZ*16;
		int Y = getHighestBlockY(worldInfo, X, Z, limitedRegion);
		if(limitedRegion.getType(X, Y+1, Z) == Material.WATER || 
				X*X + Z*Z < this.minDistanceFromCenter_2 || 
				!isAreaInRegion(X, Y-2, Z-1, X, Y+1, Z, limitedRegion)) return;
		
		if(!isGroundBlock(X, Y, Z, limitedRegion))
			limitedRegion.setType(X, Y, Z, Material.GRASS_BLOCK);
		if(!isGroundBlock(X, Y, Z-1, limitedRegion)) {
			Material m = limitedRegion.getType(X, Y, Z);
			limitedRegion.setType(X, Y, Z-1, (m != Material.SAND && m != Material.GRAVEL) ? m : Material.STONE);
		}
		limitedRegion.setType(X, Y+1, Z, Material.STONE_PRESSURE_PLATE);
		
		limitedRegion.setType(X, Y-2, Z, Material.STONE);
		limitedRegion.setType(X, Y-2, Z-1, Material.STONE);
		
		limitedRegion.setType(X+1, Y-1, Z, Material.STONE);
		limitedRegion.setType(X+1, Y-1, Z-1, Material.STONE);
		
		limitedRegion.setType(X-1, Y-1, Z, Material.STONE);
		limitedRegion.setType(X-1, Y-1, Z-1, Material.STONE);
		
		limitedRegion.setType(X, Y-1, Z+1, Material.STONE);
		limitedRegion.setType(X, Y-1, Z-2, Material.STONE);
		
		limitedRegion.setType(X, Y-1, Z, Material.POWERED_RAIL);
		limitedRegion.setType(X, Y-1, Z-1, Material.AIR);
		
		int tntCount = rand.nextInt(MIN_TNT_COUNT, MAX_TNT_COUNT);
		for(int i=0; i < tntCount; i++)
			limitedRegion.spawn(new Location(null, X+0.5, Y-0.9, Z+0.5), ExplosiveMinecart.class);
	}
}
