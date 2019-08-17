package com.ljack2k.JackBottles.Listeners;

import com.ljack2k.JackBottles.JackBottles;
import com.ljack2k.JackBottles.Utils.LangUtil;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class EventPlayerCreateXPBottle implements Listener {
    private JackBottles plugin;

    private NamespacedKey keyPlugin;
    private NamespacedKey keyAmount;
    private NamespacedKey keyUUID;

    public EventPlayerCreateXPBottle(JackBottles pl) {
        this.plugin = pl;

        keyPlugin = new NamespacedKey(plugin, "plugin");
        keyAmount = new NamespacedKey(plugin, "amount");
        keyUUID = new NamespacedKey(plugin, "UUID");

        plugin.debug("EventPlayerCreateXPBottle Registered");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        // No block click and player needs to have an item in hand and sneaking
        if (event.getAction() == Action.RIGHT_CLICK_AIR && event.getItem() != null && event.getPlayer().isSneaking()) {
            // get Player
            Player player = event.getPlayer();

            // Is the item something that we are interested in?
            if (event.getItem().getType() == plugin.getBaseItem() && event.getItem().getAmount() == 1) {

                // Player needs to have enough Experience
                if (player.getTotalExperience() > plugin.getConfig().getInt("ExperienceAmountPerClick")) {

                    // Create new item
                    ItemStack itemStack = new ItemStack(plugin.getStoredItem());

                    // Get meta data
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    // Set display name
                    itemMeta.setDisplayName(ChatColor.GREEN + "" + LangUtil.Message.XP_BOTTLE_NAME);

                    // Create lore list
                    List<String> loreList = new ArrayList<>();

                    // Stored amount
                    int amount = plugin.getConfig().getInt("ExperienceAmountPerClick");
                    loreList.add(ChatColor.AQUA + "" + LangUtil.Message.STORED_XP + ": " + ChatColor.GREEN + amount);

                    // Empty row
                    loreList.add("");

                    // Use lore last
                    loreList.add(ChatColor.YELLOW + String.format("" + LangUtil.Message.XP_BOTTLE_SHIFT_RIGHT_CLICK_USE, amount));
                    loreList.add(ChatColor.YELLOW + String.format("" + LangUtil.Message.XP_BOTTLE_SHIFT_LEFT_CLICK_USE, amount));
                    loreList.add(ChatColor.YELLOW + String.format("" + LangUtil.Message.XP_BOTTLE_USE_TROW, amount));

                    // Empty row
                    loreList.add("");

                    // UUID row
                    UUID uuid = UUID.randomUUID();
                    loreList.add(ChatColor.DARK_GRAY + uuid.toString());

                    // Set lore to meta data
                    itemMeta.setLore(loreList);

                    // Set NBT data
                    itemMeta.getPersistentDataContainer().set(keyPlugin, PersistentDataType.STRING, plugin.getName());
                    itemMeta.getPersistentDataContainer().set(keyAmount, PersistentDataType.INTEGER, amount);
                    itemMeta.getPersistentDataContainer().set(keyUUID, PersistentDataType.STRING, uuid.toString());

                    // Apply lore and NBT to item
                    itemStack.setItemMeta(itemMeta);

                    player.setTotalExperience(player.getTotalExperience() - amount);
                    player.setItemOnCursor(itemStack);
                } else {
                    plugin.sendChatMessage(event.getPlayer(), ChatColor.RED + "" + LangUtil.Message.NOT_ENOUGH_XP_TO_STORE);
                }
            }
        }
    }

    public boolean givePlayerItem(Player player, ItemStack item)
    {
        // If there are not free inventory slots
        if(player.getInventory().firstEmpty() == -1)
        {
            // Attempt to add items to the inventory, if it fails then return the items it could not place
            Map<Integer,ItemStack> couldNotStore = player.getInventory().addItem(item);

            // Check the map of items it could not place is empty
            if(couldNotStore.isEmpty()) {
                // Item has been stacked
                return true;
            }
            // Can't stack item either
            return false;
        }
        else
        {
            // There is space so give the item
            player.getInventory().addItem(item);
            return true;
        }
    }

}
