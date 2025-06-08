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

    public Component getMessage() {
        try {

            List<String> messages = (List<String>) plugin.config.messages.get("bossbar");

            // TODO LOOP OVER THE LIST

            return plugin.message.getMessageRaw(messages.get(0));
        } catch (Exception e) {
            return plugin.message.getMessage("bossbar");
        }
    }


    public void setBossbar(Player player) {
        BossBar bossBar = BossBar.bossBar(
                getMessage(),
                0,
                BossBar.Color.WHITE,
                BossBar.Overlay.PROGRESS
        );
        player.showBossBar(bossBar);
        bossBars.put(player.getUniqueId(), bossBar);
    }
}
