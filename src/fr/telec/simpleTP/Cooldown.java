package fr.telec.simpleTP;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.telec.simpleCore.MetadataAccessor;

public class Cooldown {
	public static void use(JavaPlugin plugin, Player player, String key) {
		MetadataAccessor.setMetadata(plugin, player, key, System.currentTimeMillis());
	}

	public static int getSince(JavaPlugin plugin, Player player, String key) {
		long lastUse = (long) MetadataAccessor.getMetadata(plugin, player, key, (long) 0);
		return (int) ((System.currentTimeMillis() - lastUse)/1000);
	}
	
	public static int getTo(JavaPlugin plugin, Player player, String key, String configKey) {
		return plugin.getConfig().getInt(configKey) - getSince(plugin, player, key);
	}
	
	public static boolean test(JavaPlugin plugin, Player player, String key, String configKey) {
		return getTo(plugin, player, key, configKey) <= 0;
	}
}
