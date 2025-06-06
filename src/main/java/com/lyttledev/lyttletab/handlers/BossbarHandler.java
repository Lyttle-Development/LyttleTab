package com.lyttledev.lyttletab.handlers;

import com.lyttledev.lyttletab.LyttleTab;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class BossbarHandler implements Listener {
    public static LyttleTab plugin;

    HashMap<UUID, BossBar> bossBars = new HashMap<>();

    public BossbarHandler(LyttleTab plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        BossbarHandler.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        setBossbar(player);
    }

    public void refreshBossbar() {
        plugin.getServer().getOnlinePlayers().forEach(player -> {

            UUID uuid = player.getUniqueId();
            BossBar bossBar = bossBars.get(uuid);
            player.hideBossBar(bossBar);

            setBossbar(player);
        });
    }

    public void setBossbar(Player player) {
        Component name = Component.text("Lyttle Tab");
        BossBar bossBar = BossBar.bossBar(
                plugin.message.getMessage("bossbar", player),
                0,
                BossBar.Color.WHITE,
                BossBar.Overlay.PROGRESS
        );
        player.showBossBar(bossBar);
        bossBars.put(player.getUniqueId(), bossBar);
    }

}
