package com.lupcode.mc.hungergames;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.lupcode.mc.hungergames.commands.CommandStart;
import com.lupcode.mc.hungergames.events.PlayerListeners;

public class HungerGames extends JavaPlugin {
	
	public static Game GAME = new Game();
	public static HungerGames THIS;
	
	public static final int MAP_BORDER_VISIBILITY_DISTANCE = 12;
	public static final int MAP_BORDER_SHOW_WIDTH = 10;
	public static final int MAP_BORDER_SHOW_HEIGHT = 5;
	public static final double MAP_BORDER_PARTICLES_PER_BLOCK_W = 0.75;
	public static final double MAP_BORDER_PARTICLES_PER_BLOCK_H = 0.5;
	
	
	@Override
	public void onDisable() {
		GAME.stop();
	}
	
	
	@Override
	public void onEnable() {
		THIS = this;
		reloadConfig();
		
		// TODO add defaults
		
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		
		// Register commands
		CommandStart cmdStart = new CommandStart();
		getCommand("start").setExecutor(cmdStart);
		
		
		// Register events
		Bukkit.getPluginManager().registerEvents(new PlayerListeners(), this);
		
		
		GAME.stop();
		
		
		for(Player p : Bukkit.getOnlinePlayers())
			teleportToSpawn(p);
		

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() { public void run() {
			Vector center = new Vector(0,0,0);

			if(GAME.world != null) {
				final int mapRad = GAME.getMapRadius();
				final double borderRadStep = Math.asin(1.0 / MAP_BORDER_PARTICLES_PER_BLOCK_W / mapRad);
				
				for(Player p : GAME.world.getPlayers()) {
					Location loc = p.getEyeLocation();
					double disToCenter = loc.toVector().setY(0).distance(center);
					
					if(Math.abs(mapRad - disToCenter) <= MAP_BORDER_VISIBILITY_DISTANCE) {
						double yaw = Math.atan2(loc.getZ(), loc.getX());
						
						
						for(double w=-MAP_BORDER_SHOW_WIDTH; w <= MAP_BORDER_SHOW_WIDTH; w+=1.0/MAP_BORDER_PARTICLES_PER_BLOCK_W) {
							double pyaw = yaw + borderRadStep*w;
							Location ploc = new Location(loc.getWorld(), 
															Math.cos(pyaw)*mapRad, 0, Math.sin(pyaw)*mapRad);
							for(double h=-MAP_BORDER_SHOW_HEIGHT; h <= MAP_BORDER_SHOW_HEIGHT; h+=1.0/MAP_BORDER_PARTICLES_PER_BLOCK_H) {
								ploc.setY(h+loc.getY());
				
								p.spawnParticle(Particle.PORTAL, ploc, 1, 0f,0f,0f, 0.5f, null);
							}
						}
						
					}
					
					if(disToCenter > mapRad) {
						if(p.getGameMode() != GameMode.SPECTATOR) {
							p.getWorld().spawnParticle(Particle.FLASH, loc, 10, 3.0,3.0,3.0, 1f, null);
							p.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2f, 2f);
							p.damage(4.0); // 2 hearts
							//p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 100, true, false, false));
						}
						p.setVelocity(center.clone().subtract(p.getLocation().toVector()).multiply(0.5 / (disToCenter-mapRad)).setY(0.5));
					}
				}
			}
			
		} }, 20, 10); // every 0.5s
	}
	
	
	public static void teleportToSpawn(Player p) {
		p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
		p.setGameMode(GameMode.ADVENTURE);
		p.setFoodLevel(40);
		
	}
	
	
	
	public static void deleteFiles(File file) {
		if(file.isDirectory())
			for(File f : file.listFiles())
				deleteFiles(f);
		file.delete();
	}
	public static void deleteFiles(String file) {
		deleteFiles(new File(file));
	}
}
