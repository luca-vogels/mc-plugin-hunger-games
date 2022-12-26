package com.lupcode.mc.hungergames.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.lupcode.mc.hungergames.HungerGames;

public class PlayerListeners implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		HungerGames.GAME.playerJoined(event.getPlayer());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		HungerGames.GAME.playerQuit(event.getPlayer());
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		HungerGames.GAME.playerQuit(event.getEntity());
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		HungerGames.GAME.playerJoined(event.getPlayer());
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		if(!HungerGames.GAME.isInGame(p)) event.setCancelled(true);
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		
	}
	
	@EventHandler
	public void onPortal(PlayerPortalEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onExhaustion(EntityExhaustionEvent event) {
		if(!(event.getEntity() instanceof Player)) return;
		Player p = (Player) event.getEntity();
		if(!HungerGames.GAME.isInGame(p))
			p.setExhaustion(0f);
	}
	
	@EventHandler
	public void onHunger(FoodLevelChangeEvent event) {
		if(!(event.getEntity() instanceof Player)) return;
		Player p = (Player) event.getEntity();
		if(!HungerGames.GAME.isInGame(p))
			p.setFoodLevel(40);
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player)) return;
		DamageCause c = event.getCause();
		Player p = (Player) event.getEntity();
		if(!HungerGames.GAME.isInGame(p) && c != DamageCause.SUFFOCATION && c != DamageCause.VOID)
			event.setCancelled(true);
	}
}
