package net.justryt.ultradrops.drops;

import org.jetbrains.annotations.NotNull;

public record DropConfig(@NotNull String displayName, @NotNull LootTable lootTable, int rows) {}