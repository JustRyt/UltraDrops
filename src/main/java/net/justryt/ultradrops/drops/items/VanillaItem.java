package net.justryt.ultradrops.drops.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public record VanillaItem(@NotNull Material material, int minAmount, int maxAmount) implements RewardItem {
    @Override
    public ItemStack getItem() {
        if (minAmount == maxAmount) {
            return new ItemStack(material, minAmount);
        } else {
            return new ItemStack(material, ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1));
        }
    }
}
