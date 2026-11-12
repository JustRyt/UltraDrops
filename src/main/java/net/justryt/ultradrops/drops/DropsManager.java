package net.justryt.ultradrops.drops;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.justryt.ultradrops.UltraDrops;
import net.justryt.ultradrops.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DropsManager {
    private int dropInterval;
    private ScheduledTask dropsTask;

    private final HashMap<Integer, String> timedMessages = new HashMap<>();
    private final HashMap<String, DropConfig> dropConfigs = new HashMap<>();
    private final HashSet<DropWorldConfig> dropWorldConfigs = new HashSet<>();
    private final HashMap<UUID, DropInventory> ongoingDrops = new HashMap<>();
    private final HashSet<Runnable> dropCleanupTasks = new HashSet<>();

    public DropsManager(@NotNull UltraDrops plugin) {
        load(plugin.getConfig());
    }

    public void startDropTask() {
        dropsTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(UltraDrops.getPlugin(), task -> {
            dropWorldConfigs.forEach(worldCfg -> {
                if (dropConfigs.isEmpty()) return;

                var dropTypes = new ArrayList<>(dropConfigs.keySet());
                var randomIndex = ThreadLocalRandom.current().nextInt(dropTypes.size());
                var randomDropConfig = dropConfigs.get(dropTypes.get(randomIndex));

                startNewDrop(randomDropConfig, worldCfg);
            });
        }, 1L, dropInterval * 20L);
    }

    public void startNewDrop(@NotNull DropConfig dropConfig, @NotNull DropWorldConfig dropWorldConfig) {
        Utils.getRandomLocation(dropWorldConfig.world(), dropWorldConfig.center(), dropWorldConfig.extent(), location -> {
            var dropUUID = UUID.randomUUID();
            var timedMessagesComp = timedMessages.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> Utils.mm(
                                    entry.getValue()
                                            .replace("[DROP_DISPLAY]", dropConfig.displayName())
                                            .replace("[DROP_X]", String.valueOf(location.getBlockX()))
                                            .replace("[DROP_Z]", String.valueOf(location.getBlockZ()))
                            )
                    ));

            var drop = new Drop(
                    UltraDrops.getPlugin(),
                    dropUUID,
                    Utils.mm(dropConfig.displayName()),
                    location,
                    timedMessagesComp,
                    dropInterval,
                    new AtomicBoolean(false)
            );

            drop.start();
            ongoingDrops.put(dropUUID, new DropInventory(Utils.mm(dropConfig.displayName()), dropConfig.lootTable(), dropConfig.rows()));

            Runnable cleanup = drop::cleanup;

            dropCleanupTasks.add(cleanup);
            Bukkit.getGlobalRegionScheduler().runDelayed(UltraDrops.getPlugin(), _ -> {
                if (dropCleanupTasks.contains(cleanup)) cleanup.run();
            }, UltraDrops.getPlugin().getConfig().getInt("drop-expire", 7200) * 20L);
        });
    }

    public Optional<DropInventory> getDropInventory(@NotNull UUID dropUUID) {
        return Optional.ofNullable(ongoingDrops.get(dropUUID));
    }

    public void load(@NotNull FileConfiguration config) {
        dropInterval = config.getInt("drop-interval");

        var timedMessagesSection = config.getConfigurationSection("timed-messages");

        if (timedMessagesSection != null) {
            timedMessagesSection.getKeys(false).forEach(key -> {
                try {
                    var secs = Integer.parseInt(key);
                    timedMessages.put(secs, timedMessagesSection.getString(key));
                } catch (Exception _) {}
            });
        }

        var dropConfigsSection = config.getConfigurationSection("drops");

        if (dropConfigsSection != null) {
            dropConfigsSection.getKeys(false).forEach(key -> {
                var dropConfigSection = dropConfigsSection.getConfigurationSection(key);
                assert dropConfigSection != null;
                var lootTableSection = dropConfigSection.getConfigurationSection("loot-table");

                if (lootTableSection != null) {
                    var dropConfig = new DropConfig(dropConfigSection.getString("display-name", "DEFAULT NAME"), new LootTable(lootTableSection), dropConfigSection.getInt("rows", 3));
                    dropConfigs.put(key, dropConfig);
                }
            });
        }

        config.getMapList("drop-worlds").forEach(map -> {
            var worldName = map.get("world");

            if (!(worldName instanceof String worldNameStr)) return;

            var world = Bukkit.getWorld(worldNameStr);

            if (world == null) return;

            var centerX = 0;
            var centerZ = 0;

            if (map.get("center") instanceof Map<?, ?> center) {
                if (center.get("x") instanceof Number xNum) {
                    centerX = xNum.intValue();
                }
                if (center.get("z") instanceof Number zNum) {
                    centerZ = zNum.intValue();
                }
            }

            int extent = (map.get("extent") instanceof Number extNum) ? extNum.intValue() : 5;

            var dropWorldConfig = new DropWorldConfig(world, new Location(world, centerX, world.getMaxHeight(), centerZ), extent);
            dropWorldConfigs.add(dropWorldConfig);
        });

        startDropTask();
    }

    public void cleanup() {
        dropCleanupTasks.forEach(Runnable::run);
        dropCleanupTasks.clear();

        if (dropsTask != null) dropsTask.cancel();
        timedMessages.clear();
        dropConfigs.clear();
        dropWorldConfigs.clear();
        ongoingDrops.clear();
    }

    public Optional<DropConfig> getDropConfig(@NotNull String dropType) {
        return Optional.ofNullable(dropConfigs.get(dropType));
    }
}
