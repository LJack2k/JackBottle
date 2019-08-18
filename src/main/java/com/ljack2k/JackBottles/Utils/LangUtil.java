/*
#
#   This file has been inspired by the guys at DiscordSRV. They make a very awesome
#   Discord plugin for Spigot that I recommend to everybody. Since I'm a beginner
#   coder I learned a lot from their code. Copied a lot of the structure of DiscordSRV
#   because it is so damm good.
#
#   You should totaly check them out at https://www.spigotmc.org/resources/discordsrv.18494/
#   or their github https://github.com/Scarsz/DiscordSRV
#
*/

package com.ljack2k.JackBottles.Utils;

import com.ljack2k.JackBottles.JackBottles;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LangUtil {

    public enum Language {

        EN("English");

        @Getter final String code;
        @Getter final String name;

        Language(String name) {
            this.code = name().toLowerCase();
            this.name = name;
        }

    }

    /**
     * Fixed messages
     */
    @SuppressWarnings("serial")
    public enum InternalMessage {
		CANNOT_LOAD_CONFIG_WARNING(new HashMap<Language, String>() {{
            put(Language.EN, "Configuration file cannot be loaded.");
        }}), CANNOT_LOAD_LANG_FILE_WARNING(new HashMap<Language, String>() {{
            put(Language.EN, "Language file cannot be opened");
        }}), LANGUAGE_INITIALIZED(new HashMap<Language, String>() {{ 
        	put(Language.EN, "Language file loaded");
        }}), INVALID_CONFIG(new HashMap<Language, String>() {{ 
        	put(Language.EN, "Invalid Config files");
        }}), ERROR_DATABASE_SETUP(new HashMap<Language, String>() {{
        	put(Language.EN, "Error setting up database!");
        }}), CONFIG_RELOAD(new HashMap<Language, String>() {{ 
        	put(Language.EN, "JackBottles Config Reloaded.");
        }}), CONFIG_SAVED(new HashMap<Language, String>() {{ 
        	put(Language.EN, "Config file saved");
        }});
    	
    	
    	
    	
    	
        @Getter private final Map<Language, String> definitions;
        InternalMessage(Map<Language, String> definitions) {
            this.definitions = definitions;

            // warn about if a definition is missing any translations for messages
            for (Language language : Language.values())
                if (!definitions.containsKey(language))
                    JackBottles.debug("Language " + language.getName() + " missing from definitions for " + name());
        }

        @Override
        public String toString() {
            return definitions.getOrDefault(userLanguage, definitions.get(Language.EN));
        }

    }

    public enum Message {

    	NOT_INGAME_PLAYER("NotIngamePlayer", true),
    	NO_COMMAND_PERMISSION("NoCommandPermission", true),
    	HELP_COMMAND("HelpCommand", true),
    	HELP_EMPTY_COMMAND("HelpEmptyCommand", true),
    	HELP_RELOAD_COMMAND("HelpReloadCommand", true),
    	HELP_SAVE_COMMAND("HelpSaveCommand", true),
    	HELP_DEBUG_COMMAND("HelpDebugCommand", true),
    	HELP_VERSION_COMMAND("HelpVersionCommand", true),
    	CURRENT_VERSION("CurrentVersion", true),
    	CHATHEADER_VERSION_INFORMATION("ChatHeaderVersionInformation", true),
        SOMETHING_WRONG("SomethingWrong", true),
        XP_BOTTLE_NAME("ExperienceBottleItemName", true),
        XP_BOTTLE_RIGHT_CLICK_USE("ExperienceBottleUse", true),
    	STORED_XP("StoredXP", true),
        NOT_ENOUGH_XP_TO_STORE("NotEnoughXP", true),
        EXPERIENCE_AMOUNT_ERROR("ExperienceAmountError", true),
        HELP_WITHDRAWXP_HEADER("HelpWithdrawXPHeader", true),
        HELP_WITHDRAWXP("HelpWithDrawXP", true),
        YOUR_INVENTORY_FULL("YourInventoryFull", true),
        YOU_CURRENT_XP_AMOUNT("YourCurrentXPAmount", true),
        BASE_ITEM_NEEDED("BaseItemNeeded", true),


        PLACEHOLDER("Nothing", false);

        @Getter private final String keyName;
        @Getter private final boolean translateColors;

        Message(String keyName, boolean translateColors) {
            this.keyName = keyName;
            this.translateColors = translateColors;
        }

        @Override
        public String toString() {
            String message = messages.getOrDefault(this, "");
            return message;
        }

    }

    @Getter private static final Map<Message, String> messages = new HashMap<>();
    @Getter private static final Yaml yaml = new Yaml();
    @Getter private static Language userLanguage;
    static {
        String languageCode = System.getProperty("user.language").toUpperCase();

        String forcedLanguage = JackBottles.config().getString("ForcedLanguage");
        if (StringUtils.isNotBlank(forcedLanguage) && !forcedLanguage.equalsIgnoreCase("none")) {
            if (forcedLanguage.length() == 2) {
                languageCode = forcedLanguage.toUpperCase();
            } else {
                for (Language language : Language.values()) {
                    if (language.getName().equalsIgnoreCase(forcedLanguage)) {
                        languageCode = language.getCode();
                    }
                }
            }
        }

        try {
            userLanguage = Language.valueOf(languageCode);
        } catch (Exception e) {
            userLanguage = Language.EN;

            JackBottles.info("Unknown user language " + languageCode.toUpperCase() + ".");
            //JackBottles.info("If you fluently speak " + languageCode.toUpperCase() + " as well as English, see the GitHub repo to translate it!");
        }

        saveConfig();
        saveMessages();
        reloadMessages();

        JackBottles.info(InternalMessage.LANGUAGE_INITIALIZED + userLanguage.getName());
    }

    private static void saveResource(String resource, File destination, boolean overwrite) {
        if (destination.exists() && !overwrite) return;

        try {
            FileUtils.copyInputStreamToFile(JackBottles.class.getResourceAsStream(resource), destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        saveConfig(false);
    }
    public static void saveConfig(boolean overwrite) {
        File destination = JackBottles.getPlugin().getConfigFile();
        String resource = "/config/" + userLanguage.getCode() + ".yml";

        saveResource(resource, destination, overwrite);
    }

    public static void saveMessages() {
        saveMessages(false);
    }
    public static void saveMessages(boolean overwrite) {
        String resource = "/messages/" + userLanguage.getCode() + ".yml";
        File destination = JackBottles.getPlugin().getMessagesFile();

        saveResource(resource, destination, overwrite);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void reloadMessages() {
        if (!JackBottles.getPlugin().getMessagesFile().exists()) return;

        try {
            for (Map.Entry entry : (Set<Map.Entry>) yaml.loadAs(FileUtils.readFileToString(JackBottles.getPlugin().getMessagesFile(), Charset.forName("UTF-8")), Map.class).entrySet()) {
                for (Message message : Message.values()) {
                    if (message.getKeyName().equalsIgnoreCase((String) entry.getKey())) {
                        messages.put(message, (String) entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            JackBottles.error("Failed loading " + JackBottles.getPlugin().getMessagesFile().getPath() + ": " + e.getMessage());

            File movedToFile = new File(JackBottles.getPlugin().getMessagesFile().getParent(), "messages-" + JackBottles.getPlugin().getRandom().nextInt(100) + ".yml");
            try { FileUtils.moveFile(JackBottles.getPlugin().getMessagesFile(), movedToFile); } catch (IOException ignored) {}
            saveMessages();
            JackBottles.error("A new messages.yml has been created and the erroneous one has been moved to " + movedToFile.getPath());
            reloadMessages();
        }
    }

}