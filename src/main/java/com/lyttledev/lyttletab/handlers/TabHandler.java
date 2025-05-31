package com.lyttledev.lyttletab.handlers;

import com.lyttledev.lyttletab.LyttleTab;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
        Component header = Component.text("\n\n                                  play.MapleMC.net                                  \nWebsite www.MapleMC.net\n\n");
        Component footer = Component.text("\n[Lyttletab] footer test\n\n\n");
        event.getPlayer().sendPlayerListHeader(header);
        event.getPlayer().sendPlayerListFooter(footer);
    }

}
