package com.lyttledev.lyttletab;

import com.lyttledev.lyttletab.commands.*;
import com.lyttledev.lyttletab.handlers.*;
import com.lyttledev.lyttletab.types.Configs;
import com.lyttledev.lyttletab.types.Invoice;

import com.lyttledev.lyttleutils.utils.communication.Console;
import com.lyttledev.lyttleutils.utils.communication.Message;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;

public final class LyttleTab extends JavaPlugin {
    public Configs config;
    public Console console;
    public Message message;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Setup config after creating the configs
        config = new Configs(this);
        // Migrate config
        migrateConfig();

        // Plugin startup logic
        this.console = new Console(this);
        this.message = new Message(this, config.messages);

        // Commands
        new LyttleTabCommand(this);
    }

    @Override
    public void saveDefaultConfig() {
        String configPath = "config.yml";
        if (!new File(getDataFolder(), configPath).exists())
            saveResource(configPath, false);

        String messagesPath = "messages.yml";
        if (!new File(getDataFolder(), messagesPath).exists())
            saveResource(messagesPath, false);

        // Defaults:
        String defaultPath = "#defaults/";
        String defaultGeneralPath =  defaultPath + configPath;
        saveResource(defaultGeneralPath, true);

        String defaultMessagesPath =  defaultPath + messagesPath;
        saveResource(defaultMessagesPath, true);
    }

    private void migrateConfig() {
        if (!config.general.contains("config_version")) {
            config.general.set("config_version", 0);
        }

        switch (config.general.get("config_version").toString()) {
//            case "0":
//                // Migrate config entries.
//
//                // Update config version.
//                config.general.set("config_version", 1);
//
//                // Recheck if the config is fully migrated.
//                migrateConfig();
//                break;
            default:
                break;
        }
    }
}
