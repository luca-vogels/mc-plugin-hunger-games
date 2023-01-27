package com.lupcode.mc.hungergames.maps;

import org.bukkit.generator.ChunkGenerator;

import com.lupcode.mc.hungergames.Game;

public abstract class HungerGamesMapGenerator extends ChunkGenerator {
	
	protected final Game game;
	protected final long seed;
	
	public HungerGamesMapGenerator(Game game, long seed) {
		this.game = game;
		this.seed = seed;
	}
	
	public abstract int getMapRadius();
	
	public abstract int getMapRadiusSquared();
}
