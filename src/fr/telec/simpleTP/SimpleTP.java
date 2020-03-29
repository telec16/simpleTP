package fr.telec.simpleTP;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import fr.telec.simpleCore.Language;
import fr.telec.simpleCore.MetadataAccessor;
import fr.telec.simpleCore.StringHandler;

public class SimpleTP extends JavaPlugin implements Listener {

	// Command names
	private static final String CMD_TPHERE = "tphere";
	private static final String CMD_TPTO = "tpto";
	private static final String CMD_HOME = "home";
	private static final String CMD_DELHOME = "delhome";
	private static final String CMD_SETHOME = "sethome";
	private static final String CMD_SHOWHOME = "showhome";
	private static final String CMD_LISTHOME = "listhome";
	private static final String CMD_BACK = "back";
	private static final String CMD_TPDENY = "tpdeny";
	private static final String CMD_TPACCEPT = "tpaccept";
	private static final String CMD_TPCANCEL = "tpcancel";
	// Config file paths
	private static final String CFG_USE_UUID = "use_UUID";
	private static final String CFG_COOLDOWN_BACK = "cooldown.back";
	private static final String CFG_COOLDOWN_HOME = "cooldown.home";
	private static final String CFG_COOLDOWN_TP = "cooldown.tp";
	private static final String CFG_ALLOW_HOME = "allows.home";
	private static final String CFG_ALLOW_WARP = "allows.warps";
	private static final String CFG_ALLOW_MAX = "allows.maxwarps";
	// Metadata keys
	private static final String MDK_BACK_COOLDOWN = "back_timestamp";
	private static final String MDK_HOME_COOLDOWN = "home_timestamp";
	private static final String MDK_TP_COOLDOWN = "tp_timestamp";
	private static final String MDK_LAST_LOCATION = "last_location";

	private Language lg;
	private HomeHandler homes;
	private TPHandler tp;

	private boolean allowHome = true;
	private int warpMax = 0;

	/*
	 * Plugin setup
	 */

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		saveDefaultConfig();
		reloadConfig();
		refreshConfig();

		lg = new Language(this);

		homes = new HomeHandler(this, getConfig().getBoolean(CFG_USE_UUID));

