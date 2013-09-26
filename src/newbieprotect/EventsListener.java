package newbieprotect;

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventsListener implements Listener {

	private Storage storage;
	private Config config;
	public EventsListener(Config config, Storage storage)
	{
		this.config = config;
		this.storage = storage;
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if (!event.getPlayer().hasPlayedBefore())
		{
			storage.protectPlayer(event.getPlayer().getName(), System.currentTimeMillis());
			event.getPlayer().sendMessage(config.protectMessage);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void onEntityDamageEvent(EntityDamageByEntityEvent event)
	{
		Entity eattacker = event.getDamager();
		Entity eentity = event.getEntity();
		Player attacker = null;
		Player entity = null;
		//get attacker
		if (eattacker instanceof Player)
		{
			attacker = (Player) eattacker;
		} else
		if (eattacker instanceof Arrow)
		{
			LivingEntity shooter = ((Arrow) eattacker).getShooter();
			if (shooter != null && shooter instanceof Player)
			{
				attacker = (Player) shooter;
			}
		}
		//get damaged entiity
		if (eentity instanceof Player)
		{
			entity = (Player) eentity;
		}
		//now check
		if (attacker != null && entity != null)
		{
			if (storage.isPlayerProtected(entity.getName()))
			{
				event.setCancelled(true);
				attacker.sendMessage(config.youCantAttackProtected);
			}
			if (storage.isPlayerProtected(attacker.getName()))
			{
				event.setCancelled(true);
				attacker.sendMessage(config.youCantAttackWhileProtected);
			}
			
		}
	}
	
	
	// i'm too lazy to write normal command handler
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event)
	{
		String message = event.getMessage();
		Player player = event.getPlayer();
		if (message.equalsIgnoreCase("/pvp-on") && storage.isPlayerProtected(player.getName()))
		{
			storage.unprotectPlayer(player.getName());
			player.sendMessage(config.unprotectMessage);
			event.setCancelled(true);
		} else
		if (message.equalsIgnoreCase("/nprotect reload") && player.hasPermission("nprotect.reload"))
		{
			config.loadConfig();
			player.sendMessage(ChatColor.GREEN+"Config reloaded");
			event.setCancelled(true);
		}
	}
	
}
