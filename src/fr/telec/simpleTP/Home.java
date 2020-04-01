package fr.telec.simpleTP;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.telec.simpleCore.Language;
import fr.telec.simpleCore.StringHandler;

public class Home {

	private static final String CFG_USE_UUID = "use_UUID";
	private static final String CFG_ALLOW_HOME = "allows.home";
	private static final String CFG_ALLOW_WARP = "allows.warps";
	private static final String CFG_ALLOW_MAX = "allows.maxwarps";
	private static final String CFG_COOLDOWN_HOME = "cooldown.home";
	private static final String MDK_HOME_COOLDOWN = "home_timestamp";

	private JavaPlugin plugin;
	private Language lg;
	private Cooldown cld;
	private HomeHandler homes;

	private boolean allowHome = true;
	private int warpMax = 0;

	public Home(JavaPlugin plugin, Language lg, Cooldown cld) {
		this.plugin = plugin;
		this.lg = lg;
		this.cld = cld;

		homes = new HomeHandler(plugin, plugin.getConfig().getBoolean(CFG_USE_UUID));

		refreshConfig();
	}

	/*
	 * Actions
	 */
	
	public boolean set(Player player, String warp) {
		int warpCount = homes.getWarpCount(player);

		Map<String, String> values = new HashMap<String, String>();
		values.put("warp", warp == null ? HomeHandler.HOME : warp);
		values.put("okhome", "" + allowHome);
		values.put("count", "" + warpCount);
		values.put("max", "" + warpMax);

		if (warp == null && allowHome == true
				|| warp != null && (warpCount < warpMax || homes.getHomeList(player).contains(warp))) {
			homes.set(player, warp, player.getLocation());
			player.sendMessage(StringHandler.translate(lg.get("home_set"), values));
			return true;

		} else {
			player.sendMessage(StringHandler.translate(lg.get("home_set_error"), values));
		}

		return false;
	}

	public boolean del(Player player, String warp) {
		Map<String, String> values = new HashMap<String, String>();
		values.put("warp", warp == null ? HomeHandler.HOME : warp);

		homes.del(player, warp);
		player.sendMessage(StringHandler.translate(lg.get("home_del"), values));

		return true;
	}

	public boolean list(Player player) {
		Set<String> homeList = homes.getHomeList(player);

		player.sendMessage(ChatColor.AQUA + StringHandler.colorize(lg.get("list_home")));
		for (String home : homeList) {
			player.sendMessage(ChatColor.DARK_AQUA + home);
		}

		return true;
	}

	public boolean show(Player player, String warp) {
		Location l = homes.get(player, warp);
		if (l != null) {
			Map<String, String> values = new HashMap<String, String>();
			values.put("warp", warp == null ? HomeHandler.HOME : warp);
			values.put("X", "" + l.getBlockX());
			values.put("Y", "" + l.getBlockY());
			values.put("Z", "" + l.getBlockZ());
			values.put("world", l.getWorld().getName().toString());
			player.sendMessage(StringHandler.translate(lg.get("show_home"), values));

			return true;
		}

		return false;
	}

	public boolean go(Player player, String warp) {
		Location loc = null;

		Map<String, String> values = new HashMap<String, String>();
		values.put("warp", warp == null ? HomeHandler.HOME : warp);
		values.put("okhome", "" + allowHome);
		values.put("max", "" + warpMax);

		if (warp == null && allowHome == true || warp != null && warpMax != 0) {
			loc = homes.get(player, warp);
		}

		if (loc != null) {
			if (cld.test(player, MDK_HOME_COOLDOWN, CFG_COOLDOWN_HOME)) {
				TPHandler.teleport(player, loc);
				player.sendMessage(StringHandler.translate(lg.get("home_go"), values));
				cld.use(player, MDK_HOME_COOLDOWN);
			} else {
				values.put("time", "" + cld.getTo(player, MDK_HOME_COOLDOWN, CFG_COOLDOWN_HOME));
				values.put("command", "/"+Commands.HOME);
				player.sendMessage(StringHandler.translate(lg.get("cooldown"), values));
			}
			return true;

		} else {
			player.sendMessage(StringHandler.translate(lg.get("home_go_error"), values));
		}

		return false;
	}

	/*
	 * Helpers
	 */
	
	private void refreshConfig() {
		allowHome = plugin.getConfig().getBoolean(CFG_ALLOW_HOME);
		warpMax = plugin.getConfig().getInt(CFG_ALLOW_MAX);
		if (!plugin.getConfig().getBoolean(CFG_ALLOW_WARP))
			warpMax = 0;
		else if (warpMax == 0)
			warpMax = Integer.MAX_VALUE;
	}

	public void reload() {
		refreshConfig();
		homes.reload();
	}
}