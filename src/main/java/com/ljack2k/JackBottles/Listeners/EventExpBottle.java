package com.ljack2k.JackBottles.Listeners;

import com.ljack2k.JackBottles.JackBottles;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.persistence.PersistentDataType;

public class EventExpBottle implements Listener {

    private JackBottles plugin;

    private NamespacedKey keyPlugin;
    private NamespacedKey keyAmount;
    private NamespacedKey keyUUID;

    public EventExpBottle(JackBottles pl) {
        this.plugin = pl;

        keyPlugin = new NamespacedKey(plugin, "plugin");
        keyAmount = new NamespacedKey(plugin, "amount");
        keyUUID = new NamespacedKey(plugin, "UUID");

        plugin.debug("EventExpBottle Registered");
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onExpBottleEvent(ExpBottleEvent event) {
        /*
         Some how this doesn't work. there is no NBT data after launch.
         Alternative could be to stop the launch (different event) and launch it with code and add NBT?
        */

        // Is the item something that we are interested in?
        if (event.getEntity().getPersistentDataContainer().has(keyPlugin, PersistentDataType.STRING) &&
                event.getEntity().getPersistentDataContainer().has(keyAmount, PersistentDataType.INTEGER) &&
                event.getEntity().getPersistentDataContainer().has(keyUUID, PersistentDataType.STRING)) {

            // get amount stored in bottle
            int amount = event.getEntity().getPersistentDataContainer().get(keyAmount, PersistentDataType.INTEGER);
            event.setExperience(amount);
        }
    }
}
