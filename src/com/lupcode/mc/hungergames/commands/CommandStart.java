package com.lupcode.mc.hungergames.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.lupcode.mc.hungergames.HungerGames;

public class CommandStart implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.isOp()) {
			sender.sendMessage("Only for OPs");
			return true;
		}
		
		HungerGames.GAME.start();
		return true;
	}

}
