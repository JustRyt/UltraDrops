package net.justryt.ultradrops.drops.configs;

import net.justryt.ultradrops.drops.loot.LootTable;
import org.jetbrains.annotations.NotNull;

public record DropConfig(@NotNull String displayName, @NotNull LootTable lootTable, int rows) {}