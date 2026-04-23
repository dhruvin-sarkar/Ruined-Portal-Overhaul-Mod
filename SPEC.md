# Ruined Portal Overhaul - Implementation Spec v1.10

## Source Of Truth

`CLAUDE.md` is the canonical project reference. This spec is the concise implementation-facing companion and must stay aligned with it.

## Mod Description

Ruined Portal Overhaul transforms every overworld ruined portal encounter into a Massive Portal Dungeon: a radius-136 corrupted scar with calm Nether-like surface undulation, grouped basalt formations, a protected pit hub, worm-carved corrupted caves, frequent underground caches, lava runs, red storm atmosphere, a territory aether boon, advancement progression, tiered loot, and a five-wave raid encounter.

Explorers who enter the approach zone feel the portal stir, receive the red-aether protective boon while inside the territory, and get one starter Totem of Undying per portal. Standing near the frame awakens five escalating waves of Piglin Illager variants with crossbows, axes, ravagers, evocation magic, boss-bar progress, inter-wave warnings, ring-based spawns, red thunder, server-side corruption atmosphere, and Hard-mode upgrades.

Completing the raid lights the ruined frame into a functional Nether portal, disables pre-raid spawners, spawns a boss reward chest, and summons the Exiled Piglin Trader for a short-lived gold-based reward shop. Completion is tracked per portal location so each dungeon is a one-time survival challenge rather than a repeatable farm.

## Implemented Systems

1. Procedural Portal Dungeon
   - Custom structure replaces vanilla overworld ruined portal pieces with a corrupted surface scar and underground dungeon space.
   - `PortalDungeonPiece` builds roughly radius 136 and depth 45 through `PortalStructureHelper`.
   - Three deterministic structure variants are implemented: `Crimson Throne`, `Sunken Sanctum`, and `Basalt Citadel`.
   - `Sunken Sanctum` lowers the ritual core into a bowl with heavier soul-sand corruption and a collapsed north-rim blackstone arch.
   - `Basalt Citadel` uses a blackstone inner zone, a widened ritual platform, basalt corner columns, and a pit lava moat.
   - The generated portal frame is a valid 4-by-5 or 6-by-7 outer frame.
   - Inner ritual terrain and pit placement are protected; surface height variation is applied only outside the stable core.
   - Surface terrain uses low-frequency deterministic noise clamped to about `-3` to `+3` blocks, so the scar reads as a mostly flat Nether plain with mild organic unevenness.
   - Basalt columns remain separate vertical drama placed on top of the calm terrain.
2. Pit And Underground
   - The pit uses the original-style ragged mouth and shaft behavior, with mixed blackstone/basalt/netherrack/soul-soil rim rubble and 12 lower lava seeps.
   - The cave system branches from the pit and primary chamber.
   - Cave tunnels are worm-carved: gradual direction blending, radius variation from independent smooth noise, vertical drift, side pockets, and finite path lengths.
   - The cave graph is denser than the first worm pass, with more nodes, more side branches, stronger cross-linking, and many cave-node treasure caches.
   - Deep caves bias toward blackstone/basalt, more lava runs, ceiling lava drips, larger rooms, and ghast-ready volume.
3. Runtime Portal-Zone Spawns
   - `GoldRaidManager` owns structure-local ambient spawns; global biome modifications remain only low-density lore hints.
   - Incomplete, inactive portal zones attempt natural-style spawns by distance/depth band.
   - Anchored ghasts are tagged to portal origins and cleaned up if they drift beyond the anchor radius or timeout.
- Runtime origin, heightmap, and spawn-volume scans skip unloaded chunks so portal effects do not create new chunk work during save/quit.
- Wave spawn and fallback portal-frame scans also skip unloaded chunks before terrain or block-state reads.
4. Pre-Raid Spawners
   - Structure generation places deterministic surface, chamber, tunnel, and deep-cave spawners.
   - `GoldRaidManager` scans/persists nearby spawner positions and deletes them when the raid starts.
   - Spawners are not re-enabled after raid start or completion.
5. Piglin Illager Mob Family
   - Registered combat entities: Piglin Pillager, Piglin Vindicator, Piglin Brute Pillager, Piglin Illusioner, Piglin Evoker, Piglin Ravager, and Piglin Vex.
   - The Exiled Piglin is a post-raid Wandering Trader-based reward entity.
   - Combat mobs read live config multipliers at spawn time and compose them with difficulty scaling: Easy/Peaceful 0.75x health and damage, Normal 1.0x, Hard 1.25x health and 1.5x damage.
   - Piglin Illusioner no longer applies Blindness; the red atmosphere is the visual pressure system.
