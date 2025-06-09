package com.lyttledev.lyttletab.commands;

import com.lyttledev.lyttletab.LyttleTab;
import com.lyttledev.lyttletab.handlers.BossbarHandler;
import com.lyttledev.lyttletab.handlers.TabHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class LyttleTabCommand implements CommandExecutor, TabCompleter {
    private final LyttleTab plugin;

    public LyttleTabCommand(LyttleTab plugin) {
        plugin.getCommand("lyttletab").setExecutor(this);
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Check for permission
        if (!(sender.hasPermission("lyttletab.lyttletab"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Plugin version: " + plugin.getDescription().getVersion());
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.config.reload();
                plugin.message.sendMessageRaw(sender, Component.text("The config has been reloaded"));
                if ((boolean) plugin.config.tab.get("tab_enabled")) { plugin.tabHandler.refreshTabList(); }
                if ((boolean) plugin.config.bossbar.get("bossbar_enabled")) { plugin.bossbarHandler.refreshBossbar(); }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }

        return List.of();
    }
}
