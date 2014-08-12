package newbieprotect;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;

public class Storage {

	private Nprotect plugin;
	private Config config;
	private File configfile;

	public Storage(Nprotect plugin, Config config) {
		this.plugin = plugin;
		this.config = config;
		configfile = new File(plugin.getDataFolder(), "data.yml");
	}

	private ConcurrentHashMap<UUID, Long> playerprotecttime = new ConcurrentHashMap<UUID, Long>();

	protected void protectPlayer(Player player, long starttimestamp) {
		playerprotecttime.put(player.getUniqueId(), starttimestamp);
	}

	protected void unprotectPlayer(Player player) {
		playerprotecttime.remove(player.getUniqueId());
	}

	protected void unprotectPlayer(UUID uuid) {
		playerprotecttime.remove(uuid);
	}

	protected boolean isPlayerProtected(Player player) {
		if (config.disabledWorlds.contains(player.getWorld().getName())) {
			return false;
		}
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
			try {
				List<String> aregions = WGBukkit.getRegionManager(player.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(player.getLocation()));
				for (String region : aregions) {
					if (config.disabledWGregions.contains(region)) {
						return false;
					}
				}
			} catch (Exception e) {
			}
		}
		return playerprotecttime.containsKey(player.getUniqueId());
	}

	protected void loadTimeConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(configfile);
		ConfigurationSection cs = config.getConfigurationSection("");
		if (cs != null) {
			for (String uuid : cs.getKeys(false)) {
				playerprotecttime.put(UUID.fromString(uuid), config.getLong(uuid));
			}
		}
	}

	protected void saveTimeConfig() {
		FileConfiguration config = new YamlConfiguration();
		for (Entry<UUID, Long> entry : playerprotecttime.entrySet()) {
			config.set(entry.getKey().toString(), entry.getValue());
		}
		try {
			config.save(configfile);
		} catch (IOException e) {
		}
	}

	private int taskid;

	protected void startCheck() {
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(
			plugin,
			new Runnable() {
				@Override
				public void run() {
					for (UUID uuid : new HashSet<UUID>(playerprotecttime.keySet())) {
						if (System.currentTimeMillis() - playerprotecttime.get(uuid) > config.protecttime) {
							unprotectPlayer(uuid);
						}
					}
				}
			}, 
			0, 20 * 60
		);
	}

	protected void stopCheck() {
		Bukkit.getScheduler().cancelTask(taskid);
	}

}
