package com.ljack2k.JackBottles.Commands;

import com.ljack2k.JackBottles.JackBottles;
import com.ljack2k.JackBottles.Utils.LangUtil;
import com.ljack2k.JackBottles.Utils.SetExpFix;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.*;

public class CommandWithdrawXP implements CommandExecutor {
    private JackBottles plugin;
    private NamespacedKey keyPlugin;
    private NamespacedKey keyAmount;
    private NamespacedKey keyUUID;

    public CommandWithdrawXP(JackBottles pl) {
        plugin = pl;

        keyPlugin = new NamespacedKey(plugin, "plugin");
        keyAmount = new NamespacedKey(plugin, "amount");
        keyUUID = new NamespacedKey(plugin, "UUID");

        JackBottles.debug("CommandwithdrawxpXP Registered");
    }

    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        // Send help to who needs it
        if (args.length > 0) {
            if (args[0].equals("?") || args[0].equals("help")) {
                sendCommandHelp(sender);
                return true;
            }
        }

        // Only players can do this command
        if (!(sender instanceof Player)) {
            plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NOT_INGAME_PLAYER);
            return false;
        }

        // Get player
        Player player = ((Player) sender).getPlayer();

        // Does the player have permissions to use this command
        if (!player.hasPermission(plugin.getBasePermissionNode() + ".withdrawxp")) {
            plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NO_COMMAND_PERMISSION);
            return false;
        }

        // Check if arguments where given
        if (args.length == 0) {
            // No amount given
            plugin.sendChatMessage(sender, ChatColor.RED + LangUtil.Message.EXPERIENCE_AMOUNT_ERROR.toString());
            plugin.sendChatMessage(sender, ChatColor.GREEN + String.format(LangUtil.Message.YOU_CURRENT_XP_AMOUNT.toString(), SetExpFix.getTotalExperience(player)));
            return true;
        }

        int amount;

        if(args[0] == "all") {
            // Get all the XP from the player
            amount = SetExpFix.getTotalExperience(player);
        } else {

            // Get given amount and check if it is actually a number
            try {
                amount = Integer.parseInt(args[0]);
            } catch (NumberFormatException | NullPointerException nfe) {
                plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.EXPERIENCE_AMOUNT_ERROR);
                plugin.sendChatMessage(sender, ChatColor.GREEN + String.format(LangUtil.Message.YOU_CURRENT_XP_AMOUNT.toString(), SetExpFix.getTotalExperience(player)));
                return false;
            }
        }

        // Negative amounts are not allowed
        if (amount < plugin.getConfig().getInt("MinimumAmount")) {
            // No negative amounts
            plugin.sendChatMessage(player, ChatColor.RED + "" + String.format("" + LangUtil.Message.EXPERIENCE_AMOUNT_ERROR, 1));
            plugin.sendChatMessage(sender, ChatColor.GREEN + String.format(LangUtil.Message.YOU_CURRENT_XP_AMOUNT.toString(), SetExpFix.getTotalExperience(player)));
            return false;
        }

        // Check if player can afford it
        if (SetExpFix.getTotalExperience(player) < amount) {
            // Not enough xp
            plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NOT_ENOUGH_XP_TO_STORE);
            plugin.sendChatMessage(sender, ChatColor.GREEN + String.format(LangUtil.Message.YOU_CURRENT_XP_AMOUNT.toString(), SetExpFix.getTotalExperience(player)));
            return false;
        }

        // Create itemstack from baseitem
        ItemStack baseItemStack = new ItemStack(plugin.getBaseItem());

        // Set amount
        baseItemStack.setAmount(1);

        // Search player inventory for the item
        if (!findPlayerItem(player, baseItemStack)) {
            plugin.sendChatMessage(sender, ChatColor.RED + LangUtil.Message.BASE_ITEM_NEEDED.toString());
            return false;
        }

        // Remove one base item from the inventory
        player.getInventory().removeItem(baseItemStack);

        // Create new item
        ItemStack itemStack = new ItemStack(plugin.getStoredItem());

        // Get meta data
        ItemMeta itemMeta = itemStack.getItemMeta();

        // Set display name
        itemMeta.setDisplayName(ChatColor.GREEN + "" + LangUtil.Message.XP_BOTTLE_NAME);

        // Create lore list
        List<String> loreList = new ArrayList<>();
        loreList.add(ChatColor.AQUA + "" + LangUtil.Message.STORED_XP + ": " + ChatColor.GOLD + amount);

        // Use lore last
        loreList.add(ChatColor.YELLOW + String.format("" + LangUtil.Message.XP_BOTTLE_RIGHT_CLICK_USE, amount));

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

        // Attempt to give the item
        if (givePlayerItem(player, itemStack)) {
            // Successfully given the item, now do the xp transaction
            SetExpFix.setTotalExperience(player, (SetExpFix.getTotalExperience(player) - amount));
        } else {
            // Can't give the item
            plugin.sendChatMessage(player, ChatColor.RED + "" + LangUtil.Message.YOUR_INVENTORY_FULL);
        }

        return true;
    }

    public void sendCommandHelp(CommandSender sender) {
        if (sender instanceof Player) {
            plugin.sendChatHeader(sender, LangUtil.Message.HELP_WITHDRAWXP_HEADER.toString());
            plugin.sendChatMessage(sender, LangUtil.Message.HELP_WITHDRAWXP.toString());
            plugin.sendChatMessage(sender, ChatColor.YELLOW + "/withdrawxp " + ChatColor.GREEN + "all");
            plugin.sendChatMessage(sender, ChatColor.YELLOW + "/withdrawxp " + ChatColor.GREEN + "<amount>");
        } else {
            plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NOT_INGAME_PLAYER);
        }
    }

    public boolean findPlayerItem(Player player, ItemStack item) {
        return findPlayerItem(player, item, 1);
    }

    public boolean findPlayerItem(Player player, ItemStack item, Integer qty) {
        return player.getInventory().containsAtLeast(item, qty);
    }

    /**
     * Give Player the item only if it can stack or if there space in the inventory and returns true. This will
     * not drop the item. When it can't give the item, the plugin will return false;
     *
     * @param player Player to give the item to
     * @param item The item to give
     * @return boolean Success or not
     *
     */
    public boolean givePlayerItem(Player player, ItemStack item)
    {
        // If there are not free inventory slots
        if(player.getInventory().firstEmpty() == -1)
        {
            // Attempt to add items to the inventory, if it fails then return the items it could not place
            Map<Integer, ItemStack> couldNotStore = player.getInventory().addItem(item);

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
