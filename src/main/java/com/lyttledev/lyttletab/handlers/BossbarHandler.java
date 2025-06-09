package com.lyttledev.lyttletab.handlers;

import com.lyttledev.lyttletab.LyttleTab;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
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

            // Obtain the player's bossbar
            if (bossBars.containsKey(player.getUniqueId())) {
                UUID uuid = player.getUniqueId();
                BossBar bossBar = bossBars.get(uuid);

                // Remove bossbar
                player.hideBossBar(bossBar);
                bossBars.remove(uuid);
            }

            // Set new bossbar
            setBossbar(player);
        });
    }

    public Component getMessage(List<String> messages, int index, Player player) {
        try {
            return plugin.message.getMessageRaw(messages.get(index), player);
        } catch (Exception e) {
            return plugin.message.getMessage("bossbar", player);
        }
    }

    public BossBar.Color color() {

        String color = (String) plugin.config.general.get("bossbar_color");
        switch (color) {
            case "BLUE":
                return BossBar.Color.BLUE;
            case "RED":
                return BossBar.Color.RED;
            case "PINK":
                return BossBar.Color.PINK;
            case "GREEN":
                return BossBar.Color.GREEN;
            case "PURPLE":
                return BossBar.Color.PURPLE;
            case "WHITE":
                return BossBar.Color.WHITE;
            case "YELLOW":
                return BossBar.Color.YELLOW;
        }

        return BossBar.Color.WHITE;
    }

    public void setBossbar(Player player) {
        List<String> messages = (List<String>) plugin.config.messages.get("bossbar");

        int configNumber = (int) plugin.config.general.get("bossbar_fill_precentage");
        float progress = Float.parseFloat(String.valueOf(configNumber)) / 100;;

        BossBar bossBar = BossBar.bossBar(
                getMessage(messages, 0, player),
                progress,
                color(),
                BossBar.Overlay.PROGRESS
        );
        player.showBossBar(bossBar);
        bossBars.put(player.getUniqueId(), bossBar);

        final int[] index = {0};

        // Update the bossbar every x seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    index[0] = (index[0] + 1) % messages.size(); // Cycle through messages
                    bossBar.name(getMessage(messages, index[0], player));
                } else {
                    this.cancel(); // Stop the task if the player is offline
                }
            }
        }.runTaskTimer(plugin, 0L, (int) plugin.config.general.get("bossbar_interval") * 20L); // Run every x seconds (20 ticks)
    }
}
