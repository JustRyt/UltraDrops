package net.justryt.ultradrops;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import net.justryt.ultradrops.cmd.MainCommand;
import net.justryt.ultradrops.drops.DropsListener;
import net.justryt.ultradrops.drops.DropsManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class UltraDrops extends JavaPlugin {
    @Getter private static UltraDrops plugin;
    @Getter private static DropsManager dropsManager;

    @Override
    public void onEnable() {
        plugin = this;

        saveDefaultConfig();

        int pluginId = 35283;
        Metrics metrics = new Metrics(this, pluginId);

        getServer().getPluginManager().registerEvents(new DropsListener(), this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, handler -> {
            handler.registrar().register(MainCommand.get(), "uds");
        });

        dropsManager = new DropsManager(this);
    }

    @Override
    public void onDisable() {
        dropsManager.cleanup();
    }
}
