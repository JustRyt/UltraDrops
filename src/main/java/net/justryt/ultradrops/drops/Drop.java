package net.justryt.ultradrops.drops;

import net.justryt.ultradrops.UltraDrops;
import net.justryt.ultradrops.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Chest;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Drop {
    public static final NamespacedKey dropIdKey = new NamespacedKey("ultradrops", "drop");

    private final UltraDrops plugin;
    private final UUID dropUUID;
    private final Component dropDisplay;
    private final Location dropLocation;
    private final Map<Integer, Component> timedMessages;
    private final int dropSpawnDelay;
    private final Runnable onExpire;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicReference<TextDisplay> hologramRef = new AtomicReference<>();

    public Drop(
            @NotNull UltraDrops plugin,
            @NotNull UUID dropUUID,
            @NotNull Component dropDisplay,
            @NotNull Location dropLocation,
            @NotNull Map<Integer, Component> timedMessages,
            int dropSpawnDelay,
            @NotNull Runnable onExpire
    ) {
        this.plugin = plugin;
        this.dropUUID = dropUUID;
        this.dropDisplay = dropDisplay;
        this.dropLocation = dropLocation.clone();
        this.timedMessages = new HashMap<>(timedMessages);
        this.dropSpawnDelay = dropSpawnDelay;
        this.onExpire = onExpire;
    }

    public void start() {
        long targetTimeMillis = System.currentTimeMillis() + (dropSpawnDelay * 1000L);

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            if (cancelled.get()) {
                task.cancel();
                return;
            }

            long remainingMillis = targetTimeMillis - System.currentTimeMillis();
            int timeToDrop = (int) Math.ceil(remainingMillis / 1000.0);

            if (remainingMillis <= 0) {
                task.cancel();
                dropLocation.getWorld().getChunkAtAsync(dropLocation).thenAccept(_ -> {
                    if (!cancelled.get()) startDropAnimation();
                });
                return;
            }

            var iterator = timedMessages.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                if (entry.getKey() >= timeToDrop) {
                    Bukkit.broadcast(entry.getValue());
                    iterator.remove();
                }
            }
        }, 1L, 10L);
    }

    private void startDropAnimation() {
        dropLocation.setY(dropLocation.getWorld().getMaxHeight() - 1);
        long period = Math.max(1L, 20L / Math.max(1, plugin.getConfig().getInt("drop-animation.speed", 20)));

        Bukkit.getRegionScheduler().runAtFixedRate(plugin, dropLocation, task -> {
            if (cancelled.get()) {
                dropLocation.getBlock().setType(Material.AIR);
                task.cancel();
                return;
            }

            var blockBelow = dropLocation.clone().subtract(0, 1, 0).getBlock();

            if (blockBelow.getY() < dropLocation.getWorld().getMinHeight() || !blockBelow.isEmpty()) {
                dropLocation.getBlock().setType(Material.CHEST);
                if (dropLocation.getBlock().getState() instanceof Chest chest) {
                    chest.getPersistentDataContainer().set(dropIdKey, PersistentDataType.STRING, dropUUID.toString());
                    chest.update(true);
                }

                var holoLoc = dropLocation.toCenterLocation().add(0, 1.0, 0);
                var textDisplay = dropLocation.getWorld().spawn(holoLoc, TextDisplay.class, display -> {
                    display.text(dropDisplay);
                    display.setBillboard(Display.Billboard.CENTER);
                    display.setAlignment(TextDisplay.TextAlignment.CENTER);
                    display.setShadowed(true);
                    display.setPersistent(false);
                });
                hologramRef.set(textDisplay);

                dropLocation.getWorld().spawnParticle(
                        Particle.EXPLOSION,
                        dropLocation.toCenterLocation(),
                        3, 0.3, 0.3, 0.3, 0.05
                );

                String landfallRaw = plugin.getConfig().getString("landfall-message");
                if (landfallRaw != null && !landfallRaw.isBlank()) {
                    Bukkit.broadcast(Utils.mm(landfallRaw
                            .replace("[DROP_X]", String.valueOf(dropLocation.getBlockX()))
                            .replace("[DROP_Y]", String.valueOf(dropLocation.getBlockY()))
                            .replace("[DROP_Z]", String.valueOf(dropLocation.getBlockZ()))));
                }

                long expireTicks = plugin.getConfig().getInt("drop-expire", 7200) * 20L;
                Bukkit.getGlobalRegionScheduler().runDelayed(plugin, _ -> onExpire.run(), Math.max(1L, expireTicks));

                task.cancel();
                return;
            }

            dropLocation.getBlock().setType(Material.AIR);
            dropLocation.subtract(0, 1, 0);
            dropLocation.getBlock().setType(Material.CHEST);
            if (dropLocation.getBlock().getState() instanceof Chest fallingChest) {
                fallingChest.getPersistentDataContainer().set(dropIdKey, PersistentDataType.STRING, "falling");
                fallingChest.update(true);
            }

            var centerLoc = dropLocation.toCenterLocation();
            dropLocation.getWorld().spawnParticle(Particle.CLOUD, centerLoc, 6, 0.35, 0.35, 0.35, 0.02);
            dropLocation.getWorld().spawnParticle(Particle.FLAME, centerLoc, 4, 0.25, 0.25, 0.25, 0.01);
        }, 1L, period);
    }

    public void cleanup() {
        if (!cancelled.compareAndSet(false, true)) return;

        if (plugin.isEnabled()) {
            var holo = hologramRef.getAndSet(null);
            if (holo != null) {
                holo.getScheduler().run(plugin, _ -> holo.remove(), null);
            }

            Bukkit.getRegionScheduler().execute(plugin, dropLocation, () -> {
                if (dropLocation.getBlock().getType() == Material.CHEST) {
                    dropLocation.getBlock().setType(Material.AIR);
                }
            });
        }
    }
}