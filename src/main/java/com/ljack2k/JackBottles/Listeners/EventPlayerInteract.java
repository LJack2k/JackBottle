package com.ljack2k.JackBottles.Listeners;

import com.ljack2k.JackBottles.JackBottles;
import javafx.scene.layout.Priority;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class EventPlayerInteract implements Listener {
	private JackBottles plugin;

	private NamespacedKey keyPlugin;
	private NamespacedKey keyAmount;
	private NamespacedKey keyUUID;

	public EventPlayerInteract(JackBottles pl) {
		this.plugin = pl;

		keyPlugin = new NamespacedKey(plugin, "plugin");
		keyAmount = new NamespacedKey(plugin, "amount");
		keyUUID = new NamespacedKey(plugin, "UUID");

		plugin.debug("EventPlayerInteract Registered");
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {

		// No block click and player needs to have an item in hand
		if (event.getItem() == null) {
			return;
		}

		// Is the item something that we are interested in?
		if (event.getItem().getType() != plugin.getStoredItem()) {
			return;
		}

		// Get the Item
		ItemStack is = event.getItem();
		ItemMeta im = is.getItemMeta();

		// Does it NBT data?
		if (im.getPersistentDataContainer().has(keyPlugin, PersistentDataType.STRING) &&
				im.getPersistentDataContainer().has(keyAmount, PersistentDataType.INTEGER) &&
				im.getPersistentDataContainer().has(keyUUID, PersistentDataType.STRING)) {

			// Is the NBT data for our plugin?
			if (im.getPersistentDataContainer().get(keyPlugin, PersistentDataType.STRING).equals(plugin.getName())) {

				// Get the amount stored in the item
				Integer amount = im.getPersistentDataContainer().get(keyAmount, PersistentDataType.INTEGER);

				// Give all the XP
				event.getPlayer().giveExp(amount);

				// Remove used item
				is.setAmount(is.getAmount() - 1);

				// Cancel any other action
				event.setCancelled(true);
			}
		}
	}
	
}
