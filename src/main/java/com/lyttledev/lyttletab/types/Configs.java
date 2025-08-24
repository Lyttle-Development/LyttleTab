package com.lyttledev.lyttletab.types;

import com.lyttledev.lyttletab.LyttleTab;
import com.lyttledev.lyttleutils.types.YamlConfig;

public class Configs {
    private final LyttleTab plugin;

    // Configs
    public YamlConfig general;
    public YamlConfig messages;
    public YamlConfig bossbar;
    public YamlConfig tab;

    // Default configs
    public YamlConfig defaultGeneral;
    public YamlConfig defaultMessages;
    public YamlConfig defaultBossbar;
    public YamlConfig defaultTab;

    public Configs(LyttleTab plugin) {
        this.plugin = plugin;

        // Configs
        general = new YamlConfig(plugin, "config.yml");
        messages = new YamlConfig(plugin, "messages.yml");
        bossbar = new YamlConfig(plugin, "bossbar.yml");
        tab = new YamlConfig(plugin, "tab.yml");

        // Default configs
        defaultGeneral = new YamlConfig(plugin, "#defaults/config.yml");
        defaultMessages = new YamlConfig(plugin, "#defaults/messages.yml");
        defaultBossbar = new YamlConfig(plugin, "#defaults/bossbar.yml");
        defaultTab = new YamlConfig(plugin, "#defaults/tab.yml");
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
