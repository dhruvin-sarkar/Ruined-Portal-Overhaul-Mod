# Ruined Portal Overhaul

Ruined Portal Overhaul replaces overworld ruined portal encounters with large corrupted portal dungeons: a radius-136 Nether scar, underground cave network, red storm atmosphere, five-wave Piglin Illager raid, post-raid trader, Nether Conduit rewards, Nether Crystal ritual, and an overworld Nether Dragon boss.

## Stack

- Minecraft `1.21.11`
- Fabric Loader `0.18.6`
- Fabric API `0.141.3+1.21.11`
- Java `21`
- GeckoLib `5.4.5`
- Cloth Config `21.11.153` and ModMenu `17.0.0` are optional quality-of-life integrations
- REI `21.11.814` and Patchouli are optional discovery integrations

## Features

- Vanilla overworld ruined portals are replaced with procedural portal dungeons.
- Portal scars now blend through scorched native biome blocks instead of ending at a hard netherrack edge.
- Portal zones use horizontal distance checks, so surface, pit, and cave gameplay all stay linked to the same scar.
- Red storm visuals, fog, rain, thunder, particles, and looping rumble activate near portal territory.
- The Gold Tribute Raid runs five escalating but deliberately smaller waves with custom GeckoLib Piglin Illager mobs.
- Raid completion lights the portal, disables pre-raid spawners, spawns a boss reward chest, and summons the Exiled Piglin Trader.
- Rewards include Portal Shards, Nether Conduit progression, Ghast Tear Necklace fireballs, Corrupted Netherite armor, Nether Dragon Scale trophies, and the Nether Tide music disc.
- Four Nether Crystals placed on generated pedestals summon the Nether Dragon, which is anchored to the portal, suppresses End Crystal healing, and avoids vanilla End portal death output.
- Operator-only `/rpo` admin commands help testers and server owners locate saved portals, inspect status, reset a portal, force waves, complete a raid scene, or start the dragon sequence.
- Localization-ready resource files are present for seven major locales as English stubs pending community translation.
- A dedicated creative tab and matching vanilla-tab entries expose all custom items and blocks in creative inventory.

## Screenshots To Capture

- [Screenshot: red storm forming around a corrupted ruined portal scar]
- [Screenshot: underground pit and corrupted cave network]
- [Screenshot: five-wave Piglin Illager raid in progress]
- [Screenshot: Nether Conduit activated in a nether-brick frame]
- [Screenshot: Nether Dragon circling the completed portal arena]

## Installation

1. Install Minecraft `1.21.11` with Fabric Loader `0.18.6` or newer for the same Minecraft line.
2. Install Fabric API `0.141.3+1.21.11`.
3. Use Java `21`.
4. Place the Ruined Portal Overhaul jar in the `mods` folder.
5. Optional: add GeckoLib `5.4.5`, Cloth Config, ModMenu, REI, and Patchouli as shown in the compatibility table.

## Compatibility

| Mod or Tool | Status | Notes |
|---|---|---|
| Fabric API | Required | Built and verified against `0.141.3+1.21.11`. |
| GeckoLib | Required | Entity and block animations use GeckoLib `5.4.5`. |
| Cloth Config + ModMenu | Optional | Opens the config screen when installed. |
| REI | Optional | Adds progression information pages. |
| Patchouli | Optional | Guide book data is included and injected only when Patchouli is present. |
| Terralith | Supported | Skylands and cave biomes are excluded from structure/scatter placement. |
| Biomes O' Plenty | Expected compatible | Uses normal overworld biome tagging paths. |
| Regions Unexplored | Expected compatible | Uses normal overworld biome tagging paths. |
| Accessories | Not used | Deferred until a verified `1.21.11` build exists for this branch and Lunar Client. |

## Config Overview

- Structure rarity and outer-zone scatter.
- Raid trigger radius, wave size multiplier, inter-wave delay, boss bar toggle.
- Red storm intensity and thunder cadence.
- Mob health, damage, ambient mob cap, and post-raid suppression.
- Nether Star drop rate and Nether Dragon enable toggle.

## Verification

Current static verification:

- `./gradlew.bat build` succeeds with Java 21.
- JSON resources parse successfully.
- Dedicated-server dry start reaches Fabric, GeckoLib, and mod initialization, then stops at `eula=false` without accepting the Minecraft EULA.
- Bounded client startup smoke reaches client mod initialization, resource reload, sound engine startup, texture atlas creation, recipe/advancement loading, biome modifications, and integrated-server startup without mod-relevant errors before timeout.

Recommended first gameplay smoke:

1. Start a fresh overworld and locate `minecraft:ruined_portal`.
2. Walk from the outer scar into the pit and verify storm activation, underground horizontal-distance behavior, structure blending, and pre-raid spawners.
3. Use `/rpo locate` and `/rpo status` once the portal is discovered to confirm saved-state tracking.
4. Complete the five-wave raid and confirm the staggered completion sequence, boss chest, Exiled Piglin, lit portal, and post-raid spawn suppression. Use `/rpo wave <1-5>` only to isolate late-wave testing after the natural path is checked.
5. Place four Nether Crystals on the generated pedestals, fight the Nether Dragon through phase two, and confirm rewards, advancements, pedestal cleanup, and no End portal blocks. Use `/rpo complete` and `/rpo dragon` to isolate the completion and dragon scenes in repeat passes.

## Known Limits

- Entity texture variants are generated release art with a reproducible contrast/shading post-process; particle sprites are generated release art, not hand-painted final art.
- Custom sound events ship online-sourced `.ogg` files edited from redistributable CC0 Kenney and OpenGameArt packs. Full source mapping lives in `assets/audio_sources/ONLINE_AUDIO_ATTRIBUTION.md`, and the sounds remain replaceable by resource packs.
- Nether Dragon Scale is intentionally a trophy item until a compatible Accessories build is verified for Minecraft `1.21.11` and Lunar Client.

## Issues

Report bugs and gameplay feedback at https://github.com/dhruvin-sarkar/Ruined-Portal-Overhaul-Mod/issues.

## Development Notes

- Use Mojang mappings, not Yarn names.
- Use `loot_table/` and `advancement/` resource paths.
- Keep common code free of direct `net.minecraft.client.*` imports.
- Keep `CLAUDE.md` and `SPEC.md` aligned with source changes.
