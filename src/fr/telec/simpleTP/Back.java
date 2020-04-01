package fr.telec.simpleTP;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import fr.telec.simpleCore.Language;
import fr.telec.simpleCore.MetadataAccessor;
import fr.telec.simpleCore.StringHandler;

public class Back implements Listener {

	private static final String CFG_COOLDOWN_BACK = "cooldown.back";
	private static final String MDK_BACK_COOLDOWN = "back_timestamp";
	private static final String MDK_LAST_LOCATION = "last_location";
	
	private Language lg;
	private Cooldown cld;
	private JavaPlugin plugin;

	public Back(JavaPlugin plugin, Language lg, Cooldown cld) {
		this.plugin = plugin;
		this.lg = lg;
		this.cld = cld;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	/*
	 * Events handlers
	 */
	
	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent evt) {
		Player player = evt.getEntity();
		MetadataAccessor.setMetadata(plugin, player, MDK_LAST_LOCATION, player.getLocation());
	}

	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent evt) {
		Player player = evt.getPlayer();
		MetadataAccessor.setMetadata(plugin, player, MDK_LAST_LOCATION, evt.getFrom());
	}

	/*
	 * Actions
	 */
	
	public boolean go(Player player) {
		Location loc = (Location) MetadataAccessor.getMetadata(plugin, player, MDK_LAST_LOCATION);
		if (loc != null) {
			if (cld.test(player, MDK_BACK_COOLDOWN, CFG_COOLDOWN_BACK)) {
				TPHandler.teleport(player, loc);
				cld.use(player, MDK_BACK_COOLDOWN);
			} else {
				Map<String, String> values = new HashMap<String, String>();
				values.put("time", "" + cld.getTo(player, MDK_BACK_COOLDOWN, CFG_COOLDOWN_BACK));
				values.put("command", "/"+Commands.BACK);
				player.sendMessage(StringHandler.translate(lg.get("cooldown"), values));
			}
			return true;

		} else {
			player.sendMessage(StringHandler.colorize(lg.get("no_back")));
		}

		return false;
	}
}
