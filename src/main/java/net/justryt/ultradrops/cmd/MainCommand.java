package net.justryt.ultradrops.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.justryt.ultradrops.UltraDrops;

public class MainCommand {
    public static LiteralCommandNode<CommandSourceStack> get() {
        var root = Commands
                .literal("ultradrops");

        root.then(Commands
                .literal("reload")
                .requires(src -> src.getSender().isOp())
                .executes(_ -> {
                    UltraDrops.getDropsManager().cleanup();
                    UltraDrops.getPlugin().reloadConfig();
                    UltraDrops.getDropsManager().load(UltraDrops.getPlugin().getConfig());
                    UltraDrops.getPlugin().getLogger().info("Plugin reloaded!");
                    return Command.SINGLE_SUCCESS;
                })
        );

        return root.build();
    }
}
