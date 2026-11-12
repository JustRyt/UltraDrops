package net.justryt.ultradrops.drops;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class DropInventory implements InventoryHolder {
    @Getter private final Inventory inventory;

    public DropInventory(@NotNull Component name, @NotNull LootTable lootTable, int rows) {
        inventory = Bukkit.createInventory(this, rows * 9, name);

        for (int i = 0; i < rows * 9; i++) {
            inventory.setItem(i, lootTable.rollItem());
        }
    }
}
