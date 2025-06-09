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
        // If the index is out of bounds, default to last item in the list
        if (index < 0 || index >= messages.size()) {
            index = messages.size() - 1;
        }
        try {
            return plugin.message.getMessageRaw(messages.get(index), player);
        } catch (Exception e) {
            return plugin.message.getMessage("bossbar", player);
        }
    }

    public BossBar.Color getColor(List<String> colors, int index) {
        // If the index is out of bounds, default to last item in the list
        if (index < 0 || index >= colors.size()) {
            index = colors.size() - 1;
        }
        String color = colors.get(index).toUpperCase();
        switch (color.toUpperCase()) {
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

    public float getProgress(List<String> percentages, int index) {
        // If the index is out of bounds, default to last item in the list
        if (index < 0 || index >= percentages.size()) {
            index = percentages.size() - 1;
        }
        String fillPercentage = percentages.get(index);
        return Float.parseFloat(String.valueOf(fillPercentage)) / 100;
    }

    public BossBar.Overlay getOverlay(List<String> overlays, int index) {
        // If the index is out of bounds, default to last item in the list
        if (index < 0 || index >= overlays.size()) {
            index = overlays.size() - 1;
        }
        String color = overlays.get(index).toUpperCase();
        switch (color.toUpperCase()) {
            case "2":
            case "6":
            case "NOTCHED_6":
                return BossBar.Overlay.NOTCHED_6;
            case "3":
            case "10":
            case "NOTCHED_10":
                return BossBar.Overlay.NOTCHED_10;
            case "4":
            case "12":
            case "NOTCHED_12":
                return BossBar.Overlay.NOTCHED_12;
            case "5":
            case "20":
            case "NOTCHED_20":
                return BossBar.Overlay.NOTCHED_20;
        }
        return BossBar.Overlay.PROGRESS;
    }

    public void setBossbar(Player player) {
        List<String> colors = (List<String>) plugin.config.bossbar.get("bossbar_color");
        List<String> presentages = (List<String>) plugin.config.bossbar.get("bossbar_fill_percentage");
        List<String> messages = (List<String>) plugin.config.bossbar.get("bossbar_text");
        List<String> overlays = (List<String>) plugin.config.bossbar.get("bossbar_overlay");
        Double intervalDouble = (Double) plugin.config.bossbar.get("bossbar_interval");
        // Calculate the interval in seconds to ticks (1 second = 20 ticks) and allow commas
        int intervalInt = Math.toIntExact(Math.round(intervalDouble * 20));
        long interval = intervalInt > 0 ? intervalInt : 5 * 20L; // Default to 5*20 ticks if invalid

        if (colors.isEmpty() || presentages.isEmpty() || messages.isEmpty() || overlays.isEmpty()) {
            plugin.getLogger().warning("Bossbar configuration is incomplete. Please check your config.");
            return;
        }

        BossBar bossBar = BossBar.bossBar(
                getMessage(messages, 0, player),
                getProgress(presentages, 0),
                getColor(colors, 0),
                getOverlay(overlays, 0)
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
                    bossBar.color(getColor(colors, index[0]));
                    bossBar.progress(getProgress(presentages, index[0]));
                    bossBar.overlay(getOverlay(overlays, index[0]));
                } else {
                    this.cancel(); // Stop the task if the player is offline
                }
            }
        }.runTaskTimer(plugin, 0L, interval); // Run every x seconds (20 ticks)
    }
}
