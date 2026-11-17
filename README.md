# UltraDrops

**UltraDrops** is a lightweight, Folia-supported supply drop plugin for Paper `1.21.11+` servers. Bring excitement and PvP hotspots to your worlds with automated, sky-falling supply crates, customizable loot tiers, and full Adventure MiniMessage formatting.

---

## Features

* **Animated Sky Drops:** Crates descend from world build height with customizable falling speeds (`1` to `20` blocks/sec) and smoke/flame particle trails, landing with an explosion effect and a floating `TextDisplay` hologram.
* **Native Folia & Paper Support:** Built from the ground up using Paper's Region & Global Schedulers (`folia-supported: true`), ensuring zero main-thread lag.
* **Weighted Loot Tables & Tiers:** Create unlimited drop tiers (e.g., *Common*, *Mythical*) with configurable chest sizes (`1` to `6` rows) and weighted item probabilities.
* **Vanilla & Custom Command Rewards:**
  * **Vanilla Items:** Supports custom display names, lore, enchantment glint overrides, randomized stack amounts (`min-max`), and any registered enchantment.
  * **Custom Commands:** Display a custom clickable GUI item that executes console commands upon claim using `[PLAYER_NAME]` and `[PLAYER_UUID]` placeholders.
* **Grief & Dupe Protection:** Supply chests are protected from block breaking and explosions (TNT/Creepers), and inventory interactions are synchronized to prevent item duplication when multiple players loot at once.
* **Full MiniMessage Support:** Style every broadcast, hologram, item name, and lore line with gradients, hex colors, and formatting via Adventure MiniMessage.
* **Configurable Timers & Bounds:** Customize drop intervals, countdown warning broadcasts, chest expiration timers, and chunk-based spawn radii per world.

---

## Screenshots

### Supply Drop Descending
![Supply Drop Falling](https://cdn.modrinth.com/data/cached_images/7802882c6104dbf4dff0db7fb460f0b60be6f734.png)

### Loot GUI & Hologram
![Loot GUI and Custom Items](https://cdn.modrinth.com/data/cached_images/174331bfd5565817b58e20d97476537d13bf95ce_0.webp)

### Landfall & Spawn Broadcasts
![Landfall Broadcast](https://cdn.modrinth.com/data/cached_images/612dfa94fc13b68cf9156df721382bc6b1bf734b.png)
![Spawn Command Feedback](https://cdn.modrinth.com/data/cached_images/8bc835d7c14b6da803732a30ac4d5b35f3238db0.png)

---

## Commands & Permissions

| Command | Alias | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/ultradrops spawn <type>` | `/uds spawn <type>` | Immediately spawns a supply drop of the specified tier at your current location. | `uds.spawn` |
| `/ultradrops reload` | `/uds reload` | Cleans up active drops and reloads `config.yml`. | `uds.reload` |

---

## Installation

1. Download `UltraDrops.jar`.
2. Place the `.jar` file into your server's `/plugins/` directory.
3. Restart your server to generate the default `config.yml`.
4. Edit `/plugins/UltraDrops/config.yml` to customize your drop tiers, worlds, and loot tables, then run `/ultradrops reload`.

## Facing a problem?
Open an issue on [Github](https://github.com/JustRyt/UltraDrops/issues)
