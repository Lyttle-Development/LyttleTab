package com.lyttledev.lyttletab.handlers;

import com.lyttledev.lyttletab.LyttleTab;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TabHandler implements Listener {
    public static LyttleTab plugin;

    public TabHandler(LyttleTab plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        TabHandler.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        setTabList(player);
    }

    public void refreshTabList() {
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            setTabList(player);
        });
    }

    public void setTabList(Player player) {
        player.sendPlayerListHeader(plugin.message.getMessage("tab_list_header", player));
        player.sendPlayerListFooter(plugin.message.getMessage("tab_list_footer", player));

        Replacements replacements = new Replacements.Builder()
                .add("<NAME>", com.lyttledev.lyttleutils.utils.entity.Player.getDisplayName(player))
                .build();

        player.playerListName(plugin.message.getMessage("tab_player_name", replacements, player));

    }
}
