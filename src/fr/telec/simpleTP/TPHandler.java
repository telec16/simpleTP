package fr.telec.simpleTP;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class TPHandler {

	public TPHandler() {
	}

	public void askTp(Player moved, Player target, boolean askTarget) {
		// TODO Auto-generated method stub
		
	}

	public static void teleport(Player player, Player target) {
		teleport(player, target.getLocation());
	}

	public static void teleport(Player player, Location target) {
		if(player.isInsideVehicle()) {
			Entity vehicule = player.getVehicle();
			player.leaveVehicle();
			vehicule.teleport(target);
			player.teleport(target);
			vehicule.addPassenger(player);
		} else {
			player.teleport(target);
		}
	}

}
