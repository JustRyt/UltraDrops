package net.justryt.ultradrops.drops;

import net.justryt.ultradrops.UltraDrops;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public record Drop(
        @NotNull UltraDrops plugin,
        @NotNull UUID dropUUID,
        @NotNull Component dropDisplay,
        @NotNull Location dropLocation,
        @NotNull Map<Integer, Component> timedMessages,
        @NotNull Integer dropSpawnDelay,
        @NotNull AtomicBoolean completed
) {
    public static final NamespacedKey dropIdKey = new NamespacedKey("ultradrops", "drop");

    public Drop {
        timedMessages = new HashMap<>(timedMessages);
        dropLocation = dropLocation.clone();
        completed = new AtomicBoolean(false);
    }

    public void start() {
        var startTimestamp = System.currentTimeMillis();

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                plugin,
                task -> {
                    if (completed.get()) {
                        task.cancel();
                        return;
                    }

                    var timeToDrop = (int) ((startTimestamp + (dropSpawnDelay * 1000L)) - System.currentTimeMillis()) / 1000;

                    if (timeToDrop <= 0) {
                        dropLocation.getWorld().getChunkAtAsync(dropLocation).thenAccept(_ -> startDropAnimation());
                        task.cancel();
                        return;
                    }

                    var msg = timedMessages.remove(timeToDrop);
                    if (msg != null) {
                        Bukkit.broadcast(msg);
                    }
                },
                1, 10
        );
    }

    private void startDropAnimation() {
        dropLocation.setY(dropLocation.getWorld().getMaxHeight() - 1);

        Bukkit.getRegionScheduler().runAtFixedRate(plugin, dropLocation, task -> {
            if (completed.get()) {
                task.cancel();
                return;
            }

            var blockBelow = new Location(dropLocation.getWorld(), dropLocation.getBlockX(), dropLocation.getBlockY() - 1, dropLocation.getBlockZ()).getBlock();

            if (blockBelow.getY() < dropLocation.getWorld().getMinHeight() || blockBelow.isSolid()) {
                dropLocation.getBlock().setType(Material.CHEST);
                var chest = (Chest) dropLocation.getBlock().getState();

                chest.getPersistentDataContainer().set(dropIdKey, PersistentDataType.STRING, dropUUID.toString());
                chest.update(true);
                task.cancel();
                completed.set(true);
                return;
            }

            dropLocation.getBlock().setType(Material.AIR);
            dropLocation.setY(dropLocation.getY() - 1);
            blockBelow.setType(Material.CHEST);
        }, 1, Math.max(1, 20 / Math.max(1, plugin.getConfig().getInt("drop-animation.speed", 20))));
    }

    public void cleanup() {
        completed.set(true);
        dropLocation.getBlock().setType(Material.AIR);
    }
}
