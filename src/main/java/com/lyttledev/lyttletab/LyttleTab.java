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
        if ((boolean) config.tab.get("tab_enabled")) { this.tabHandler = new TabHandler(this); }
        if ((boolean) config.bossbar.get("bossbar_enabled")) { this.bossbarHandler = new BossbarHandler(this); }
    }

    @Override
    public void saveDefaultConfig() {
        String configPath = "config.yml";
        if (!new File(getDataFolder(), configPath).exists())
            saveResource(configPath, false);

        String messagesPath = "messages.yml";
        if (!new File(getDataFolder(), messagesPath).exists())
            saveResource(messagesPath, false);

        String bossbarPath = "bossbar.yml";
        if (!new File(getDataFolder(), bossbarPath).exists())
            saveResource(bossbarPath, false);

        String tabPath = "tab.yml";
        if (!new File(getDataFolder(), tabPath).exists())
            saveResource(tabPath, false);

        // Defaults:
        String defaultPath = "#defaults/";
        String defaultGeneralPath =  defaultPath + configPath;
        saveResource(defaultGeneralPath, true);

        String defaultMessagesPath =  defaultPath + messagesPath;
        saveResource(defaultMessagesPath, true);

        String defaultBossbarPath =  defaultPath + bossbarPath;
        saveResource(defaultBossbarPath, true);

        String defaultTabPath =  defaultPath + tabPath;
        saveResource(defaultTabPath, true);
    }

    private void migrateConfig() {
        if (!config.general.contains("config_version")) {
            config.general.set("config_version", 0);
        }

        switch (config.general.get("config_version").toString()) {
            case "0":
                // Migrate config entries.
                config.tab.set("tab_list_refresh_interval", config.defaultTab.get("tab_list_refresh_interval"));

                // Update config version.
                config.general.set("config_version", 1);

                // Recheck if the config is fully migrated.
                migrateConfig();
                break;
            default:
                break;
        }
    }
}
