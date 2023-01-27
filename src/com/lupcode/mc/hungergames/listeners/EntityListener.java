package com.lupcode.mc.hungergames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import com.lupcode.mc.hungergames.items.Turret;

public class EntityListener implements Listener {

	
	@EventHandler
	public void onEntityDied(EntityDeathEvent event) {
		if(Turret.onDied(event.getEntity())) {
			event.getDrops().clear();
			return;
		}
	}
}
