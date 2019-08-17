package com.ljack2k.JackBottles.Listeners;

import com.ljack2k.JackBottles.JackBottles;
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
		if (event.getClickedBlock() == null && event.getItem() != null) {

			// get Player
			Player player = event.getPlayer();

			// Is the item something that we are interested in?
			if (event.getItem().getType() == plugin.getStoredItem()) {

				// Get the Item
				ItemStack is = event.getItem();
				ItemMeta im = is.getItemMeta();

				// Does it NBT data?
				if (im.getPersistentDataContainer().has(keyPlugin, PersistentDataType.STRING) &&
						im.getPersistentDataContainer().has(keyAmount, PersistentDataType.DOUBLE) &&
						im.getPersistentDataContainer().has(keyUUID, PersistentDataType.STRING)) {

					// Is the NBT data for our plugin?
					if (im.getPersistentDataContainer().get(keyPlugin, PersistentDataType.STRING).equals(plugin.getName())) {

						// Get the amount stored in the item
						Integer amount = im.getPersistentDataContainer().get(keyAmount, PersistentDataType.INTEGER);
						Integer store = plugin.getConfig().getInt("ExperienceAmountPerClick");

						// Check the mouse click type
						if (event.getAction() == Action.LEFT_CLICK_AIR) {

							// STORING XP
							if (player.isSneaking()) {
								// Store specific amount
								plugin.debug("Left Click + Player is sneaking!");

								// Check if player has the xp
								if (player.getTotalExperience() < store) {

									// Calculate new amount
									amount = amount + player.getTotalExperience();

									// Set player new experience
									player.setTotalExperience(0);

									// Store it in the item
									im.getPersistentDataContainer().set(keyAmount, PersistentDataType.INTEGER, amount);
								} else {
									// Calculate new amount
									amount = amount + store;

									// Set player new experience
									player.setTotalExperience(player.getTotalExperience() - store);

									// Store it in the item
									im.getPersistentDataContainer().set(keyAmount, PersistentDataType.INTEGER, amount);
								}

							// RETRIEVING XP
							} else if (player.isSprinting()){
								// Retrieve specific amount
								plugin.debug("Left Click + Player is sprinting!");

								// Remainder XP
								if (amount < store) {

									// delete the bottle
									player.giveExp(amount);

									// Create the base item again
									ItemStack itemStack = new ItemStack(plugin.getBaseItem());

									// remove jack bottle
									event.getItem().setAmount(0);

									// Replace it with the jack bottle
									player.getInventory().setItemInMainHand(itemStack);


								} else {

									// give the standard amount of xp
									player.giveExp(store);

									// update the jack bottle
									im.getPersistentDataContainer().set(keyAmount, PersistentDataType.INTEGER, amount - store);
									is.setItemMeta(im);

									// update the inventory
									player.getInventory().setItemInMainHand(is);

								}

							}
						}

					}
				}
			}
		}
	}
	
}
