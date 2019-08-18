package com.ljack2k.JackBottles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ljack2k.JackBottles.Commands.CommandWithdrawXP;
import com.ljack2k.JackBottles.Listeners.EventBlockDispense;
import com.ljack2k.JackBottles.Listeners.EventExpBottle;
import com.ljack2k.JackBottles.Listeners.EventPlayerInteract;
import com.ljack2k.JackBottles.Listeners.EventProjectileLaunch;
import com.ljack2k.JackBottles.Utils.ConfigUtil;
import com.ljack2k.JackBottles.Utils.LangUtil;
import com.ljack2k.JackBottles.Utils.SetExpFix;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class JackBottles extends JavaPlugin {
    @Getter
    public static JackBottles instance;
    @Getter
    private File configFile = new File(getDataFolder(), "config.yml");
    @Getter
    private File messagesFile = new File(getDataFolder(), "messages.yml");
    @Getter
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Getter
    private Random random = new Random();

    @Getter
    public static boolean isReady = false;

    @Getter
    public static String chatPrefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "JB" + ChatColor.GRAY + "] " + ChatColor.RESET;
    @Getter
    static String baseCommand = "jackbottles";           // Must match with the plugin.yml command
    @Getter
    static String basePermissionNode = "jackbottles";
    @Getter
    static String pluginName = "";                  // Filled by Bukkit
    @Getter
    static String version = "";             // Filled by Bukkit

    @Getter
    static Material baseItem = Material.GLASS_BOTTLE;

    @Getter
    static Material storedItem = Material.EXPERIENCE_BOTTLE;

    public static JackBottles getPlugin() {
        return getPlugin(JackBottles.class);
    }

    public static FileConfiguration config() {
        return JackBottles.getPlugin().getConfig();
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        version = getDescription().getVersion();
        pluginName = getDescription().getName();

        initConfigAndLang();
        registerEvents();
        // Register commands
        getCommand("withdrawxp").setExecutor(new CommandWithdrawXP(this));

        getLogger().info("I'm done loading.");
    }


    public void registerEvents() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new EventPlayerInteract(this), this);
        pm.registerEvents(new EventExpBottle(this), this);
        pm.registerEvents(new EventProjectileLaunch(this), this);
        pm.registerEvents(new EventBlockDispense(this), this);
        JackBottles.debug("Events Registered");
    }

    private void initConfigAndLang() {
        if (!configFile.exists()) {
            LangUtil.saveConfig();
            reloadConfig();
        }
        if (!messagesFile.exists()) {
            LangUtil.saveMessages();
            LangUtil.reloadMessages();
        }

        ConfigUtil.migrate();

        try {
            getConfig();
        } catch (IllegalArgumentException e) {
            JackBottles.error(LangUtil.InternalMessage.INVALID_CONFIG + ": " + e.getMessage());
            try {
                new Yaml().load(FileUtils.readFileToString(getConfigFile(), Charset.forName("UTF-8")));
            } catch (IOException io) {
                JackBottles.error(io.getMessage());
            }
            return;
        }
        JackBottles.info("Loaded Config Version: " + getConfig().getString("ConfigVersion"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

        if (command.getName().equalsIgnoreCase(baseCommand)) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission(basePermissionNode + ".admin")) {
                        sendNoPermission(sender);
                        return false;
                    }
                    reloadConfig();
                    LangUtil.reloadMessages();
                    sendChatMessage(sender, ChatColor.YELLOW + "" + LangUtil.InternalMessage.CONFIG_RELOAD);
                    return true;
                } else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                    sendHelp(sender);
                    return true;
                } else if (args[0].equalsIgnoreCase("save")) {
                    if (!sender.hasPermission(basePermissionNode + ".admin")) {
                        sendNoPermission(sender);
                        return false;
                    }
                    sendChatMessage(sender, "" + LangUtil.InternalMessage.CONFIG_SAVED);
                    saveConfig();
                    return true;
                } else if (args[0].equalsIgnoreCase("debug")) {
                    if (!sender.hasPermission(basePermissionNode + ".admin")) {
                        sendNoPermission(sender);
                        return false;
                    }
                    setDebugLevelCommand(sender, args);
                    return true;
                } else if (args[0].equalsIgnoreCase("version")) {
                    sendVersionInfo(sender);
                    return true;
                }
            } else {
                sendHelp(sender);
                return false;
            }
        }
        return false;
    }

    public void sendVersionInfo(CommandSender sender) {
        sendChatHeader(sender, "" + LangUtil.Message.CHATHEADER_VERSION_INFORMATION);
        sendChatMessage(sender, ChatColor.YELLOW + "" + LangUtil.Message.CURRENT_VERSION + Bukkit.getPluginManager().getPlugin(pluginName).getDescription().getVersion());
        sendChatMessage(sender, ChatColor.YELLOW + "" + Bukkit.getPluginManager().getPlugin(pluginName).getDescription().getDescription());
    }

    public void setDebugLevelCommand(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("0")) {
                JackBottles.instance.getConfig().set("DebugLevel", 0);
                sendChatMessage(sender, ChatColor.WHITE + "Debug level set to " + ChatColor.GREEN + "0 (OFF)");
            } else if (args[1].equalsIgnoreCase("1")) {
                JackBottles.instance.getConfig().set("DebugLevel", 1);
                sendChatMessage(sender, ChatColor.WHITE + "Debug level set to " + ChatColor.YELLOW + "1 (BASIC)");
            } else if (args[1].equalsIgnoreCase("2")) {
                JackBottles.instance.getConfig().set("DebugLevel", 2);
                sendChatMessage(sender, ChatColor.WHITE + "Debug level set to " + ChatColor.RED + "2 (FULL)");
            } else {
                sendChatMessage(sender, ChatColor.RED + "Set to 0 (OFF), 1 (BASIC) or 2 (FULL). Example: /" + baseCommand + " debug 0");
            }
        } else {
            if (JackBottles.instance.getConfig().getInt("DebugLevel") == 0) {
                sendChatMessage(sender, ChatColor.WHITE + "Debug level is currently " + ChatColor.GREEN + "0 (OFF)" + ChatColor.WHITE + ". Use /" + baseCommand + " debug 1 to turn it on.");
            } else if (JackBottles.instance.getConfig().getInt("DebugLevel") == 1) {
                sendChatMessage(sender, ChatColor.WHITE + "Debug level is currently " + ChatColor.YELLOW + "1 (BASIC)" + ChatColor.WHITE + ". Use /" + baseCommand + " debug 0 to turn it off.");
            } else if (JackBottles.instance.getConfig().getInt("DebugLevel") == 2){
                sendChatMessage(sender, ChatColor.WHITE + "Debug level is currently " + ChatColor.RED + "2 (FULL)" + ChatColor.WHITE + ". Use /" + baseCommand + " debug 0 to turn it off.");
            } else {
                sendChatMessage(sender, "Well this is awkward");
            }
        }
    }

    public void sendNoPermission(CommandSender sender) {
        sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NO_COMMAND_PERMISSION);
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase(baseCommand)) {
            List<String> l = new ArrayList<>();

            if (args.length == 1 ) {
                l.add("?");
                l.add("help");
                l.add("empty");

                if (sender.hasPermission(basePermissionNode + ".admin")) {
                    l.add("reload");
                    l.add("save");
                    l.add("debug");
                    l.add("version");
                }
            } else {
                if (args[0].equalsIgnoreCase("debug")) {
                    l.add("0");
                    l.add("1");
                    l.add("2");
                }
            }
            return l;
        }
        return null;
    }

    public void sendHelp(CommandSender sender) {
        sendChatHeader(sender, "Command Help");
        sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "?" + ChatColor.RESET
                + ChatColor.WHITE + " " + LangUtil.Message.HELP_COMMAND);
        sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "help"
                + ChatColor.RESET + ChatColor.WHITE + " " + LangUtil.Message.HELP_COMMAND);

        if (sender.hasPermission(basePermissionNode + ".withdrawxp")) {
            sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/withdrawxp" + ChatColor.GREEN + "?"
                    + ChatColor.RESET + ChatColor.WHITE + " " + LangUtil.Message.HELP_WITHDRAWXP);
        }


        if (sender.hasPermission(basePermissionNode + ".admin")) {
            sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "reload"
                    + ChatColor.RESET + ChatColor.WHITE + " " + LangUtil.Message.HELP_RELOAD_COMMAND);
            sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "save"
                    + ChatColor.RESET + ChatColor.WHITE + " " + LangUtil.Message.HELP_SAVE_COMMAND);
            sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "debug"
                    + ChatColor.RESET + ChatColor.WHITE + " " + LangUtil.Message.HELP_DEBUG_COMMAND);
            sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "version"
                    + ChatColor.RESET + ChatColor.WHITE + " " + LangUtil.Message.HELP_VERSION_COMMAND);
        }
    }

    public void sendChatHeader(CommandSender sender, String description) {
        sender.sendMessage(chatPrefix + ChatColor.RESET + " - " + description);
    }

    public void sendChatMessage(CommandSender sender, String message) {
        sender.sendMessage(chatPrefix + ChatColor.RESET + message);
        if ((sender instanceof Player)) { // Console send this, don't send another debug line
            JackBottles.debug(ChatColor.RESET + message);
        }
    }

    public static void info(LangUtil.InternalMessage message) {
        info(message.toString());
    }
    public static void info(String message) {
        getPlugin().getLogger().info(message);
    }
    public static void warning(LangUtil.InternalMessage message) {
        warning(message.toString());
    }
    public static void warning(String message) {
        getPlugin().getLogger().warning(message);
    }
    public static void error(LangUtil.InternalMessage message) {
        error(message.toString());
    }
    public static void error(String message) {
        getPlugin().getLogger().severe(message);
    }

    public static void debug(String message) {
        // return if plugin is not in debug mode
        if (getPlugin().getConfig().getInt("DebugLevel") == 0) return;

        getPlugin().getLogger().info(ChatColor.YELLOW +"[DEBUG] " + message + (getPlugin().getConfig().getInt("DebugLevel") >= 2 ? "\n" + getStackTrace() : ""));
    }

    public static String getStackTrace() {
        List<String> stackTrace = new LinkedList<>();
        stackTrace.add("Stack trace @ debug call (THIS IS NOT AN ERROR)");
        Arrays.stream(ExceptionUtils.getStackTrace(new Throwable()).split("\n"))
                .filter(s -> s.toLowerCase().contains("JackBottles"))
                .filter(s -> !s.contains("DebugUtil.getStackTrace"))
                .forEach(stackTrace::add);
        return String.join("\n", stackTrace);
    }

}
