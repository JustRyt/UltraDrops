package net.justryt.ultradrops.utils;

import net.justryt.ultradrops.UltraDrops;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class Utils {
    public static void getRandomLocation(@NotNull World world, @NotNull Location center, int extent, @NotNull Consumer<Location> callback) {
        Bukkit.getGlobalRegionScheduler().execute(UltraDrops.getPlugin(), () -> {
            int centerChunkX = center.getBlockX() >> 4;
            int centerChunkZ = center.getBlockZ() >> 4;

            int minChunkX = centerChunkX - extent;
            int maxChunkX = centerChunkX + extent;

            int minChunkZ = centerChunkZ - extent;
            int maxChunkZ = centerChunkZ + extent;

            int minX = minChunkX * 16;
            int maxX = (maxChunkX * 16) + 15;
            int minZ = minChunkZ * 16;
            int maxZ = (maxChunkZ * 16) + 15;

            int randomX = ThreadLocalRandom.current().nextInt(minX, maxX+1);
            int randomZ = ThreadLocalRandom.current().nextInt(minZ, maxZ+1);

            callback.accept(new Location(world, randomX, world.getMaxHeight(), randomZ));
        });
    }

    public static Component mm(@NotNull String serialized) {
        return MiniMessage
                .miniMessage()
                .deserialize(serialized)
                .decoration(TextDecoration.ITALIC, false);
    }
}
