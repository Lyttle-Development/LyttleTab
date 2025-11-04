package com.lyttledev.lyttletab.commands;

import com.lyttledev.lyttletab.LyttleTab;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public class LyttleTabCommand {
    private static LyttleTab plugin;

    public static void createCommand(LyttleTab lyttlePlugin, Commands commands) {
        plugin = lyttlePlugin;

        // Define the different nodes
        LiteralArgumentBuilder<CommandSourceStack> top = Commands.literal("lyttletab")
                .then(Commands.literal("reload")
                        .requires(source -> source.getSender().hasPermission("lyttletab.lyttletab.reload"))
                        .executes(LyttleTabCommand::reloadNode));

        // Defines root node functions
        top.requires(source -> source.getSender().hasPermission("lyttletab.lyttletab"));
        top.executes(LyttleTabCommand::rootNode);

        // Finish the command
        commands.register(
                top.build(),
                "Admin command for the LyttleTab plugin"
        );
    }

    private static int rootNode(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        Component version = Component.text("Plugin version: " + plugin.getDescription().getVersion());
        plugin.message.sendMessageRaw(sender, version);
        return Command.SINGLE_SUCCESS;
    }

    private static int reloadNode(CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        plugin.config.reload();
        plugin.message.sendMessageRaw(sender, Component.text("The config has been reloaded"));
        if ((boolean) plugin.config.tab.get("tab_enabled")) { plugin.tabHandler.reload(); }
        if ((boolean) plugin.config.bossbar.get("bossbar_enabled")) { plugin.bossbarHandler.refreshBossbar(); }
        return Command.SINGLE_SUCCESS;
    }
}
