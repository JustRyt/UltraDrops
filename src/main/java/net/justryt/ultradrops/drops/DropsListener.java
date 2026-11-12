package net.justryt.ultradrops.drops;

import net.justryt.ultradrops.UltraDrops;
import net.justryt.ultradrops.drops.items.CustomCommandItem;
import net.justryt.ultradrops.drops.loot.DropInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class DropsListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof DropInventory dropInv)) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;
        if (!(event.getClickedInventory().getHolder() instanceof DropInventory)) return;

        synchronized (dropInv) {
            var item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            if (item.getPersistentDataContainer().has(CustomCommandItem.commandKey)) {
                var command = item.getPersistentDataContainer().get(CustomCommandItem.commandKey, PersistentDataType.STRING);
                if (command != null) {
                    var finalCommand = command
                            .replace("[PLAYER_NAME]", event.getWhoClicked().getName())
                            .replace("[PLAYER_UUID]", event.getWhoClicked().getUniqueId().toString());
                    Bukkit.getGlobalRegionScheduler().execute(UltraDrops.getPlugin(), () ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand)
                    );
                }
                event.getClickedInventory().setItem(event.getSlot(), null);
                return;
            }

            var leftover = event.getWhoClicked().getInventory().addItem(item.clone());

            if (leftover.isEmpty()) {
                event.getClickedInventory().setItem(event.getSlot(), null);
            } else {
                event.getClickedInventory().setItem(event.getSlot(), leftover.get(0));
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof DropInventory) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChestInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;

        var block = event.getClickedBlock();
        if (block == null) return;

        if (block.getState(false) instanceof Chest chest) {
            var pdc = chest.getPersistentDataContainer();

            if (!pdc.has(Drop.dropIdKey)) return;
            event.setCancelled(true);
            var dropUUID = pdc.get(Drop.dropIdKey, PersistentDataType.STRING);
            if (dropUUID == null) return;

            var dropInv = UltraDrops.getDropsManager().getDropInventory(UUID.fromString(dropUUID));

            if (dropInv.isEmpty()) {
                if (! "falling".equals(dropUUID)) {
                    block.setType(Material.AIR);
                }
                return;
            }

            event.getPlayer().openInventory(dropInv.get().getInventory());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getState(false) instanceof Chest chest
                && chest.getPersistentDataContainer().has(Drop.dropIdKey)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block ->
                block.getState(false) instanceof Chest chest
                        && chest.getPersistentDataContainer().has(Drop.dropIdKey));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block ->
                block.getState(false) instanceof Chest chest
                        && chest.getPersistentDataContainer().has(Drop.dropIdKey));
    }
}