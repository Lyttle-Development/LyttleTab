package com.lyttledev.lyttletab.types;

import com.lyttledev.lyttletab.LyttleTab;
import com.lyttledev.lyttleutils.types.Config;

public class Configs {
    private final LyttleTab plugin;

    // Configs
    public Config general;
    public Config messages;
    public Config bossbar;
    public Config tab;

    // Default configs
    public Config defaultGeneral;
    public Config defaultMessages;
    public Config defaultBossbar;
    public Config defaultTab;

    public Configs(LyttleTab plugin) {
        this.plugin = plugin;

        // Configs
        general = new Config(plugin, "config.yml");
        messages = new Config(plugin, "messages.yml");
        bossbar = new Config(plugin, "bossbar.yml");
        tab = new Config(plugin, "tab.yml");

        // Default configs
        defaultGeneral = new Config(plugin, "#defaults/config.yml");
        defaultMessages = new Config(plugin, "#defaults/messages.yml");
        defaultBossbar = new Config(plugin, "#defaults/bossbar.yml");
        defaultTab = new Config(plugin, "#defaults/tab.yml");
    }

    public void reload() {
        general.reload();
        messages.reload();
        bossbar.reload();
        tab.reload();

        plugin.reloadConfig();
    }

    private String getConfigPath(String path) {
        return plugin.getConfig().getString("configs." + path);
    }
}
