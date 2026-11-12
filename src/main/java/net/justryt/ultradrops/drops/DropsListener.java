package net.justryt.ultradrops.drops;

import net.justryt.ultradrops.UltraDrops;
import net.justryt.ultradrops.drops.items.CustomCommandItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class DropsListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof DropInventory)) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;
        if (!(event.getClickedInventory().getHolder() instanceof DropInventory)) return;

        var item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) return;

        if (item.getPersistentDataContainer().has(CustomCommandItem.commandKey)) {
            var command = item.getPersistentDataContainer().get(CustomCommandItem.commandKey, PersistentDataType.STRING);
            assert command != null;
            command = command.replace("[PLAYER_NAME]", event.getWhoClicked().getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
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

            assert dropUUID != null;
            var dropInv = UltraDrops.getDropsManager().getDropInventory(UUID.fromString(dropUUID));

            if (dropInv.isEmpty()) return;

            event.getPlayer().openInventory(dropInv.get().getInventory());
        }
    }
}
