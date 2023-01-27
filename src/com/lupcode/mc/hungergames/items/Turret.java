package com.lupcode.mc.hungergames.items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.lupcode.mc.hungergames.HungerGames;
import com.lupcode.mc.hungergames.Utils;

public class Turret {

	private static String CUSTOM_NAME = "\u00A77\u00A7lTurret";
	private static int RANGE = 16;
	private static int SHOOT_COOLDOWN_TICKS = 20;
	private static double ROTATION_SPEED_PER_S = 90 * Math.PI / 180.0 / 10.0; // per 10ticks

	private static HashMap<String, HashSet<ArmorStand>> ENTITIES = new HashMap<>(); // <world, [Entity]>
	private static int RANGE2 = RANGE * RANGE;

	public static ItemStack getItem() {
		ItemStack s = new ItemStack(Material.ARMOR_STAND);
		ItemMeta m = s.getItemMeta();
		m.setDisplayName(CUSTOM_NAME);
		m.setLore(Arrays.asList("Place on a solid block."));
		s.setItemMeta(m);
		return s;
	}

	public static boolean onClick(Player p, EquipmentSlot slot, Block b, BlockFace bf) {
		if (!Utils.isSimilar(p.getInventory().getItem(slot), getItem()))
			return false;
		if (!spawn(b.getRelative(bf).getLocation(), p))
			return false;
		Utils.removeItem(p, slot, getItem(), 1);
		return true;
	}

	private static boolean isTurret(Entity ent) {
		return ent instanceof ArmorStand && ent.getCustomName() != null && ent.getCustomName().equals(CUSTOM_NAME);
	}

	public static boolean onDied(Entity ent) {
		if (!isTurret(ent))
			return false;
		HashSet<ArmorStand> entities = ENTITIES.get(ent.getWorld().getName());
		if (entities != null) {
			entities.remove(ent);
			if (entities.isEmpty())
				ENTITIES.remove(ent.getWorld().getName());
			else
				ENTITIES.put(ent.getWorld().getName(), entities);
		}
		ent.getWorld().dropItem(ent.getLocation(), getItem());
		ent.remove();
		return true;
	}

	public static void onChunkLoad(Chunk chunk) {
		for (Entity ent : chunk.getEntities()) {
			if (!isTurret(ent))
				continue;
			HashSet<ArmorStand> entities = ENTITIES.get(ent.getWorld().getName());
			entities = entities != null ? entities : new HashSet<>();
			entities.add((ArmorStand) ent);
			ENTITIES.put(ent.getWorld().getName(), entities);
		}
	}
	
	private static void modifyEntity(String worldName, ArmorStand ent, Player owner) {
		ent.getEquipment().setHelmet(new ItemStack(Material.DISPENSER, 1));

		ent.setArms(false);
		ent.setBasePlate(true);
		ent.setCanPickupItems(false);
		ent.setCollidable(true);
		ent.setCustomName(CUSTOM_NAME);
		ent.setCustomNameVisible(false);
		ent.setGravity(true);
		ent.setPersistent(true);
		ent.setRemoveWhenFarAway(false);

		ent.setMemory(MemoryKey.LIKED_PLAYER, owner != null ? owner.getUniqueId() : null);

		HashSet<ArmorStand> entities = ENTITIES.get(worldName);
		entities = entities != null ? entities : new HashSet<>();
		entities.add(ent);
		ENTITIES.put(worldName, entities);
	}

	public static boolean spawn(Location loc, Player owner) {
		if (loc.getBlock().getType() != Material.AIR
				|| loc.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR || Utils.hasBlockEntities(loc))
			return false;

		loc.setX(loc.getBlockX() + 0.5);
		loc.setY(loc.getBlockY() + 0.1);
		loc.setZ(loc.getBlockZ() + 0.5);

		ArmorStand ent = loc.getWorld().spawn(loc, ArmorStand.class);
		modifyEntity(loc.getWorld().getName(), ent, owner);

		return true;
	}
	
