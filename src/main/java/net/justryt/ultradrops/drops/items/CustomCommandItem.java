package net.justryt.ultradrops.drops.items;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public record CustomCommandItem(@NotNull ItemStack displayItem, @NotNull String giveCommand) implements RewardItem {
    public static final NamespacedKey commandKey = new NamespacedKey("ultradrops", "reward_with_command");

    public CustomCommandItem {
        displayItem.editPersistentDataContainer(pdc -> {
            pdc.set(commandKey, PersistentDataType.STRING, giveCommand);
        });
    }

    @Override
    public ItemStack getItem() {
        return displayItem.clone();
    }
}