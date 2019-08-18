package com.ljack2k.JackBottles.Listeners;

import com.ljack2k.JackBottles.JackBottles;
import com.ljack2k.JackBottles.Utils.LangUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class EventBlockDispense implements Listener {

    private JackBottles plugin;

    private NamespacedKey keyPlugin;
    private NamespacedKey keyAmount;
    private NamespacedKey keyUUID;

    public EventBlockDispense(JackBottles pl) {
        this.plugin = pl;

        keyPlugin = new NamespacedKey(plugin, "plugin");
        keyAmount = new NamespacedKey(plugin, "amount");
        keyUUID = new NamespacedKey(plugin, "UUID");

        plugin.debug("EventBlockDispense Registered");
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onBlockDispense(BlockDispenseEvent event) {

        // Dispensing an item?
        if (event.getItem() == null) {
            return;
        }

        // Only block dispensers
        if (event.getBlock().getType() != Material.DISPENSER) {
            return;
        }

        // get the item
        ItemStack itemStack = event.getItem();

        // Is the item something that we are interested in?
        if (itemStack.getType() != plugin.getStoredItem()) {
            return;
        }

        // Get the meta data
        ItemMeta itemMeta = itemStack.getItemMeta();


        // Does it NBT data?
        if (itemMeta.getPersistentDataContainer().has(keyPlugin, PersistentDataType.STRING) &&
                itemMeta.getPersistentDataContainer().has(keyAmount, PersistentDataType.INTEGER) &&
                itemMeta.getPersistentDataContainer().has(keyUUID, PersistentDataType.STRING)) {

            // Is the NBT data for our plugin?
            if (itemMeta.getPersistentDataContainer().get(keyPlugin, PersistentDataType.STRING).equals(plugin.getName())) {

                // Cancel any other action
                event.setCancelled(true);
            }
        }

    }

}
