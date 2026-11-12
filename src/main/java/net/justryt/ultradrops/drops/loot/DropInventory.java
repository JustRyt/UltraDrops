package net.justryt.ultradrops.drops.loot;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class DropInventory implements InventoryHolder {
    @Getter private final Inventory inventory;

    public DropInventory(@NotNull Component name, @NotNull LootTable lootTable, int rows) {
        int clampedRows = Math.clamp(rows, 1, 6);
        inventory = Bukkit.createInventory(this, clampedRows * 9, name);

        for (int i = 0; i < rows * 9; i++) {
            inventory.setItem(i, lootTable.rollItem());
        }
    }
}
