package com.lupcode.mc.hungergames.maps.structures;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

public class HungerGamesPopulator extends BlockPopulator {

	private final int populatorSeed;
	private final double probabilityPerChunk;
	protected int minDistanceFromCenter_2;
	
	protected HungerGamesPopulator(int populatorSeed, double probabilityPerChunk, int minDistanceFromCenter) {
		this.populatorSeed = populatorSeed;
		this.probabilityPerChunk = probabilityPerChunk;
		this.minDistanceFromCenter_2 = minDistanceFromCenter*minDistanceFromCenter;
	}
	

	protected boolean generateInThisChunk(Random rand) {
		rand.setSeed(rand.nextLong() + this.populatorSeed);
		return rand.nextDouble() < this.probabilityPerChunk;
	}
	
	
	protected boolean isAreaInRegion(int x1, int y1, int z1, int x2, int y2, int z2, LimitedRegion limitedRegion) {
		int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
		int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
		int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
		return limitedRegion.isInRegion(minX, minY, minZ) &&
				limitedRegion.isInRegion(minX, minY, maxZ) &&
				limitedRegion.isInRegion(minX, maxY, minZ) &&
				limitedRegion.isInRegion(minX, maxY, maxZ) &&
				limitedRegion.isInRegion(maxX, minY, minZ) &&
				limitedRegion.isInRegion(maxX, minY, maxZ) &&
				limitedRegion.isInRegion(maxX, maxY, minZ) &&
				limitedRegion.isInRegion(maxX, maxY, maxZ);
	}
	
	protected boolean isGroundBlock(int x, int y, int z, LimitedRegion limitedRegion) {
		Material m = limitedRegion.getBlockData(x, y, z).getMaterial();
		return !m.isAir() && !m.isBurnable() && m.isOccluding() && m.isSolid();
	}
	
	protected boolean isWalkable(int x, int y, int z, LimitedRegion limitedRegion) {
		Material m = limitedRegion.getBlockData(x, y, z).getMaterial();
		return m.isAir() || !m.isSolid();
	}
	
	protected boolean isLiquid(int x, int y, int z, LimitedRegion limitedRegion) {
		Material m = limitedRegion.getBlockData(x, y, z).getMaterial();
		return m == Material.WATER || m == Material.LAVA;
	}
	
	protected int getHighestBlockY(WorldInfo worldInfo, int x, int z, LimitedRegion limitedRegion) {
		for(int y = worldInfo.getMaxHeight()-1; y > worldInfo.getMinHeight(); y--)
			if(isGroundBlock(x, y, z, limitedRegion)) return y;
		return worldInfo.getMinHeight()+1;
	}
	
	
	protected int getNearestFloorBlockY(WorldInfo worldInfo, int x, int y, int z, LimitedRegion limitedRegion) {
		int ty = 0;
		if(limitedRegion.isInRegion(x, ty, z) && !isGroundBlock(x, y, z, limitedRegion))
			for(int i=1; i < worldInfo.getMaxHeight(); i++) {
				ty = y + i;
				if(limitedRegion.isInRegion(x, ty, z) && !isGroundBlock(x, ty, z, limitedRegion)) return ty-1;
				ty = y - i;
				if(limitedRegion.isInRegion(x, ty, z) && ty > 0 && !isGroundBlock(x, ty, z, limitedRegion)) break;
			}
		// found ceiling, now search for floor
		for(int i=1; i < worldInfo.getMaxHeight(); i++) {
			ty--;
			if(limitedRegion.isInRegion(x, ty, z) && isGroundBlock(x, ty, z, limitedRegion)) break;
		}
		return ty > worldInfo.getMinHeight() ? ty : y;
	}
	
	
	protected void fillCircle(int centerX, int y, int centerZ, double radX, double radZ, Random rand, LimitedRegion limitedRegion, Material ...materials) {
		double radX_2 = radX*radX, radZ_2 = radZ*radZ;
		int radXC = (int) Math.ceil(radX), radZC = (int) Math.ceil(radZ);
		for(int x=-radXC; x <= radXC; x++) {
			int x_2 = x*x;
			for(int z=-radZC; z <= radZC; z++) {
				if(x_2/radX_2 + z*z/radZ_2 <= 1)
					limitedRegion.setType(centerX+x, y, centerZ+z, materials[rand.nextInt(materials.length)]);
			}
		}
	}
}
