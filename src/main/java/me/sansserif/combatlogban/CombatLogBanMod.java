package me.sansserif.combatlogban;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CombatLogBanMod extends Thread implements DedicatedServerModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("combatlogban");
	private static final SimpleConfig CONFIG = SimpleConfig.of("combatlogban").request();
	private static final int hitTimeout = CONFIG.getOrDefault("hitTimeout", 7);
	private static final int banMinutes = CONFIG.getOrDefault("banMinutes", 1440);
	private static boolean threadrunning = false;
	private static Thread t;
	private static List<PlayerRecord> timeouts = new ArrayList<>();
	private static List<PlayerRecord> bans = new ArrayList<>();

	@Override
	public void onInitializeServer() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("CombatLogBan starting.");
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
			LOGGER.info("CombatLogBan started!");
		});
		//register hits
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (entity.isPlayer()) {
				//send warning
				boolean already1 = false;
				boolean already2 = false;
				for (int i = 0; i != timeouts.size(); i++) {
					if (timeouts.get(i).getPlayer().getName().asString().equals(player.getName().asString()))
						already1 = true;
					if (timeouts.get(i).getPlayer().getName().asString().equals(entity.getName().asString()))
						already2 = true;
				}
				if (!already1) player.sendMessage(Text.of(Formatting.RED + "You attacked someone! Do NOT log off or you will get banned."), false);
				if (!already2) ((PlayerEntity) entity).sendMessage(Text.of(Formatting.RED + "You were attacked by someone! Do NOT log off or you will get banned."), false);
				// add player cooldown
				timeouts.add(new PlayerRecord(player, hitTimeout));
				timeouts.add(new PlayerRecord((PlayerEntity) entity, hitTimeout));
				// start thread if not running
				if (!threadrunning) {
					t = new Thread(new CombatLogBanMod());
					t.start();
				}
			}
			return ActionResult.PASS;
		});
		//register leave (to ban players)
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			boolean ban = false;
			for (int i = 0; i != timeouts.size(); i++) {
				if (timeouts.get(i).getPlayer().getName().asString().equals(handler.getPlayer().getName().asString()))
					ban = true;
			}
			if (ban) {
				//ban
				LOGGER.info("Player " + handler.getPlayer().getName().asString() + " was banned for " + banMinutes + " minutes for logging off midfight.");
				bans.add(new PlayerRecord(handler.getPlayer(), banMinutes));
				//start thread
				if (!BanThread.brunning) {
					BanThread.t = new Thread(new BanThread());
					BanThread.t.start();
				}
			}
		});
		ServerPlayConnectionEvents.INIT.register(((handler, server) -> {
			boolean disconnect = false;
			for (int i = 0; i != bans.size(); i++) {
				if (bans.get(i).getPlayer().getName().asString().equals(handler.getPlayer().getName().asString()))
					disconnect = true;
			}
			if (disconnect)
				handler.disconnect(Text.of(Formatting.RED + "You were temporarily banned for logging off midfight!"));
		}));
	}
	//thread to timeout hits
	@Override
	public void run() {
		threadrunning = true;
		while (!timeouts.isEmpty()) {
			try {
				Thread.sleep(TimeUnit.SECONDS.toMillis(1));
				List<PlayerRecord> temp = new ArrayList<>();
				timeouts.forEach((timeout) -> {
					timeout.oneLess();
					if (timeout.getUnit() <= 0) {
						temp.add(timeout);
					}
				});
				timeouts.removeAll(temp);
				List<String> sentmsg = new ArrayList<>();
				for (int i = 0; i != temp.size(); i++) {
					boolean incombat = false;
					for (int a = 0; a != timeouts.size(); a++) {
						if (timeouts.get(a).getPlayer().getName().asString().equals(temp.get(i).getPlayer().getName().asString()))
							incombat = true;
					}
					if (!incombat)
						if (!sentmsg.contains(temp.get(i).getPlayer().getName().asString())) {
							temp.get(i).getPlayer().sendMessage(Text.of(Formatting.GREEN + "You can now safely log off."), false);
							sentmsg.add(temp.get(i).getPlayer().getName().asString());
						}
				}
			} catch (InterruptedException ignored) {}
		}
		threadrunning = false;
	}
	private static class BanThread implements Runnable {
		private static Thread t;
		private static boolean brunning = false;
		@Override
		public void run() {
			brunning = true;
			while (!bans.isEmpty()) {
				try {
					Thread.sleep(TimeUnit.MINUTES.toMillis(1));
					List<PlayerRecord> temp = new ArrayList<>();
					bans.forEach((ban) -> {
						ban.oneLess();
						if (ban.getUnit() <= 0) {
							temp.add(ban);
						}
					});
					bans.removeAll(temp);
				} catch (InterruptedException ignored) {}
			}
			brunning = false;
		}
	}






	//class for storing player records, i just dont like using hashmaps :)
	private static class PlayerRecord {
		private final PlayerEntity pl;
		private int anyunit;
		public PlayerRecord(PlayerEntity p, int s) {
			pl = p;
			anyunit = s;
		}
		public void oneLess() {
			anyunit--;
		}
		public int getUnit() {
			return anyunit;
		}
		public PlayerEntity getPlayer() {
			return pl;
		}
	}
}