6. Proximity Raid
   - The manager class remains `GoldRaidManager` for compatibility, but the trigger is not gold armor and no Bad Omen is involved.
   - Approach triggers horizontally within 136 blocks of an uncompleted generated portal.
   - Raid starts horizontally within the configured trigger radius of an uncompleted, inactive generated portal. The built-in default is 24 blocks and is clamped to 12-48.
   - Horizontal X/Z checks are used for zone membership, so the storm and progression work inside the pit and caves below the frame.
7. Red Storm And Audio
   - Server sends `PortalAtmospherePayload` every 10 ticks while a player is horizontally inside the portal zone.
   - Client overlay is about 15-20% stronger than the previous storm pass.
   - Red thunder is roughly twice as frequent and uses a brief 2-3 tick deep-red HUD flash instead of the vanilla white sky flash.
   - Sky, rain, weather state, and fog are handled through client mixins; `ClientLevelStormMixin` only affects real `ClientLevel` instances so fake storm weather does not leak into integrated-server logic, and fog is tighter underground through descent-scaled intensity.
8. Territory Boon
   - While a portal is incomplete, players horizontally inside its radius-136 territory receive Regeneration II, Resistance I, Fire Resistance I, and Absorption IV every 10 ticks with a 260-tick duration.
   - The boon uses the same horizontal zone logic as the atmosphere, so it works on the surface, in the pit, and throughout the caves.
   - Each player receives one Totem of Undying per portal territory on first entry, guarded by the entity tag `rpo_totem_granted_<portalOriginLong>`.
9. Loot
   - `portal_surface`: 9-12 rolls with six surface prep chests, including stronger potion frequency, golden apples/carrots, emergency totems, enchanted gold gear, diamonds, and rare scrap.
   - `portal_deep`: 12-16 rolls with frequent underground caches, netherite scraps, ancient debris, templates, rare netherite ingots, diamond gear, totems, enchanted apples, Mending, Efficiency V, Sharpness V, Protection IV, Thorns III, and survival potions.
   - `portal_boss_reward`: 16-20 rolls plus a two-roll high-weight totem/enchanted-golden-apple bonus pool and guaranteed `Shard of the Nether`; includes 2-5 netherite ingots and the weighted `Corrupted Portal Key`.
   - Custom combat mob drops are richer across the board, with more gold, food, ammunition, equipment, books, totems from high-threat mobs, and rare netherite/debris drops from deeper wave threats.
10. Advancements
   - Advancements live under `data/ruined_portal_overhaul/advancement/`.
   - Custom criteria live in `com.ruinedportaloverhaul.advancement`.
   - `PortalEventTrigger` and `ModAdvancementTriggers` register and fire portal approach, pit descent, deep storm, aether boon, territory totem, raid start, raid completion, and Exiled Piglin trade events.
11. Nether Conduit
   - Custom block and block entity activate from a nether-bricks frame, grant Nether survival effects, reduce lava movement at higher level, allow Nether sleep near an active conduit, attack nearby Nether mobs, and clear runtime lava-boost state on server stop.
   - Exactly one conduit is inserted directly into a generated structure chest, with additional rare drops from custom raid mobs.
12. Ghast Tear Necklace
   - Native carried charm item with no external accessory dependency.
   - While carried, it grants Speed II and Jump Boost II; the client keybind sends a server packet that spawns a cooldown-gated Small Fireball.
13. Nether Crystal Ritual And Dragon
   - Nether Crystals place on netherite blocks or obsidian.
   - Four generated netherite pedestals around a completed portal track crystal placement in persistent raid state.
   - Ritual state is reconciled from the loaded pedestal crystals; broken crystals clear saved progress, interrupted summoning only resumes when all four crystals are still present, and the persistent dragon-active flag means a live dragon rather than a queued summon.
   - Completing the ritual summons the Nether Dragon, which suppresses End-only behavior, drops Nether rewards, and shatters the pedestals on death.

## Advancement Tree

