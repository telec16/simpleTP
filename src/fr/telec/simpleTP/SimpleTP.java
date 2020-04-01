package fr.telec.simpleTP;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.telec.simpleCore.Language;
import fr.telec.simpleCore.StringHandler;

public class SimpleTP extends JavaPlugin {

	private Language lg;
	private Cooldown cld;
	private Back back;
	private Home home;
	private TP tp;

	/*
	 * Plugin setup
	 */

	@Override
	public void onEnable() {
		saveDefaultConfig();
		reloadConfig();

		lg = new Language(this);
		cld = new Cooldown(this);

		back = new Back(this, lg, cld);
		home = new Home(this, lg, cld);
		tp = new TP(lg, cld);
	}

	@Override
	public void onDisable() {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("update")) {

			reloadConfig();

			home.reload();
			lg.reload();

			sender.sendMessage(ChatColor.GRAY + "[" + getName() + "] " + lg.get("updated"));
			return true;
		} else if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 0) {
				if (cmd.getName().equalsIgnoreCase(Commands.TPCANCEL)) {
					return tp.cancelTp(player);
				} else if (cmd.getName().equalsIgnoreCase(Commands.TPACCEPT)) {
					return tp.acceptTp(player);
				} else if (cmd.getName().equalsIgnoreCase(Commands.TPDENY)) {
					return tp.denyTp(player);
				} else if (cmd.getName().equalsIgnoreCase(Commands.BACK)) {
					return back.go(player);
				} else if (cmd.getName().equalsIgnoreCase(Commands.LISTHOME)) {
					return home.list(player);
				} else if (cmd.getName().equalsIgnoreCase(Commands.SHOWHOME)) {
					return home.show(player, null);
				} else if (cmd.getName().equalsIgnoreCase(Commands.SETHOME)) {
					return home.set(player, null);
				} else if (cmd.getName().equalsIgnoreCase(Commands.DELHOME)) {
					return home.del(player, null);
				} else if (cmd.getName().equalsIgnoreCase(Commands.HOME)) {
					return home.go(player, null);
				}
			} else if (args.length == 1) {
				if (cmd.getName().equalsIgnoreCase(Commands.TPTO)) {
					return tp.askTp(player, args[0]);
				} else if (cmd.getName().equalsIgnoreCase(Commands.TPHERE)) {
					return tp.askTp(args[0], player);
				} else if (cmd.getName().equalsIgnoreCase(Commands.SETHOME)) {
					return home.set(player, args[0]);
				} else if (cmd.getName().equalsIgnoreCase(Commands.DELHOME)) {
					return home.del(player, args[0]);
				} else if (cmd.getName().equalsIgnoreCase(Commands.SHOWHOME)) {
					return home.show(player, args[0]);
				} else if (cmd.getName().equalsIgnoreCase(Commands.HOME)) {
					return home.go(player, args[0]);
				}
			}
		} else
			sender.sendMessage(ChatColor.RED + StringHandler.colorize(lg.get("only_player")));

		return false;
	}
}
