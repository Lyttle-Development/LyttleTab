package com.lyttledev.lyttletab;

import com.lyttledev.lyttletab.commands.*;
import com.lyttledev.lyttletab.handlers.*;
import com.lyttledev.lyttletab.types.Configs;

import com.lyttledev.lyttleutils.utils.communication.Console;
import com.lyttledev.lyttleutils.utils.communication.Message;
import com.lyttledev.lyttleutils.utils.storage.GlobalConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class LyttleTab extends JavaPlugin {
    public Configs config;
    public Console console;
    public Message message;
    public GlobalConfig global;
    public TabHandler tabHandler;
    public BossbarHandler bossbarHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Setup config after creating the configs
        this.config = new Configs(this);
        this.global = new GlobalConfig(this);

        // Migrate config
        migrateConfig();

        // Plugin startup logic
        this.console = new Console(this);
        this.message = new Message(this, config.messages, global);

        // Commands
        new LyttleTabCommand(this);

        // Handlers
        if ((boolean) config.general.get("use_tab")) { this.tabHandler = new TabHandler(this); }
        if ((boolean) config.general.get("use_bossbar")) { this.bossbarHandler = new BossbarHandler(this); }
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