| Advancement | Trigger |
|---|---|
| `Something Stirs` | Custom `portal_approach` |
| `The Air Cheats` | Custom `aether_boon` |
| `Not Yours Anymore` | Custom `territory_totem` |
| `Take Only What Screams` | `inventory_changed` for surface-useful loot |
| `Down the Red Throat` | Custom `pit_descent` |
| `The Weather Is Down Here` | Hidden custom `deep_storm` branch for staying in-zone at least 24 blocks below the frame |
| `Cache Problem` | `inventory_changed` for netherite upgrade template or netherite scrap |
| `No Tribute Required` | Custom `raid_started` |
| `Hostile Acquisition` | `player_killed_entity` for Piglin Illager variants |
| `Large Print` | `player_killed_entity` for Piglin Ravager |
| `Contract Voided` | `player_killed_entity` for Piglin Evoker |
| `The Good Chest Was Lower` | `inventory_changed` for ancient debris or totem |
| `The Last Wave Broke` | Challenge custom `raid_completed` |
| `No Clan, Still Capitalism` | Custom `exiled_trade` from `notifyTrade(...)` |
| `Paid in Scar Tissue` | `inventory_changed` for netherite ingot |
| `Nether Bond` | `inventory_changed` for the Nether Conduit |
| `Fully Awakened` | Challenge custom `nether_conduit_level_2` |
| `Draped in Sorrow` | Custom `ghast_tear_necklace_equipped` |
| `Ghost Fire` | Custom `nether_fireball_used` |
| `The Final Offering` | Challenge custom `nether_crystal_ritual_complete` |
| `Here Be Dragons` | Challenge custom `nether_dragon_defeated` |

## Raid Lifecycle

1. Approach zone marks the portal as awakened and grants the root advancement.
2. Raid trigger starts wave 1, grants `No Tribute Required`, deletes pre-raid spawners, and shows the raid title.
3. Active mobs are tracked by UUID and persisted through `PortalRaidState`.
4. Runtime portal discovery also persists the discovered structure variant through `PortalRaidState` without mutating save data during chunk generation.
5. Active raids rehydrate after server restart and pause mob-death evaluation while the portal area is not entity-ticking.
6. Wave completion advances through five boss-bar waves.
7. Final completion hides and clears the boss bar, lights the portal, spawns the boss chest, summons the Exiled Piglin, marks the portal complete, disables any remaining spawner blocks, then plays completion effects and grants nearby players the raid-complete trigger.

## Compatibility

- Minecraft `1.21.11`
- Fabric Loader `0.18.6`
- Fabric API `0.141.3+1.21.11`
- Java `21`
- Mojang mappings
- Resource paths use modern singular names such as `loot_table/` and `advancement/`.
- Accessories is intentionally not required for the Lunar-compatible build. Re-verified Wisp Maven metadata still has no `1.21.11` Accessories build as of 2026-04-22, and the available `1.21.10` jar crashes Lunar Client `1.21.11` inventory screen mixins, so the Ghast Tear Necklace is implemented as a native carried item.

## Validation

- `./gradlew.bat build` succeeds with Java 21 when `JAVA_HOME` points at `C:\Users\dhruv\.codex\jdks\temurin-21`.
- JSON data files in resources parse successfully.

## Remaining Work

1. Run a full in-game `runClient` survival smoke test with log review.
2. Use `/locate structure minecraft:ruined_portal` across multiple seeds to confirm all three structure variants appear with readable transitions in-game.
3. Replace generated entity textures with hand-polished art if a future visual pass has time.
4. Add custom `.ogg` sounds only if a later asset pass wants unique audio; the current release uses vanilla sounds intentionally.

## Guardrails

- Keep this spec aligned to `CLAUDE.md` and the current source tree.
- Do not reintroduce gold-armor or Bad Omen raid triggers.
- Do not reintroduce Blindness as a mob pressure mechanic.
- Do not reintroduce the old ranged/brute/chief/shaman roster.
- Do not use client-side hooks or imports in raid logic.
- Do not store direct entity references in persistent raid state.
- Do not mutate persistent raid state from structure chunk generation.
- Do not re-enable pre-raid spawners after raid start or raid completion.
- Do not use `loot_tables/`; use `loot_table/`.
- Use current Fabric and Minecraft `1.21.11` APIs when extending the mod.
- Do not reintroduce Accessories or `data/accessories` slot/tag files until a matching `1.21.11` Accessories build has been verified in Lunar Client.
- During structure world generation, do not read block entities outside the current `chunkBox`; generation must skip positions owned by neighboring chunks until those chunks are processed.
- During runtime portal proximity scans, never force-load chunks; query only loaded chunks and clear runtime-only server maps on shutdown.
