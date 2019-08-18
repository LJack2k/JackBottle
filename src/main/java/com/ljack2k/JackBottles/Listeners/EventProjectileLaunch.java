package com.ljack2k.JackBottles.Listeners;

import com.ljack2k.JackBottles.JackBottles;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.persistence.PersistentDataType;

public class EventProjectileLaunch implements Listener {

    private JackBottles plugin;

    private NamespacedKey keyPlugin;
    private NamespacedKey keyAmount;
    private NamespacedKey keyUUID;

    public EventProjectileLaunch(JackBottles pl) {
        this.plugin = pl;

        keyPlugin = new NamespacedKey(plugin, "plugin");
        keyAmount = new NamespacedKey(plugin, "amount");
        keyUUID = new NamespacedKey(plugin, "UUID");

        plugin.debug("EventProjectileLaunch Registered");
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {

        /*
         Some how this doesn't work. there is no NBT data after launch.
         Alternative could be to stop the launch (different event) and launch it with code and add NBT?
        */

        // Get the projectile from the event
        Projectile projectile = event.getEntity();

        // Is it a XP bottle
        if (projectile instanceof ThrownExpBottle) {

            // Does it NBT data?
            if (projectile.getPersistentDataContainer().has(keyPlugin, PersistentDataType.STRING) &&
                    projectile.getPersistentDataContainer().has(keyAmount, PersistentDataType.INTEGER) &&
                    projectile.getPersistentDataContainer().has(keyUUID, PersistentDataType.STRING)) {

                // Is the NBT data for our plugin?
                if (projectile.getPersistentDataContainer().get(keyPlugin, PersistentDataType.STRING).equals(plugin.getName())) {

                    // Cancel any other action
                    event.setCancelled(true);
                }
            }
        }
    }
}
