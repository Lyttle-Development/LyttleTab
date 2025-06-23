package com.lyttledev.lyttletab.handlers;

import com.lyttledev.lyttletab.LyttleTab;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.lyttledev.lyttleutils.utils.entity.Player.getDisplayName;

public class TabHandler implements Listener {
    public static LyttleTab plugin;

    public TabHandler(LyttleTab plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        TabHandler.plugin = plugin;

        // Refresh tab list for all players every x seconds
        int refreshInterval = (int) plugin.config.tab.get("tab_list_refresh_interval");
        Bukkit.getScheduler().runTaskTimer(plugin, this::refreshTabList, 0L, refreshInterval * 20L); // Convert seconds to ticks
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        setTabList(player);
    }

    public void refreshTabList() {
        plugin.getServer().getOnlinePlayers().forEach(this::setTabList);
    }

    public void setTabList(Player player) {
        String header = (String) plugin.config.tab.get("tab_list_header");
        String footer = (String) plugin.config.tab.get("tab_list_footer");
        player.sendPlayerListHeader(plugin.message.getMessageRaw(header, player));
        player.sendPlayerListFooter(plugin.message.getMessageRaw(footer, player));

        Replacements replacements = new Replacements.Builder()
                .add("<NAME>", getDisplayName(player))
                .build();

        String playerName = (String) plugin.config.tab.get("tab_player_name");
        player.playerListName(plugin.message.getMessageRaw(playerName, replacements, player));

    }
}
