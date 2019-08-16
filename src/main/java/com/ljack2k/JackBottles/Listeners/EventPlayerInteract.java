package com.ljack2k.JackBottles.Listeners;

import com.ljack2k.JackBottles.JackBottles;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class EventPlayerInteract implements Listener {
	private JackBottles plugin;

	private NamespacedKey keyPlugin;
	private NamespacedKey keyAmount;

	public EventPlayerInteract(JackBottles pl) {
		this.plugin = pl;

		keyPlugin = new NamespacedKey(plugin, "plugin");
		keyAmount = new NamespacedKey(plugin, "amount");

		plugin.debug("EventPlayerInteract Registered");
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		event.getPlayer().sendMessage(event.getAction().toString() + " was ze action");

		if (event.hasItem()) {
			ItemStack item = event.getItem();
			ItemMeta im = item.getItemMeta();

			if (im != null) {
				if (im.getPersistentDataContainer().has(keyPlugin, PersistentDataType.STRING) &&
						im.getPersistentDataContainer().has(keyAmount, PersistentDataType.DOUBLE)) {
					if (im.getPersistentDataContainer().get(keyPlugin, PersistentDataType.STRING).equals(plugin.getName())) {
						double amount = im.getPersistentDataContainer().get(keyAmount, PersistentDataType.DOUBLE);
						item.setAmount(item.getAmount() - 1);
						event.getPlayer().sendMessage("You are retrieving " + amount + "");
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
}