	public static void spawn(int X, int Y, int Z, String worldName, LimitedRegion limitedRegion) {
		ArmorStand ent = limitedRegion.spawn(new Location(null, X+0.5, Y+0.1, Z+0.5), ArmorStand.class);
		modifyEntity(worldName, ent, null);
	}

	public static void onUpdateFast() {
		Iterator<Entry<String, HashSet<ArmorStand>>> worldIt = ENTITIES.entrySet().iterator();
		while (worldIt.hasNext()) {
			Entry<String, HashSet<ArmorStand>> worldEntities = worldIt.next();
			Iterator<ArmorStand> it = worldEntities.getValue().iterator();
			while (it.hasNext()) {
				ArmorStand ent = it.next();
				if (ent.isDead()) {
					onDied(ent);
					it.remove();
					continue;
				}

				Location hl = ent.getEyeLocation();
				UUID ownerId = null; // TODO ent.getMemory(MemoryKey.LIKED_PLAYER);
				Player owner = ownerId!=null ? Bukkit.getPlayer(ownerId) : null;

				Player target = null;
				boolean shoot = false;
				double distance2 = Double.MAX_VALUE;
				for (Player p : ent.getWorld().getPlayers()) {
					double dis2 = p.getEyeLocation().distanceSquared(hl);
					if (dis2 > RANGE2 || !ent.hasLineOfSight(p))
						continue;
					if (dis2 <= distance2 && (target == null || !shoot)) {
						target = p;
						shoot = HungerGames.isInGame(p) && (ownerId == null || !ownerId.equals(p.getUniqueId()));
						distance2 = dis2;
					}
				}
				if (target != null) {
					Location pl = target.getEyeLocation();
					Location loc = ent.getLocation();
					EulerAngle hp = ent.getHeadPose();
					double currYaw = (loc.getYaw()+90) * Math.PI / 180;
					double currPitch = -ent.getHeadPose().getX();
					double desiredYaw = Math.atan2(pl.getZ() - hl.getZ(), pl.getX() - hl.getX());
					double desiredPitch = Math.asin((pl.getY()-hl.getY()) / pl.distance(hl));
					
					double pitch = currPitch + Math.max(-ROTATION_SPEED_PER_S, Math.min(desiredPitch - currPitch, ROTATION_SPEED_PER_S));
					double yaw = currYaw + Math.max(-ROTATION_SPEED_PER_S, Math.min(desiredYaw - currYaw, ROTATION_SPEED_PER_S));
					yaw += Math.round((desiredYaw-currYaw)/2/Math.PI) * 2*Math.PI; // prevent rotation in opposite direction when 360 -> 0
					
					hp = hp.setX(-pitch);
					ent.setHeadPose(hp);
					
					loc.setYaw((float)(yaw * 180 / Math.PI) - 90);
					ent.teleport(loc);
					
					ent.setArrowCooldown(Math.max(0, ent.getArrowCooldown() - 2)); // 2ticks interval
					if (shoot && ent.getArrowCooldown() == 0) {
						ent.setArrowCooldown(SHOOT_COOLDOWN_TICKS);
						hl.getWorld().playSound(hl, Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
						
						Vector d = new Vector(Math.cos(yaw), Math.sin(pitch)+0.1, Math.sin(yaw));
						hl.setY(hl.getY()-0.25);
						hl.add(d.clone().multiply(0.5));
						
						Arrow arrow = ent.getWorld().spawn(hl, Arrow.class);
						arrow.setCritical(true);
						arrow.setKnockbackStrength(2);
						arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);
						arrow.setShooter(owner!=null ? owner : ent);
						arrow.setVelocity(d.multiply(1.5));
					}
				}
			}

			if (worldEntities.getValue().isEmpty())
				worldIt.remove();
		}
	}
}
