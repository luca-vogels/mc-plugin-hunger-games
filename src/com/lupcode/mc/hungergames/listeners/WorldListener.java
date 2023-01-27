package com.lupcode.mc.hungergames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import com.lupcode.mc.hungergames.items.Turret;

public class WorldListener implements Listener {

	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		Turret.onChunkLoad(event.getChunk());
	}
}
