package com.lupcode.mc.hungergames.maps;

import org.bukkit.generator.ChunkGenerator;

public abstract class HungerGamesMapGenerator extends ChunkGenerator {
	
	public abstract int getMapRadius();
	
	public abstract int getMapRadiusSquared();
}
