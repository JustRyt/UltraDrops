package net.justryt.ultradrops.drops.items;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record CustomItem(@NotNull ItemStack item) implements RewardItem {
    @Override
    public @NotNull ItemStack getItem() {
        return item.clone();
    }
}