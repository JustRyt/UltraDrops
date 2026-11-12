package net.justryt.ultradrops.drops.items;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public record VanillaItem(@NotNull ItemStack template, int minAmount, int maxAmount) implements RewardItem {
    public VanillaItem {
        template = template.clone();
    }

    @Override
    public ItemStack getItem() {
        if (template.getType().isAir()) {
            return ItemStack.empty();
        }

        var item = template.clone();
        int amount = (minAmount == maxAmount)
                ? minAmount
                : ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1);
        item.setAmount(amount);
        return item;
    }
}