# Ruined Portal Overhaul

Ruined Portal Overhaul turns overworld ruined portals into complete corrupted dungeon encounters. A portal is no longer just a small loot stop: it becomes a red-storm scar, an underground cave complex, a five-wave raid, a reward trader, a conduit progression path, and finally a Nether Dragon ritual.

## Features

- Procedural portal dungeons replace overworld ruined portal encounters with a radius-136 corrupted scar, three deterministic structure variants, a protected ritual core, underground pits, worm-carved tunnels, special rooms, lava features, and loot caches.
- Red storm atmosphere follows the portal zone with sky, fog, rain, thunder, particles, rumble, and underground-friendly horizontal-distance checks.
- Gold Tribute Raid starts by proximity only, with no Bad Omen and no gold armor requirement. Five waves escalate through custom GeckoLib Piglin Illager mobs, boss bars, inter-wave pacing, and persistent restart recovery.
- Raid completion lights the portal, disables pre-raid spawners, drops a boss reward chest, and summons the Exiled Piglin Trader.
- Reward progression includes Portal Shards, Nether Conduits, Ghast Tear Necklace fireballs, Corrupted Netherite armor, Nether Tide music disc, and Nether Dragon Scale trophy drops.
- Nether Crystal ritual uses four generated pedestals around completed portals to summon the Nether Dragon.
- Nether Dragon is anchored to the portal arena, suppresses End Crystal healing, avoids vanilla End portal death output, uses a phase-two slam, and drops Nether-themed rewards.
- Optional REI and Patchouli discovery content is included when those mods are installed.
- Operator-only `/rpo` admin commands help server owners and testers locate, inspect, reset, complete, wave-test, and dragon-test saved portal encounters.

## Compatibility

- Minecraft `1.21.11`
- Fabric Loader `0.18.6`
- Fabric API `0.141.3+1.21.11`
- Java `21`
- GeckoLib `5.4.5`
- Cloth Config and ModMenu are optional for configuration.
- REI and Patchouli are optional for discovery content.
- Terralith skylands and cave biome exclusions are included.
- Accessories is intentionally not required until a verified `1.21.11` build exists for this branch.

## Installation

1. Install Fabric Loader for Minecraft `1.21.11`.
2. Install Fabric API and GeckoLib.
3. Put the Ruined Portal Overhaul jar in your `mods` folder.
4. Add optional integrations if desired: Cloth Config, ModMenu, REI, and Patchouli.
5. Start a new world or explore new chunks so replaced ruined portals can generate.

## FAQ

### Does this use Bad Omen?

No. The raid is triggered by proximity to an uncompleted portal dungeon.

### Does the Nether Dragon behave like the End fight?

No. It uses vanilla dragon combat semantics where useful, but it is anchored to the portal arena, suppresses End Crystal healing, and does not create an End exit portal or dragon egg.

### Does this require Accessories or Trinkets?

No. The Ghast Tear Necklace is a native carried charm. Dragon Scale remains a trophy item until a compatible accessories slot dependency is verified for Minecraft `1.21.11`.

### Can server admins test encounters quickly?

Yes. Operators with gamemaster permission can use `/rpo locate`, `/rpo status`, `/rpo reset`, `/rpo wave <1-5>`, `/rpo complete`, and `/rpo dragon`.

### Are the non-English language files translated?

Not yet. They are complete English-valued stubs so translators have every key ready to edit.
