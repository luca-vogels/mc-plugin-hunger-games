package com.lupcode.mc.hungergames.maps.structures;

import java.util.Random;

import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.lupcode.mc.hungergames.items.Turret;

public class TurretPopulator extends HungerGamesPopulator {

	public TurretPopulator(double probabilityPerChunk, int minDistanceFromCenter) {
		super(315855653, probabilityPerChunk, minDistanceFromCenter);
	}
	
	
	@Override
	public void populate(WorldInfo worldInfo, Random rand, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
		if(!generateInThisChunk(rand)) return;
		int X = rand.nextInt(16) + chunkX*16, Z = rand.nextInt(16) + chunkZ*16;
		int Y = rand.nextInt(worldInfo.getMinHeight()+2, worldInfo.getMaxHeight()-2);
		Y = getNearestFloorBlockY(worldInfo, X, Y, Z, limitedRegion) + 1;
		if(isLiquid(X, Y, Z, limitedRegion) || 
			!isWalkable(X, Y, Z, limitedRegion) || 
			!isWalkable(X, Y+1, Z, limitedRegion)) return;
		Turret.spawn(X, Y, Z, worldInfo.getName(), limitedRegion);
	}

}
