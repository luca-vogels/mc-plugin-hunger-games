package com.lupcode.mc.hungergames;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Utils {

	public static String toKey(Location loc) {
		return loc.getWorld().getName()+"|"+loc.getBlockX()+"|"+loc.getBlockY()+"|"+loc.getBlockZ();
	}
	
	
	public static boolean hasBlockEntities(Location loc) {
		Location l = new Location(loc.getWorld(), loc.getBlockX()+0.5, loc.getBlockY()+0.5, loc.getBlockZ()+0.5);
		return !l.getWorld().getNearbyEntities(l, 0.5, 0.5, 0.5).isEmpty();
	}
	
	public static boolean isSimilar(ItemStack s1, ItemStack s2) {
		if((s1 == null && s2 != null) || (s1 != null && s2 == null)) return false;
		if(s1 == null && s2 == null) return true;
		if(s1.getType() != s2.getType()) return false;
		ItemMeta m1 = s1.getItemMeta(), m2 = s2.getItemMeta();
		if((m1 == null && m2 != null) || (m1 != null && m2 == null)) return false;
		if(m1 == null && m2 == null) return true;
		String dn1 = m1.getDisplayName(), dn2 = m2.getDisplayName();
		if((dn1 == null && dn2!=null) || !dn1.equals(dn2)) return false;
		return true;
	}
	
	public static boolean removeItem(Player p, EquipmentSlot slot, ItemStack is, int amount) {
		// hand
		slot = slot!=null ? slot : EquipmentSlot.HAND;
		if(isSimilar(is, p.getEquipment().getItem(slot))) {
			ItemStack s = p.getEquipment().getItem(slot);
			s.setAmount(s.getAmount()-1);
			p.getEquipment().setItem(slot, s.getAmount() > 0 ? s : null);
			return true;
		}
		// hand
		slot = slot == EquipmentSlot.HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND;
		if(isSimilar(is, p.getEquipment().getItem(slot))) {
			ItemStack s = p.getEquipment().getItem(slot);
			s.setAmount(s.getAmount()-1);
			p.getEquipment().setItem(slot, s.getAmount() > 0 ? s : null);
			return true;
		}
		// hotbar
		for(int i=36; i <= 44; i++) {
			if(isSimilar(is, p.getInventory().getItem(i))) {
				ItemStack s = p.getInventory().getItem(i);
				s.setAmount(s.getAmount()-1);
				p.getInventory().setItem(i, s.getAmount() > 0 ? s : null);
				return true;
			}
		}
		// inventory
		for(int i=1; i <= 35; i++) {
			if(isSimilar(is, p.getInventory().getItem(i))) {
				ItemStack s = p.getInventory().getItem(i);
				s.setAmount(s.getAmount()-1);
				p.getInventory().setItem(i, s.getAmount() > 0 ? s : null);
				return true;
			}
		}
		return false;
	}
}
