package fr.telec.simpleTP;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.telec.simpleCore.MetadataAccessor;

public class Cooldown {
	
	private JavaPlugin plugin;

	public Cooldown(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void use(Player player, String key) {
		use(plugin, player, key);
	}
	public static void use(JavaPlugin plugin, Player player, String key) {
		MetadataAccessor.setMetadata(plugin, player, key, System.currentTimeMillis());
	}

	public int getSince(Player player, String key) {
		return getSince(plugin, player, key);
	}
	public static int getSince(JavaPlugin plugin, Player player, String key) {
		long lastUse = (long) MetadataAccessor.getMetadata(plugin, player, key, (long) 0);
		return (int) ((System.currentTimeMillis() - lastUse)/1000);
	}

	public int getTo(Player player, String key, String configKey) {
		return getTo(plugin, player, key, configKey);
	}
	public static int getTo(JavaPlugin plugin, Player player, String key, String configKey) {
		return plugin.getConfig().getInt(configKey) - getSince(plugin, player, key);
	}

	public boolean test(Player player, String key, String configKey) {
		return test(plugin, player, key, configKey);
	}
	public static boolean test(JavaPlugin plugin, Player player, String key, String configKey) {
		return getTo(plugin, player, key, configKey) <= 0;
	}
}
