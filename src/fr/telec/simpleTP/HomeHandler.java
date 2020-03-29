package fr.telec.simpleTP;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.telec.simpleCore.ConfigAccessor;

public class HomeHandler {

	public static final String HOME = "home";
	private static final String HOMES_FILENAME = "locations.yml";

	private ConfigAccessor homesAcs = null;
	private FileConfiguration homes = null;
	private boolean use_UUID = true;

	public HomeHandler(JavaPlugin plugin, boolean use_UUID) {
		this.use_UUID = use_UUID;

		homesAcs = new ConfigAccessor(plugin, HOMES_FILENAME);

		homesAcs.saveDefaultConfig();

		reload();
	}

	public void reload() {
		homesAcs.reloadConfig();
		homes = homesAcs.getConfig();
	}

	public void set(Player player, String warp, Location loc) {
		homes.set(getPath(player, warp), loc);
		homesAcs.saveConfig();
	}

	public void del(Player player, String warp) {
		homes.set(getPath(player, warp), null);
		homesAcs.saveConfig();
	}

	public Location get(Player player, String warp) {
		return homes.getLocation(getPath(player, warp));
	}

	public Set<String> getHomeList(Player player) {
		ConfigurationSection home = homes.getConfigurationSection(getRoot(player));
		if (home != null)
			return home.getKeys(false);
		else
			return new HashSet<String>();
	}

	public int getWarpCount(Player player) {
		Set<String> keys = getHomeList(player);
		keys.remove(HOME);
		return keys.size();
	}

	private String getPath(Player player, String warp) {
		return getRoot(player) + "." + (warp == null ? HOME : warp);
	}

	private String getRoot(Player player) {
		return use_UUID ? player.getUniqueId().toString() : player.getName();
	}
}