		tp = new TPHandler(this);
	}

	@Override
	public void onDisable() {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("update")) {

			reloadConfig();
			refreshConfig();

			homes.reload();

			lg.reload();

			sender.sendMessage(ChatColor.GRAY + "[" + getName() + "] " + lg.get("updated"));
			return true;
		} else if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 0) {
				if (cmd.getName().equalsIgnoreCase(CMD_TPCANCEL)) {
					return cancelTp(player);
				} else if (cmd.getName().equalsIgnoreCase(CMD_TPACCEPT)) {
					return acceptTp(player);
				} else if (cmd.getName().equalsIgnoreCase(CMD_TPDENY)) {
					return denyTp(player);
				} else if (cmd.getName().equalsIgnoreCase(CMD_BACK)) {
					return goBack(player);
				} else if (cmd.getName().equalsIgnoreCase(CMD_LISTHOME)) {
					return listHome(player);
				} else if (cmd.getName().equalsIgnoreCase(CMD_SHOWHOME)) {
					return showHome(player, null);
				} else if (cmd.getName().equalsIgnoreCase(CMD_SETHOME)) {
					return setHome(player, null);
				} else if (cmd.getName().equalsIgnoreCase(CMD_DELHOME)) {
					return delHome(player, null);
				} else if (cmd.getName().equalsIgnoreCase(CMD_HOME)) {
					return goHome(player, null);
				}
			} else if (args.length == 1) {
				if (cmd.getName().equalsIgnoreCase(CMD_TPTO)) {
					return askTp(player, args[0]);
				} else if (cmd.getName().equalsIgnoreCase(CMD_TPHERE)) {
					return askTp(args[0], player);
				} else if (cmd.getName().equalsIgnoreCase(CMD_SETHOME)) {
					return setHome(player, args[0]);
				} else if (cmd.getName().equalsIgnoreCase(CMD_DELHOME)) {
					return delHome(player, args[0]);
				} else if (cmd.getName().equalsIgnoreCase(CMD_SHOWHOME)) {
					return showHome(player, args[0]);
				} else if (cmd.getName().equalsIgnoreCase(CMD_HOME)) {
					return goHome(player, args[0]);
				}
			}
		} else
			sender.sendMessage(ChatColor.RED + StringHandler.colorize(lg.get("only_player")));

		return false;
	}

	/*
	 * Events handlers
	 */

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent evt) {
		Player player = evt.getEntity();
		MetadataAccessor.setMetadata(this, player, MDK_LAST_LOCATION, player.getLocation());
	}

	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent evt) {
		Player player = evt.getPlayer();
		MetadataAccessor.setMetadata(this, player, MDK_LAST_LOCATION, player.getLocation());
	}

	/*
	 * onCommand fallbacks
	 */

	/////////
	// BACK
	/////////

	private boolean goBack(Player player) {
		Location loc = (Location) MetadataAccessor.getMetadata(this, player, MDK_LAST_LOCATION);
		if (loc != null) {
			if (Cooldown.test(this, player, MDK_BACK_COOLDOWN, CFG_COOLDOWN_BACK)) {
				TPHandler.teleport(player, loc);
				Cooldown.use(this, player, MDK_BACK_COOLDOWN);
			} else {
				Map<String, String> values = new HashMap<String, String>();
				values.put("time", "" + Cooldown.getTo(this, player, MDK_BACK_COOLDOWN, CFG_COOLDOWN_BACK));
				values.put("command", "/back");
				player.sendMessage(StringHandler.translate(lg.get("cooldown"), values));
			}
			return true;

		} else {
			player.sendMessage(StringHandler.colorize(lg.get("no_back")));
		}

		return false;
	}

	/////////
	// HOME
	/////////

	private boolean setHome(Player player, String warp) {
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

	private boolean delHome(Player player, String warp) {
		Map<String, String> values = new HashMap<String, String>();
		values.put("warp", warp == null ? HomeHandler.HOME : warp);

		homes.del(player, warp);
		player.sendMessage(StringHandler.translate(lg.get("home_del"), values));

		return true;
	}

	private boolean listHome(Player player) {
		Set<String> homeList = homes.getHomeList(player);

		player.sendMessage(ChatColor.AQUA + StringHandler.colorize(lg.get("list_home")));
		for (String home : homeList) {
			player.sendMessage(ChatColor.DARK_AQUA + home);
		}

		return true;
	}

	private boolean showHome(Player player, String warp) {
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

	private boolean goHome(Player player, String warp) {
		Location loc = null;

		Map<String, String> values = new HashMap<String, String>();
		values.put("warp", warp == null ? HomeHandler.HOME : warp);
		values.put("okhome", "" + allowHome);
		values.put("max", "" + warpMax);

		if (warp == null && allowHome == true || warp != null && warpMax != 0) {
			loc = homes.get(player, warp);
		}

		if (loc != null) {
			if (Cooldown.test(this, player, MDK_HOME_COOLDOWN, CFG_COOLDOWN_HOME)) {
				TPHandler.teleport(player, loc);
				player.sendMessage(StringHandler.translate(lg.get("home_go"), values));
				Cooldown.use(this, player, MDK_HOME_COOLDOWN);
			} else {
				values.put("time", "" + Cooldown.getTo(this, player, MDK_HOME_COOLDOWN, CFG_COOLDOWN_HOME));
				values.put("command", "/home");
				player.sendMessage(StringHandler.translate(lg.get("cooldown"), values));
			}
			return true;

		} else {
			player.sendMessage(StringHandler.translate(lg.get("home_go_error"), values));
		}

		return false;
	}

	/////////
	// TP
	/////////

	private boolean askTp(Player moved, String targetName) {
		return askTp(moved.getName(), targetName, true);
	}

	private boolean askTp(String movedName, Player target) {
		return askTp(movedName, target.getName(), false);
	}

	private boolean askTp(String movedName, String targetName, boolean askTarget) {
		Player moved = (Bukkit.getServer().getPlayer(movedName));
		Player target = (Bukkit.getServer().getPlayer(targetName));
		Player player = askTarget ? moved : target;
		Player other = askTarget ? target : moved;

		Map<String, String> values = new HashMap<String, String>();
		values.put("moved", movedName);
		values.put("target", targetName);
		values.put("you", player.getDisplayName());
		values.put("other", other.getDisplayName());

		if (target != null) {
			if (Cooldown.test(this, player, MDK_TP_COOLDOWN, CFG_COOLDOWN_TP)) {
				tp.askTp(moved, target, askTarget);
				Cooldown.use(this, player, MDK_HOME_COOLDOWN);
			} else {
				values.put("time", "" + Cooldown.getTo(this, player, MDK_HOME_COOLDOWN, CFG_COOLDOWN_HOME));
				values.put("command", "/tp");
				player.sendMessage(StringHandler.translate(lg.get("cooldown"), values));
			}
			return true;

		} else {
			moved.sendMessage(StringHandler.translate(lg.get("no_online"), values));
		}

		return false;
	}

	private boolean cancelTp(Player player) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean acceptTp(Player player) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean denyTp(Player player) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * Helpers
	 */

	private void refreshConfig() {
		allowHome = getConfig().getBoolean(CFG_ALLOW_HOME);
		warpMax = getConfig().getInt(CFG_ALLOW_MAX);
		if (!getConfig().getBoolean(CFG_ALLOW_WARP))
			warpMax = 0;
		else if (warpMax == 0)
			warpMax = Integer.MAX_VALUE;
	}

}
