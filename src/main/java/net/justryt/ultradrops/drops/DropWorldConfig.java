package net.justryt.ultradrops.drops;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public record DropWorldConfig(@NotNull World world, @NotNull Location center, int extent) {}
