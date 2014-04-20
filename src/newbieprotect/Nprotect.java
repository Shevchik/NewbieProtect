package newbieprotect;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Nprotect extends JavaPlugin {

	private Config config;
	private Storage storage;
	private EventsListener listener;

	@Override
	public void onEnable() {
		setNaggable(false);
		config = new Config(this);
		config.loadConfig();
		storage = new Storage(this, config);
		storage.loadTimeConfig();
		storage.startCheck();
		listener = new EventsListener(config, storage);
		getServer().getPluginManager().registerEvents(listener, this);
	}

	@Override()
	public void onDisable() {
		HandlerList.unregisterAll(this);
		listener = null;
		storage.stopCheck();
		storage.saveTimeConfig();
		storage = null;
		config = null;
	}

}
