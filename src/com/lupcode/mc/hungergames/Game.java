package com.lupcode.mc.hungergames;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import com.lupcode.mc.hungergames.maps.HungerGamesMapGenerator;
import com.lupcode.mc.hungergames.maps.JungleClockGenerator;

/**
 * Game instance that keeps track of state of game
 */
public class Game {
	protected String worldFile = "games/tmp1";
	protected World world; // reference to all players
	protected HashMap<String, Player> ingamePlayers = new HashMap<>();
	protected HungerGamesMapGenerator generator = new JungleClockGenerator(this);
	
	public Game() {
		
	}

	public boolean isInGame(Player p) {
		return p != null && world != null && ingamePlayers.containsKey(p.getName());
	}
	
	public int getMapRadius() {
		return generator.getMapRadius();
	}
	
	public void start() {
		
		world = WorldCreator.name(worldFile).generator(generator).createWorld();
		world.setAutoSave(false);
		world.setClearWeatherDuration(20 * 60 * 20 * 3);
		world.setDifficulty(Difficulty.HARD);
		world.setFullTime(23500);
		world.setKeepSpawnInMemory(false);
		world.setPVP(true);
		
		for(Player p : Bukkit.getOnlinePlayers()){
			p.teleport(world.getSpawnLocation()); // TODO
			ingamePlayers.put(p.getName(), p);
			
			p.setGameMode(GameMode.SURVIVAL);
			p.setFoodLevel(40);
			// TODO set stats
		}
	}
	
	public void stop() {
		if(world == null) return;
		for(Player p : world.getPlayers())
			HungerGames.teleportToSpawn(p);
		Bukkit.unloadWorld(world, false);
		world = null;
		HungerGames.deleteFiles(worldFile);
		ingamePlayers.clear();
	}
	
	
	public void playerJoined(Player p) {
		if(p == null) return;
		if(world == null) {
			HungerGames.teleportToSpawn(p);
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(HungerGames.THIS, new Runnable() { public void run() {
				p.setGameMode(GameMode.SPECTATOR);
				p.teleport(world.getSpawnLocation());
			} }, 1);
		}
	}
	
	public void playerQuit(Player p) {
		if(p == null) return;
		ingamePlayers.remove(p.getName());
	}
}
