package com.lyttledev.lyttletab.handlers;

import com.lyttledev.lyttletab.LyttleTab;
import com.lyttledev.lyttletab.types.Configs;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lyttledev.lyttleutils.utils.entity.Player.getDisplayName;

public class TabHandler implements Listener {
    public static LyttleTab plugin;

    private List<String> animatedHeaders;
    private List<String> animatedFooters;
    private int tabListRefreshInterval;
    private int tabListAnimationInterval;
    private AtomicInteger headerIndex = new AtomicInteger(0);
    private AtomicInteger footerIndex = new AtomicInteger(0);

    private int headerTaskId = -1;
    private int footerTaskId = -1;
    private int refreshTaskId = -1;

    public TabHandler(LyttleTab plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        TabHandler.plugin = plugin;
        loadConfig();
        startTasks();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        setTabList(player);
    }

    public void refreshTabList() {
        plugin.getServer().getOnlinePlayers().forEach(this::setTabList);
    }

    private void advanceHeader() {
        headerIndex.updateAndGet(i -> (i + 1) % animatedHeaders.size());
    }

    private void advanceFooter() {
        footerIndex.updateAndGet(i -> (i + 1) % animatedFooters.size());
    }

    private void setTabList(Player player) {
        setTabListHeader(player);
        setTabListFooter(player);

        Replacements replacements = new Replacements.Builder()
                .add("<NAME>", getDisplayName(player))
                .build();

        String playerName = (String) plugin.config.tab.get("tab_player_name");
        player.playerListName(plugin.message.getMessageRaw(playerName, replacements, player));
        player.setPlayerListOrder(getTabPriority(player));
    }

    private void setTabListHeader(Player player) {
        String header = animatedHeaders.get(headerIndex.get());
        player.sendPlayerListHeader(plugin.message.getMessageRaw(header, player));
    }

    private void setTabListFooter(Player player) {
        String footer = animatedFooters.get(footerIndex.get());
        player.sendPlayerListFooter(plugin.message.getMessageRaw(footer, player));
    }

    private int getTabPriority(Player player) {
        return player.getEffectivePermissions().stream()
                .filter(permission -> permission.getPermission().startsWith("lyttletab.tabpriority."))
                .map(permission -> {
                    try {
                        return Integer.parseInt(permission.getPermission().replace("lyttletab.tabpriority.", ""));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);
    }

    // --- RELOAD SUPPORT ---
    public void reload() {
        stopTasks();
        loadConfig();
        resetIndexes();
        startTasks();
        refreshTabList();
    }

    private void loadConfig() {
        Configs config = plugin.config;

        List<String> loadedHeaders = config.tab.getStringList("tab_list_header");
        List<String> loadedFooters = config.tab.getStringList("tab_list_footer");

        this.animatedHeaders = (loadedHeaders != null) ? new ArrayList<>(loadedHeaders) : new ArrayList<>();
        this.animatedFooters = (loadedFooters != null) ? new ArrayList<>(loadedFooters) : new ArrayList<>();

        this.tabListRefreshInterval = config.tab.getInt("tab_list_refresh_interval");
        this.tabListAnimationInterval = config.tab.getInt("tab_list_animation_interval");
    }

    private void startTasks() {
        headerTaskId = Bukkit.getScheduler().runTaskTimer(plugin, this::advanceHeader, 0L, tabListAnimationInterval * 20L).getTaskId();
        footerTaskId = Bukkit.getScheduler().runTaskTimer(plugin, this::advanceFooter, 0L, tabListAnimationInterval * 20L).getTaskId();
        refreshTaskId = Bukkit.getScheduler().runTaskTimer(plugin, this::refreshTabList, 0L, tabListRefreshInterval * 20L).getTaskId();
    }

    private void stopTasks() {
        if (headerTaskId != -1) Bukkit.getScheduler().cancelTask(headerTaskId);
        if (footerTaskId != -1) Bukkit.getScheduler().cancelTask(footerTaskId);
        if (refreshTaskId != -1) Bukkit.getScheduler().cancelTask(refreshTaskId);
        headerTaskId = -1;
        footerTaskId = -1;
        refreshTaskId = -1;
    }

    private void resetIndexes() {
        headerIndex.set(0);
        footerIndex.set(0);
    }
}