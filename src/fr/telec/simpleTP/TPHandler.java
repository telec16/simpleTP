package fr.telec.simpleTP;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TPHandler {

	@SuppressWarnings("unused")
	private JavaPlugin plugin;

	public TPHandler(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void askTp(Player moved, Player target, boolean askTarget) {
		// TODO Auto-generated method stub
		
	}

	public static void teleport(Player player, Location location) {
		if(player.isInsideVehicle()) {
			Entity vehicule = player.getVehicle();
			player.leaveVehicle();
			vehicule.teleport(location);
			player.teleport(location);
			vehicule.addPassenger(player);
		} else {
			player.teleport(location);
		}
	}

}
