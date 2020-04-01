package fr.telec.simpleTP;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.telec.simpleCore.Language;
import fr.telec.simpleCore.MetadataAccessor;
import fr.telec.simpleCore.StringHandler;

public class TP {
	private static final String MDK_TP_COOLDOWN = "tp_timestamp";
	private static final String CFG_COOLDOWN_TP = "cooldown.tp";
	private static final String MDK_INVOLVED = "is_involved_in_teleport";
	private static final String MDK_IS_ASKING = "is_asking_to_teleport";
	private static final String MDK_IS_TARGET = "is_target_to_teleport";
	private static final String MDK_OTHER = "other_player_to_teleport";

	private JavaPlugin plugin;
	private Language lg;
	private Cooldown cld;

	public TP(JavaPlugin plugin, Language lg, Cooldown cld) {
		this.plugin = plugin;
		this.lg = lg;
		this.cld = cld;
	}

	/*
	 * Actions
	 */

	public boolean ask(Player moved, String targetName) {
		return testNask(moved.getName(), targetName, true);
	}

	public boolean ask(String movedName, Player target) {
		return testNask(movedName, target.getName(), false);
	}

	public boolean cancel(Player player) {
		if (isInvolved(player) && isAsking(player)) {
			Player other = getOther(player);
			setInvolved(player, false);
			setInvolved(other, false);

			player.sendMessage(StringHandler.colorize(lg.get("canceled")));
			other.sendMessage(StringHandler.colorize(lg.get("canceled")));

			return true;

		} else {
			player.sendMessage(StringHandler.colorize(lg.get("cant_cancel")));
		}

		return false;
	}

	public boolean accept(Player player) {
		if (isInvolved(player) && !isAsking(player)) {
			Player other = getOther(player);
			setInvolved(player, false);
			setInvolved(other, false);

			player.sendMessage(StringHandler.colorize(lg.get("accepted")));
			other.sendMessage(StringHandler.colorize(lg.get("accepted")));

			if (isTarget(player))
				TPHandler.teleport(other, player);
			else
				TPHandler.teleport(player, other);

			return true;

		} else {
			player.sendMessage(StringHandler.colorize(lg.get("cant_accept")));
		}

		return false;
	}

	public boolean deny(Player player) {
		if (isInvolved(player) && !isAsking(player)) {
			Player other = getOther(player);
			setInvolved(player, false);
			setInvolved(other, false);

			player.sendMessage(StringHandler.colorize(lg.get("denied")));
			other.sendMessage(StringHandler.colorize(lg.get("denied")));

			return true;

		} else {
			player.sendMessage(StringHandler.colorize(lg.get("cant_deny")));
		}

		return false;
	}

	/*
	 * Helpers
	 */

	private boolean testNask(String movedName, String targetName, boolean askTarget) {
		Player moved = (Bukkit.getServer().getPlayer(movedName));
		Player target = (Bukkit.getServer().getPlayer(targetName));
		Player player = askTarget ? moved : target;
		Player other = askTarget ? target : moved;

		Map<String, String> values = new HashMap<String, String>();
		values.put("accept", Commands.TPACCEPT);
		values.put("deny", Commands.TPDENY);
		values.put("moved", movedName);
		values.put("target", targetName);
		values.put("you", player.getDisplayName());

		if (isInvolved(player)) {
			moved.sendMessage(StringHandler.translate(lg.get("player_involved"), values));
			return false;
		}

		if (other == null) {
			moved.sendMessage(StringHandler.translate(lg.get("no_online"), values));
			return false;
		}
		values.put("other", other.getDisplayName());

		if (isInvolved(player)) {
			moved.sendMessage(StringHandler.translate(lg.get("other_involved"), values));
			return false;
		}

		if (cld.test(player, MDK_TP_COOLDOWN, CFG_COOLDOWN_TP)) {
			setInvolved(moved, true);
			setInvolved(target, true);

			setAsking(player, true);
			setAsking(other, false);

			setTarget(moved, false);
			setTarget(target, true);

			setOther(moved, target);
			setOther(target, moved);

			if (isTarget(other))
				other.sendMessage(StringHandler.translate(lg.get("tpto"), values));
			else
				other.sendMessage(StringHandler.translate(lg.get("tphere"), values));
			
			player.sendMessage(StringHandler.translate(lg.get("sended"), values));

			cld.use(player, MDK_TP_COOLDOWN);
		} else {
			values.put("time", "" + cld.getTo(player, MDK_TP_COOLDOWN, CFG_COOLDOWN_TP));
			values.put("command", "/" + Commands.TP);
			player.sendMessage(StringHandler.translate(lg.get("cooldown"), values));
		}
		return true;
	}

	private boolean isInvolved(Player player) {
		Object asking = MetadataAccessor.getMetadata(plugin, player, MDK_INVOLVED);
		return asking != null ? ((boolean) asking) : false;
	}

	private void setInvolved(Player player, boolean involved) {
		MetadataAccessor.setMetadata(plugin, player, MDK_INVOLVED, involved);
	}

	private boolean isAsking(Player player) {
		Object asking = MetadataAccessor.getMetadata(plugin, player, MDK_IS_ASKING);
		return asking != null ? ((boolean) asking) : null;
	}

	private void setAsking(Player player, boolean asking) {
		MetadataAccessor.setMetadata(plugin, player, MDK_IS_ASKING, asking);
	}

	private boolean isTarget(Player player) {
		Object target = MetadataAccessor.getMetadata(plugin, player, MDK_IS_TARGET);
		return target != null ? ((boolean) target) : null;
	}

	private void setTarget(Player player, boolean target) {
		MetadataAccessor.setMetadata(plugin, player, MDK_IS_TARGET, target);
	}

	private Player getOther(Player player) {
		Object other = MetadataAccessor.getMetadata(plugin, player, MDK_OTHER);
		return other != null ? ((Player) other) : null;
	}

	private void setOther(Player player, Player other) {
		MetadataAccessor.setMetadata(plugin, player, MDK_OTHER, other);
	}

}