package fr.telec.simpleTP;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.telec.simpleCore.Language;
import fr.telec.simpleCore.StringHandler;

public class TP {
	private static final String MDK_TP_COOLDOWN = "tp_timestamp";
	private static final String CFG_COOLDOWN_TP = "cooldown.tp";

	private TPHandler tp;
	private Language lg;
	private Cooldown cld;

	public TP(Language lg, Cooldown cld) {
		this.lg = lg;
		this.cld = cld;

		tp = new TPHandler();
	}

	/*
	 * Actions
	 */
	
	public boolean askTp(Player moved, String targetName) {
		return askTp(moved.getName(), targetName, true);
	}

	public boolean askTp(String movedName, Player target) {
		return askTp(movedName, target.getName(), false);
	}
	
	public boolean cancelTp(Player player) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean acceptTp(Player player) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean denyTp(Player player) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * Helpers
	 */
	
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
			if (cld.test(player, MDK_TP_COOLDOWN, CFG_COOLDOWN_TP)) {
				tp.askTp(moved, target, askTarget);
				cld.use(player, MDK_TP_COOLDOWN);
			} else {
				values.put("time", "" + cld.getTo(player, MDK_TP_COOLDOWN, CFG_COOLDOWN_TP));
				values.put("command", "/"+Commands.TP);
				player.sendMessage(StringHandler.translate(lg.get("cooldown"), values));
			}
			return true;

		} else {
			moved.sendMessage(StringHandler.translate(lg.get("no_online"), values));
		}

		return false;
	}


}