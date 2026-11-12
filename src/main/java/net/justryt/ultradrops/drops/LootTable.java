package net.justryt.ultradrops.drops;

import net.justryt.ultradrops.UltraDrops;
import net.justryt.ultradrops.drops.items.CustomCommandItem;
import net.justryt.ultradrops.drops.items.CustomItem;
import net.justryt.ultradrops.drops.items.RewardItem;
import net.justryt.ultradrops.drops.items.VanillaItem;
import net.justryt.ultradrops.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class LootTable {
    private final HashMap<RewardItem, Integer> rewards = new HashMap<>();

    public LootTable(@NotNull ConfigurationSection section) {
        var rewardKeys = section.getKeys(false);

        for (var rewardKey : rewardKeys) {
            var rewardSection = section.getConfigurationSection(rewardKey);
            assert rewardSection != null;

            RewardType rewardType;

            try {
                rewardType = RewardType.valueOf(rewardSection.getString("type"));
            } catch (Exception _) {
                UltraDrops.getPlugin().getLogger().warning("Invalid or no type set for reward with key: " + rewardKey);
                continue;
            }

            int weight = rewardSection.getInt("weight");

            if (weight == 0) {
                UltraDrops.getPlugin().getLogger().warning("Weight set to zero for reward with key: " + rewardKey);
            }

            switch (rewardType) {
                case VANILLA -> {
                    Material material;

                    try {
                        material = Material.valueOf(rewardSection.getString("material"));
                    } catch (Exception _) {
                        UltraDrops.getPlugin().getLogger().warning("Invalid or no material set for reward with key: " + rewardKey);
                        continue;
                    }

                    int origin;
                    int bound;

                    try {
                        var amount = rewardSection.getString("amount");

                        if (amount == null) throw new RuntimeException();

                        var split = amount.split("-");

                        if (split.length == 0) throw new RuntimeException();
                        else if (split.length == 1) {
                            origin = Integer.parseInt(split[0]);
                            bound = origin;
                        } else if (split.length == 2) {
                            origin = Integer.parseInt(split[0]);
                            bound = Integer.parseInt(split[1]);
                        } else throw new RuntimeException();
                    } catch (Exception _) {
                        UltraDrops.getPlugin().getLogger().warning("Invalid or no amount set for reward with key: " + rewardKey);
                        continue;
                    }

                    var item = new VanillaItem(material, origin, bound);
                    rewards.put(item, weight);
                }
                case CUSTOM -> {
                    var itemStack = rewardSection.getItemStack("stack");

                    if (itemStack == null) {
                        UltraDrops.getPlugin().getLogger().warning("Invalid or no itemstack set for custom reward with key: " + rewardKey);
                        continue;
                    }

                    var item = new CustomItem(itemStack);

                    rewards.put(item, weight);
                }
                case CUSTOM_COMMAND -> {
                    Component displayItemName;

                    try {
                        var serialized = rewardSection.getString("display-item.name");

                        if (serialized != null) {
                            displayItemName = Utils.mm(serialized);
                        } else throw new RuntimeException();
                    } catch (Exception _) {
                        UltraDrops.getPlugin().getLogger().warning("Invalid or no display item name set for reward with key: " + rewardKey);
                        continue;
                    }

                    var displayItemLore = new ArrayList<Component>();

                    rewardSection.getStringList("display-item.lore").forEach(line -> {
                        displayItemLore.add(Utils.mm(line));
                    });

                    String giveCommand;

                    try {
                        giveCommand = rewardSection.getString("give-command");

                        if (giveCommand == null) throw new RuntimeException();
                    } catch (Exception _) {
                        UltraDrops.getPlugin().getLogger().warning("Invalid or no give command set for reward with key: " + rewardKey);
                        continue;
                    }

                    Material material;

                    try {
                        material = Material.valueOf(rewardSection.getString("display-item.material"));
                    } catch (Exception _) {
                        UltraDrops.getPlugin().getLogger().warning("Invalid or no display item material set for reward with key: " + rewardKey);
                        continue;
                    }

                    boolean glint = rewardSection.getBoolean("display-item.glint", false);

                    var displayItem = new ItemStack(material);

                    displayItem.editMeta(meta -> {
                        meta.displayName(displayItemName);
                        meta.lore(displayItemLore);

                        if (glint) meta.setEnchantmentGlintOverride(true);
                    });

                    var item = new CustomCommandItem(displayItem, giveCommand);

                    rewards.put(item, weight);
                }
            }
        }
    }

    public ItemStack rollItem() {
        int totalWeight = 0;
        for (int weight : rewards.values()) {
            totalWeight += weight;
        }

        if (totalWeight <= 0) {
            return ItemStack.of(Material.AIR);
        }

        int roll = ThreadLocalRandom.current().nextInt(1, totalWeight + 1);

        for (var entry : rewards.entrySet()) {
            roll -= entry.getValue();
            if (roll <= 0) {
                return entry.getKey().getItem();
            }
        }

        return ItemStack.of(Material.AIR);
    }

    enum RewardType {
        VANILLA,
        CUSTOM,
        CUSTOM_COMMAND
    }
}
