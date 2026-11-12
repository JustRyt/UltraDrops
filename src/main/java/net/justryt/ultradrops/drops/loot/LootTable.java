package net.justryt.ultradrops.drops.loot;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.justryt.ultradrops.UltraDrops;
import net.justryt.ultradrops.drops.items.CustomCommandItem;
import net.justryt.ultradrops.drops.items.RewardItem;
import net.justryt.ultradrops.drops.items.VanillaItem;
import net.justryt.ultradrops.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class LootTable {
    private final HashMap<RewardItem, Integer> rewards = new HashMap<>();

    public LootTable(@NotNull ConfigurationSection section) {
        var rewardKeys = section.getKeys(false);
        var enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

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

            if (weight <= 0) {
                UltraDrops.getPlugin().getLogger().warning("Weight must be greater than zero for reward with key: " + rewardKey);
                continue;
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
                        var amount = rewardSection.getString("amount", "1");
                        var split = amount.split("-");

                        if (split.length == 1) {
                            origin = Integer.parseInt(split[0].trim());
                            bound = origin;
                        } else if (split.length == 2) {
                            origin = Integer.parseInt(split[0].trim());
                            bound = Integer.parseInt(split[1].trim());
                        } else {
                            throw new IllegalArgumentException();
                        }
                    } catch (Exception _) {
                        UltraDrops.getPlugin().getLogger().warning("Invalid amount set for reward with key: " + rewardKey);
                        continue;
                    }

                    var template = new ItemStack(material);

                    if (!material.isAir()) {
                        var rawName = rewardSection.getString("name");
                        var rawLore = rewardSection.getStringList("lore");
                        boolean glint = rewardSection.getBoolean("glint", false);
                        var enchantsSection = rewardSection.getConfigurationSection("enchants");

                        template.editMeta(meta -> {
                            if (rawName != null && !rawName.isBlank()) {
                                meta.displayName(Utils.mm(rawName));
                            }

                            if (!rawLore.isEmpty()) {
                                var loreComponents = new ArrayList<Component>(rawLore.size());
                                rawLore.forEach(line -> loreComponents.add(Utils.mm(line)));
                                meta.lore(loreComponents);
                            }

                            if (glint) {
                                meta.setEnchantmentGlintOverride(true);
                            }

                            if (enchantsSection != null) {
                                for (String enchantKey : enchantsSection.getKeys(false)) {
                                    int level = enchantsSection.getInt(enchantKey, 1);
                                    NamespacedKey key = NamespacedKey.fromString(enchantKey.toLowerCase(Locale.ROOT));
                                    Enchantment enchantment = (key != null) ? enchantmentRegistry.get(key) : null;

                                    if (enchantment == null) {
                                        UltraDrops.getPlugin().getLogger().warning(
                                                "Invalid enchantment '" + enchantKey + "' for reward with key: " + rewardKey
                                        );
                                        continue;
                                    }

                                    meta.addEnchant(enchantment, level, true);
                                }
                            }
                        });
                    }

                    var item = new VanillaItem(template, Math.min(origin, bound), Math.max(origin, bound));
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
        CUSTOM_COMMAND
    }
}