package net.justryt.ultradrops.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.justryt.ultradrops.UltraDrops;
import net.justryt.ultradrops.utils.Utils;
import org.bukkit.entity.Player;

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

        root.then(Commands
                .literal("spawn")
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            UltraDrops.getDropsManager().getDropTypes().forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage(Utils.mm("<red>Only players can spawn a drop at their location.</red>"));
                                return Command.SINGLE_SUCCESS;
                            }

                            String type = StringArgumentType.getString(ctx, "type");
                            var configOpt = UltraDrops.getDropsManager().getDropConfig(type);

                            if (configOpt.isEmpty()) {
                                player.sendMessage(Utils.mm("<red>Unknown drop type: " + type + "</red>"));
                                return Command.SINGLE_SUCCESS;
                            }

                            var targetLoc = player.getLocation().getBlock().getLocation();
                            UltraDrops.getDropsManager().spawnDropAt(configOpt.get(), targetLoc, 0);
                            player.sendMessage(Utils.mm("<green>Spawned a " + configOpt.get().displayName() + " <green>drop at your location!</green>"));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );

        return root.build();
    }
}
