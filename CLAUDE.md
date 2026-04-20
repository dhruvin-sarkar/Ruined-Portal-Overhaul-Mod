# Ruined Portal Overhaul - Canonical Project Context

Last reconciled: 2026-04-20. Current build status: `./gradlew build` succeeds with Java 21 when `JAVA_HOME` points at `C:\Users\dhruv\.codex\jdks\temurin-21`.

This file is the single source of truth for the project. `SPEC.md` is the concise companion and must stay aligned with this file.

## Target Stack

- Minecraft: `1.21.11`
- Fabric Loader: `0.18.6`
- Fabric API: `0.141.3+1.21.11`
- Loom: `1.15-SNAPSHOT`
- Java: 21 toolchain, `sourceCompatibility`, and `targetCompatibility`
- Mappings: Mojang official mappings through `mappings loom.officialMojangMappings()`
- External accessory dependencies: none
- Mod ID: `ruined_portal_overhaul`
- Package root: `com.ruinedportaloverhaul`

Lunar compatibility note:

The original expansion targeted Accessories API, but the official Wisp Maven metadata currently has no `1.21.11` Accessories build. The newest available line is `1.4.3-beta+1.21.10`, and that jar crashes Lunar Client `1.21.11` while applying `accessories-common.mixins.json:client.InventoryScreenMixin` to `net.minecraft.class_490`. Keep this project free of the Accessories dependency until a matching `1.21.11` build has been verified in Lunar.

## Source Layout

```text
src/main/java/com/ruinedportaloverhaul/RuinedPortalOverhaul.java
src/main/java/com/ruinedportaloverhaul/client/RuinedPortalOverhaulClient.java
src/main/java/com/ruinedportaloverhaul/client/atmosphere/PortalAtmosphereClient.java
src/main/java/com/ruinedportaloverhaul/client/mixin/ClientLevelStormMixin.java
src/main/java/com/ruinedportaloverhaul/client/mixin/FogRendererMixin.java
src/main/java/com/ruinedportaloverhaul/client/mixin/SkyRendererMixin.java
src/main/java/com/ruinedportaloverhaul/client/mixin/WeatherEffectRendererMixin.java
src/main/java/com/ruinedportaloverhaul/client/render/*.java
src/main/java/com/ruinedportaloverhaul/advancement/*.java
src/main/java/com/ruinedportaloverhaul/block/*.java
src/main/java/com/ruinedportaloverhaul/block/entity/*.java
src/main/java/com/ruinedportaloverhaul/component/*.java
src/main/java/com/ruinedportaloverhaul/damage/*.java
src/main/java/com/ruinedportaloverhaul/entity/*.java
src/main/java/com/ruinedportaloverhaul/item/*.java
src/main/java/com/ruinedportaloverhaul/mixin/LivingEntityLavaMovementMixin.java
src/main/java/com/ruinedportaloverhaul/network/ModNetworking.java
src/main/java/com/ruinedportaloverhaul/network/PortalAtmospherePayload.java
src/main/java/com/ruinedportaloverhaul/network/NetherFireballPayload.java
src/main/java/com/ruinedportaloverhaul/network/NetherFireballHandler.java
src/main/java/com/ruinedportaloverhaul/raid/GoldRaidManager.java
src/main/java/com/ruinedportaloverhaul/raid/NetherDragonRituals.java
src/main/java/com/ruinedportaloverhaul/raid/PortalRaidState.java
src/main/java/com/ruinedportaloverhaul/structure/NetherConduitChestPlacement.java
src/main/java/com/ruinedportaloverhaul/structure/PortalDungeonPiece.java
src/main/java/com/ruinedportaloverhaul/structure/PortalDungeonStructure.java
src/main/java/com/ruinedportaloverhaul/structure/PortalStructureHelper.java
src/main/java/com/ruinedportaloverhaul/world/ModStructures.java
src/main/java/com/ruinedportaloverhaul/world/ModWorldGen.java
src/main/resources/fabric.mod.json
src/main/resources/ruined_portal_overhaul.mixins.json
src/main/resources/ruined_portal_overhaul.client.mixins.json
src/main/resources/assets/ruined_portal_overhaul/lang/en_us.json
src/main/resources/assets/ruined_portal_overhaul/models/block/*.json
src/main/resources/assets/ruined_portal_overhaul/models/item/*.json
src/main/resources/assets/ruined_portal_overhaul/textures/entity/*.png
src/main/resources/data/minecraft/worldgen/structure/ruined_portal*.json
src/main/resources/data/ruined_portal_overhaul/advancement/*.json
src/main/resources/data/ruined_portal_overhaul/damage_type/*.json
src/main/resources/data/ruined_portal_overhaul/loot_table/chests/*.json
src/main/resources/data/ruined_portal_overhaul/loot_table/entities/*.json
src/main/resources/data/ruined_portal_overhaul/recipe/*.json
src/main/resources/data/ruined_portal_overhaul/worldgen/configured_feature/*.json
src/main/resources/data/ruined_portal_overhaul/worldgen/placed_feature/*.json
```

## Master Content Expansion

This expansion adds an endgame dependency chain:

1. Native carried-necklace support avoids external accessory mixins for Lunar compatibility.
2. Nether stars are added to raid loot, making the new recipes attainable.
3. The Nether Conduit is found in structures and used by the Nether Crystal recipe.
4. The Ghast Tear Necklace uses native inventory scanning and the new nether-star economy.
5. Four Nether Crystals on generated pedestals summon the Nether Dragon.

No new PNG assets were added for these systems. Models and renderers reuse vanilla textures: conduit, netherrack, obsidian, ghast tear, end crystal, fire charge, and dragon-head icons.

## Lunar-Compatible Necklace Integration

Accessories is intentionally not a dependency in the Lunar-compatible build. Keep these conventions:

- `GhastTearNecklaceItem` extends vanilla `Item`, not `AccessoryItem`.
- `GhastTearNecklaceEvents` runs from the common initializer and applies passive necklace effects when the player carries the item anywhere in inventory.
- Server-side fireball lookup uses `GhastTearNecklaceItem.findCarriedNecklace(player)`.
- Necklace cooldown state still belongs in `ModDataComponents` on the carried stack.
- Do not add `data/accessories/...` tags or `data/<modid>/accessories/slot/...` files unless the project deliberately restores a verified compatible Accessories dependency later.
- `fabric.mod.json` must not depend on `"accessories"` for the Lunar-compatible jar.

## Nether Conduit

The Nether Conduit is a custom block/block entity pair:

- `ModBlocks`: registers `nether_conduit` block and matching `NetherConduitBlockItem`.
- `NetherConduitBlock`: handles shape, ticking block entity, and ancient debris upgrades.
- `NetherConduitBlockItem`: documents levels/effects in the item tooltip.
- `ModBlockEntities`: registers the block entity type.
- `NetherConduitBlockEntity`: owns activation, level state, player effects, action-bar status, nether mob attacks, and NBT persistence.
- `NetherConduitPowerTracker`: short-lived per-player lava movement boost state used by the lava movement mixin.
- `NetherConduitEvents`: allows sleeping in Nether-like dimensions when an active conduit is within 16 blocks.
- `LivingEntityLavaMovementMixin`: reduces lava movement drag/acceleration penalties while `NetherConduitPowerTracker` is active.
- `ModDamageTypes` plus `data/ruined_portal_overhaul/damage_type/nether_conduit.json`: custom damage source for conduit attacks.
- `NetherConduitChestPlacement`: deterministic exactly-one-per-structure chest insertion helper.

Activation and levels:

- Active frame requires at least 12 regular `minecraft:nether_bricks` blocks in the conduit-frame positions.
- Level 0: Fire Resistance I, Haste I, Regeneration I, 16-block support radius, 4 conduit damage.
- Level 1: Fire Resistance II, 20-block attack radius, 6 conduit damage.
- Level 2: Haste II, Regeneration II, 24-block attack radius, 8 conduit damage, near-zero lava movement penalty.
- Upgrades consume ancient debris: 1 for level 0 to 1, 2 for level 1 to 2.
- Effects refresh every 20 ticks with 40-tick duration. Mob attacks scan every 30 ticks and emit `ParticleTypes.ELECTRIC_SPARK`.
- Attack targets include Zombie Piglin, Piglin, Piglin Brute, Blaze, Wither Skeleton, Ghast, Hoglin, Magma Cube, and all seven custom combat mobs.

Acquisition:

- Exactly one Nether Conduit is inserted directly into either a deep chest or boss reward chest during structure generation.
- Each of the seven custom raid mobs has a 2% killed-by-player Nether Conduit drop pool.
- There is no crafting recipe for the Nether Conduit.

## Ghast Tear Necklace

The Ghast Tear Necklace is a native carried charm:

- `ModItems`: registers `ghast_tear_necklace`, stack size 1, fire resistant.
- `GhastTearNecklaceItem`: extends `Item`, exposes carried-stack lookup helpers, and applies Speed II plus Jump Boost II.
- `GhastTearNecklaceEvents`: server tick hook that applies passive effects while carried and triggers the first-carry advancement.
- `ModDataComponents`: registers `last_necklace_fireball_tick` as a persistent/networked long component on the carried stack.
- `NetherFireballPayload`: empty C2S payload for the keybind ability.
- `NetherFireballHandler`: server-side carried-stack lookup, cooldown check, fireball spawn, sound, stack component update, and advancement trigger.
- `NetherFireballKeybinds`: client keybind registration, default key `G`, sends the payload only when the server supports it.
- `RuinedPortalOverhaulClient`: registers the keybind from the client initializer.

Passive effects refresh every 40 ticks with 80-tick duration while the necklace is carried. The fireball ability has a 2400-tick cooldown, spawns a vanilla `SmallFireball` owned by the player, and uses `SoundEvents.GHAST_SHOOT`.

## Nether Crystal And Nether Dragon

The Nether Crystal ritual is the endgame loop:

- `NetherCrystalEntity`: extends vanilla `EndCrystal` and returns the custom crystal item as pick result.
- `NetherCrystalRenderer`: client renderer using the vanilla end-crystal model/texture with a dark red tint.
- `NetherCrystalItem`: places crystals only on `minecraft:netherite_block` or `minecraft:obsidian`, spawns `NetherCrystalEntity`, and calls the ritual tracker.
- `ModEntities`: registers `nether_crystal` and `nether_dragon`.
- `PortalStructureHelper`: places four netherite pedestals at offsets north/south/east/west six blocks from the portal center and exposes `ritualPedestalPositions(...)`.
- `PortalRaidState`: persists filled ritual pedestals and active dragon portals.
- `NetherDragonRituals`: tracks crystal placement, starts the summoning sequence, manages the Nether Dragon boss bar, drops death rewards, and shatters pedestals.
- `NetherDragonEntity`: extends vanilla `EnderDragon`, suppresses End fight hooks, suppresses crystal healing, sets 300 HP, and delegates custom death rewards to `NetherDragonRituals`.

Ritual conditions:

- The portal must be completed/lit.
- A Nether Crystal must be placed on top of each generated pedestal.
- No dragon may already be active for that portal.

Summoning sequence:

- Tick 0: Wither spawn sound plus flame and large smoke sphere around the portal.
- Tick 40: Nearby title `The Nether Dragon Awakens`, subtitle `Flee or fight.`
- Tick 80: Nether Dragon spawns at portal center plus `(0, 10, 0)` and plays the dragon growl.

Death behavior:

- Drops 2 Nether Stars, 1-3 Ancient Debris, and 500 XP through the advancement reward.
- Removes the four Nether Crystals and replaces the four netherite pedestals with air.
- Does not spawn an End portal and does not use End-crystal healing.

## Recipes

Recipes live in singular `data/ruined_portal_overhaul/recipe/`.

- `ghast_tear_necklace.json`: shaped `TST/GTG/TST`, where `T = minecraft:ghast_tear`, `S = minecraft:nether_star`, and `G = minecraft:gold_ingot`. This exact grid uses five ghast tears, two nether stars, and two gold ingots.
- `nether_crystal.json`: shaped `CSC/SNS/ISI`, where `C = minecraft:crying_obsidian`, `S = minecraft:nether_star`, `N = ruined_portal_overhaul:nether_conduit`, and `I = minecraft:netherite_ingot`.

## Structure Generation

`PortalDungeonStructure` creates one `PortalDungeonPiece` at the chunk center. `PortalDungeonPiece` owns generation order and delegates block placement to `PortalStructureHelper`.

The generated piece uses a radius-136 surface footprint and a depth-45 underground rupture:

- Inner zone, radius `0-15`: stable ritual core with netherrack ground, blackstone brick platform, valid 4x5 or 6x7 portal frame, chains, and Exiled Piglin anchor. The ritual platform and frame stay at a consistent readable height.
- Middle zone, radius `15-52`: netherrack-dominant Nether scar with contained lava pools and grouped basalt pillar formations. The ground now uses deterministic low-frequency height variation outside the ritual core, clamped to roughly `-3` to `+3` blocks, so the surface reads as a mostly flat Nether plain with organic undulation rather than jagged stacked terrain.
- Outer zone, radius `52-136`: lower-density netherrack corruption scatter using the same calm surface sculpting, so outer patches inherit gentle rises and depressions.
- Underground pit: protected original-style ragged mouth, organic shaft, 12 lower lava seeps, mixed basalt/blackstone/netherrack/soul-soil rim rubble, and Nether material conversion around carved space.
- Primary chamber: large blackstone/basalt/netherrack cavern with lava lake, vents, glowstone clusters, stalactites, and basalt/blackstone spikes.
- Cave network: denser graph-based cave nodes connected by worm-carved organic tunnels. Each tunnel advances by a gradually blended noisy direction, varies radius from roughly 2-6 blocks with an independent smooth noise signal, drifts vertically, and creates side pockets. Deep nodes are larger and taller, including ghast-ready caverns with high ceilings, more blackstone/basalt material, lava runs, ceiling drips, glowstone pockets, and frequent underground cache pads.
- Altar and caches: blackstone brick altar with crying obsidian focus, two altar `portal_deep` chests, plus many cave-node `portal_deep` caches distributed through shallow, middle, and deep branches.
- Ritual pedestals: four `minecraft:netherite_block` pedestals are placed at ground level exactly 6 blocks north, south, east, and west of the portal center. These are generated by the structure and are the only canonical Nether Dragon ritual pedestals.
- Nether Conduit guarantee: after structure chest placement, `NetherConduitChestPlacement` deterministically picks either a deep chest or the boss reward chest path and inserts exactly one Nether Conduit directly into the selected chest inventory/NBT. This is not loot-table driven.

Pit, chamber, and tunnel carvers replace adjacent overworld stone, deepslate, dirt, and common overworld ore/geology blocks with Nether materials so natural cave intersections read as corrupted Nether geology. Structure-local water encountered in transformed terrain is converted into lava with a bounded 8-block vertical clear. All writes are bounded by both the structure piece box and current chunk box.

Overworld ruined portal structure JSON files are overridden to use `ruined_portal_overhaul:portal_dungeon`; vanilla structure-set spacing remains 40 and separation remains 15.

## Spawners And Spawn Pressure

Structure generation places deterministic pre-raid spawners, then `GoldRaidManager` scans/persists those positions when a portal is approached and deletes them when the raid starts. Spawners are not re-enabled after the raid starts or completes. On raid completion, known structure spawners are permanently disabled again before the portal is marked complete.

Spawner pressure now escalates by depth:

- Surface spawners: zombified piglins, magma cubes, piglin pillagers, piglin vindicators, and one blaze slot across a ring around the portal.
- Primary chamber spawners: magma cube, blaze, wither skeleton, piglin brute pillager, piglin pillager, and piglin vindicator positions.
- Tunnel and cave spawners: up to 28 positions from node centers, tunnel midpoints, and branch endpoints. Upper tunnels use magma cubes, pillagers, vindicators, blazes, and wither skeletons. Lower tunnels add brutes and illusioners. Deep cave spawners add ghasts, evokers, more brutes, and stronger blaze/wither pressure.
- Ghast spawners use low spawn count, larger spawn range, larger player range, and deep cavern placement so ghasts have space to move and fire.

Runtime structure-local ambient spawning is also owned by `GoldRaidManager`, not `BiomeModifications`:

- Ground ambient cap: `180` tagged mobs per portal footprint.
- Ambient tick interval: every `10` server ticks.
- Burst size: `8-12` on the surface, `10-14` in lower caves, and `12-16` in deepest caves.
- Outer surface pool: zombified piglins, piglin pillagers, piglin vindicators, magma cubes, rare blazes.
- Middle pool: piglin pillagers, vindicators, brutes, blazes, wither skeletons, magma cubes, zombified piglins.
- Inner pool: blazes, wither skeletons, brutes, vindicators, magma cubes, illusioners, pillagers.
- Lower underground pool: blazes, wither skeletons, magma cubes, brutes, illusioners, vindicators, pillagers.
- Deep underground pool: wither skeletons, blazes, magma cubes, brutes, illusioners, evokers, vindicators.
- Anchored ghasts: cap `16`, spawn every `25-55` ticks when available, use a clear-volume check, are tagged to their portal origin, and are discarded if they drift beyond the anchor radius or timeout.

Completed portal suppression:

- `GoldRaidManager.initialize()` registers a server entity-load hook that discards hostile mobs loaded inside completed portal footprints.
- Suppression intentionally skips the Exiled Piglin trader and mobs requiring persistence so reward/trader behavior is not broken.
- Runtime ambient portal spawning already ignores completed portals, so completed structures stay quiet.

## Raid Trigger And Flow

The manager class is still named `GoldRaidManager` for compatibility with the existing codebase, but the raid trigger is no longer gold armor and no Bad Omen is involved.

`GoldRaidManager` runs from `ServerTickEvents.END_SERVER_TICK`, only in the overworld, and scans players every `10` server ticks.

Trigger phases:

- Approach zone: any player horizontally within `136` blocks of an uncompleted generated portal activates the portal once. The player receives `...something stirs.` and the portal plays a low portal ambient sound.
- Raid trigger: any player horizontally within `28` blocks of an uncompleted, inactive generated portal starts the raid. This distance is deliberate enough to require entering the ritual area without springing the raid from the far outer scar.
- Distance checks for approach, atmosphere, boss bars, completion feedback, and raid trigger use horizontal X/Z distance where portal-zone membership matters, so the storm and raid work in the pit and cave system below the frame.

Raid start:

- Uses `PortalRaidState.beginRaid()` as the active flag check-and-set.
- Plays raid start sounds and particles.
- Sends title `The Red Storm Breaks` and subtitle `Survive the waves...` to nearby players.
- Deletes known/scanned pre-raid spawners.
- Creates a `ServerBossEvent` and spawns wave 1 in an adaptive 14-24 block ring using `Heightmap.Types.MOTION_BLOCKING`.

Current wave table:

| Wave | Boss Bar Label | Composition |
|---|---|---|
| 1 | `The Red Storm Breaks` | 12x PiglinPillager, 8x PiglinVindicator |
| 2 | `They Grow Bolder` | 14x PiglinPillager, 8x PiglinVindicator, 5x PiglinBrutePillager |
| 3 | `The Brutes Arrive` | 12x PiglinPillager, 9x PiglinVindicator, 8x PiglinBrutePillager, 5x PiglinIllusioner |
| 4 | `Chaos Unleashed` | 8x PiglinPillager, 10x PiglinBrutePillager, 8x PiglinIllusioner, 10x PiglinVindicator, 1x PiglinRavager with mounted PiglinVindicator, 2x PiglinEvoker |
| 5 | `The Evoker Awakens` | 14x PiglinPillager, 12x PiglinVindicator, 10x PiglinBrutePillager, 7x PiglinIllusioner, 3x PiglinRavager, 4x PiglinEvoker |

Completion order:

1. Grant nearby players the raid-complete custom advancement trigger.
2. Remove all boss-bar players and hide the bar.
3. Play completion fanfare.
4. Ignite the portal.
5. Spawn the boss reward chest.
6. Spawn the Exiled Piglin trader at the anchor.
7. Mark the portal completed in persistent state.
8. Send nearby action-bar feedback: `The portal falls silent.`

## Persistence And Multiplayer

- Completed portals, active raids, approach activations, ritual crystal fills, active dragon portals, and known pre-raid spawner positions are tracked per portal `BlockPos`.
- `PortalRaidState.CODEC` uses save-compatible optional defaults for newer fields.
- Active wave mobs are stored as UUIDs, never direct entity references.
- Active raids rehydrate from persistent state after server restart.
- Active raids pause mob-death evaluation while the portal area is not entity-ticking, so unloaded mobs are not counted as dead.
- Boss bars track all players horizontally within 48 blocks of the active portal and remove players who leave range or disconnect.
- Ritual state persists as portal-origin to filled-pedestal sets. Dragon activity is stored separately so placing replacement crystals cannot start duplicate fights while a dragon is active.

## Red Storm And Audio

The red storm is a client-side visual/audio system driven by server proximity packets:

- `GoldRaidManager` sends `PortalAtmospherePayload` every `10` ticks while a player is horizontally inside the radius-136 zone and the portal is incomplete.
- Packet intensity uses horizontal distance and never falls below `0.22` while in-zone.
- Packet descent uses how far the player is below the portal frame, making pit and cave atmosphere tighter and more intense.
- `PortalAtmosphereClient` eases target intensity/descent, fades when packets stop, applies a 4.2-second breathing pulse, and renders the HUD tint. Current tint strength is about 15-20% more pronounced than the earlier storm pass.
- `ClientLevelStormMixin` makes the local client report rain/thunder gradients during the storm.
- `WeatherEffectRendererMixin` forces red rain visuals.
- `SkyRendererMixin` tints the sky toward a dark red storm color and dims rain brightness.
- `FogRendererMixin` tints fog red and tightens fog distance, especially underground.
- Red thunder is generated on a client-side storm timer that is roughly twice as frequent as the earlier storm pass. Thunder uses a 2-3 tick deep-red HUD flash instead of the vanilla white sky flash, layered with vanilla lightning thunder, low-volume wither spawn, and portal trigger sounds. It does not rely on real world weather.
- Storm music starts when storm intensity rises: `SoundEvents.MUSIC_BIOME_BASALT_DELTAS` is played through the client `MusicManager`, then stopped when the player leaves the zone or the raid completes and packets fade.

Server-side atmosphere remains active too: ash, crimson spores, smoke, lava drips, frame particles, lava ambience, raid start bursts, inter-wave pulses, completion particles, and mob spawn sounds are all server-side effects.

## Territory Boon

Incomplete portal territories now give players a protective red-aether boon while they remain horizontally inside the radius-136 zone:

- Regeneration II, Resistance I, Fire Resistance I, and Absorption IV are reapplied every 10 server ticks with a 260-tick duration, matching the functional protection profile of an enchanted golden apple for as long as the player stays in the territory.
- The effect is tied to the same horizontal portal-zone logic as the atmosphere, so it applies on the surface, in the pit, and throughout the caves below the portal.
- The first time each player enters a specific uncompleted portal territory, the portal grants one Totem of Undying. This is protected by the per-player entity tag `rpo_totem_granted_<portalOriginLong>` so each portal gives each player one starter totem, not an infinite farm.
- The boon fires the `aether_boon` custom advancement trigger, and the starter totem fires the `territory_totem` custom advancement trigger.

## Mob Roster

| Entity ID | Class | Base | Behavior |
|---|---|---|---|
| `piglin_pillager` | `PiglinPillagerEntity` | Pillager | Fire immune, Quick Charge III crossbow with Piercing II or Multishot variation, 44 HP, 9.5 arrow damage |
| `piglin_vindicator` | `PiglinVindicatorEntity` | Vindicator | Fire immune, golden axe/sword, guaranteed Sharpness III-V, chance of Fire Aspect and Knockback, 58 HP, 16.5 attack |
| `piglin_brute_pillager` | `PiglinBrutePillagerEntity` | Pillager | Fire immune, mostly melee golden axe/sword loadouts with Sharpness IV-V, rare Multishot + Quick Charge II crossbow variant, close-range melee fallback for crossbow brutes, 88 HP, 20 attack, 11 arrow damage |
| `piglin_illusioner` | `PiglinIllusionerEntity` | Illusioner | Fire immune, Flame + Power III bow with Punch I variation, 54 HP, 8 arrow damage, and Nether combat sounds for arrows |
| `piglin_evoker` | `PiglinEvokerEntity` | Evoker | Fire immune, 70 HP, 10-fang Magma Eruption with blaze/lava cue every 160 ticks, ignites targets, summons 4 Piglin Vexes every 220 ticks, summons 2 desperation Piglin Vexes below 50% HP with NBT persistence |
| `piglin_ravager` | `PiglinRavagerEntity` | Ravager | Fire immune, 210 HP, 24 attack, half projectile damage, Hoglin sound set, Slowness roar, wave-4 rider owned by `GoldRaidManager`, Hard wall/obsidian charge roar applying Slowness II for 60 ticks |
| `piglin_vex` | `PiglinVexEntity` | Vex | Fire immune, 28 HP, 10 attack, limited life of 1400 ticks, summoned by Piglin Evoker |
| `exiled_piglin` | `ExiledPiglinTraderEntity` | Wandering Trader | Invulnerable reward trader with Piglin sounds, one customer at a time, action-bar messages, restock every 40000 ticks, despawn after 72000 world ticks |

All seven combat mobs call `PiglinDifficultyScaler.applyHardHealth(...)` from their 1.21.11 spawn initialization hook. On Hard, they receive +50% max health and +60% attack damage when the attack attribute exists.

## Loot Tables
Loot table files contain `_comment` fields documenting reward intent.

- `chests/portal_surface`: `9-12` rolls. Surface generation now places six prep chests around the scar. Loot includes more gold/iron/obsidian, Fire Resistance, Strength II, Regeneration II, healing splash potions, golden apples/carrots, rare enchanted golden apples, emergency totems, enchanted gold gear, Fire Protection IV books, diamonds, and rare netherite scrap. Golden apple weight is increased to 7.
- `chests/portal_deep`: `12-16` rolls. Underground generation now adds frequent cave caches beyond the two altar chests. Loot includes larger stacks of netherite scraps, ancient debris, upgrade templates, rare netherite ingots, diamonds, enchanted diamond gear, totems, enchanted golden apples, Mending, Efficiency V, Sharpness V, Protection IV, Thorns III, Sharpness III diamond swords, a rare Nether Star weight, other strong books, and survival potions.
- `chests/portal_boss_reward`: `16-20` rolls plus a two-roll high-weight totem/enchanted-golden-apple bonus pool, a guaranteed Nether Star pool, and a guaranteed named `Shard of the Nether`. Includes 2-5 netherite ingots, larger netherite scrap and ancient debris stacks, multiple upgrade templates, enchanted golden apples, common totems, diamonds, gold blocks, high-tier books, enchanted diamond gear, and a weighted named `Corrupted Portal Key`.
- Entity loot tables are under `data/ruined_portal_overhaul/loot_table/entities/`. All custom combat mob drops are now richer, with more gold, food, ammunition, gear, potion ingredients, books, totems from high-threat mobs, and rare netherite/debris drops from deeper wave threats.
- Nether Star entity drops: Piglin Evoker has a 5% killed-by-player chance, Piglin Ravager has 3%, and Piglin Illusioner has 1%.
- Nether Conduit entity drops: all seven custom combat mobs have a 2% killed-by-player chance.

## Advancements

Advancements live under `data/ruined_portal_overhaul/advancement/`. Vanilla criteria cover inventory and kill events; custom player-event triggers live in `com.ruinedportaloverhaul.advancement`.

Custom trigger classes:

- `PortalEventTrigger`: a `PlayerTrigger` subclass used for portal-specific player events.
- `ModAdvancementTriggers`: registers `portal_approach`, `pit_descent`, `deep_storm`, `aether_boon`, `territory_totem`, `raid_started`, `raid_completed`, `exiled_trade`, `nether_conduit_level_2`, `ghast_tear_necklace_equipped`, `nether_fireball_used`, `nether_crystal_ritual_complete`, and `nether_dragon_defeated` from the common initializer and exposes a small trigger helper.

Current advancement tree:

| Advancement | File | Trigger |
|---|---|---|
| `Something Stirs` | `root.json` | Custom `portal_approach`, fired when a player first activates an uncompleted portal's approach zone |
| `The Air Cheats` | `the_air_cheats.json` | Custom `aether_boon`, fired when the portal territory applies the enchanted-golden-apple-style aether boon |
| `Not Yours Anymore` | `not_yours_anymore.json` | Custom `territory_totem`, fired when the player receives the once-per-portal starter Totem of Undying |
| `Take Only What Screams` | `surface_plunder.json` | `inventory_changed` for a surface-useful golden apple or potion |
| `Down the Red Throat` | `down_the_red_throat.json` | Custom `pit_descent`, fired from horizontal portal-zone atmosphere checks when the player is 8+ blocks below the frame |
| `The Weather Is Down Here` | `the_weather_is_down_here.json` | Hidden custom `deep_storm`, fired only when a player remains in the horizontal portal zone at least 24 blocks below the frame |
| `Cache Problem` | `cache_problem.json` | `inventory_changed` for a netherite upgrade template or netherite scrap from the denser underground caches |
| `No Tribute Required` | `no_tribute_required.json` | Custom `raid_started`, fired when proximity starts the raid |
| `Hostile Acquisition` | `hostile_acquisition.json` | `player_killed_entity` for Piglin Pillager, Piglin Vindicator, or Piglin Brute Pillager |
| `Large Print` | `large_print.json` | `player_killed_entity` for Piglin Ravager |
| `Contract Voided` | `contract_voided.json` | `player_killed_entity` for Piglin Evoker |
| `The Good Chest Was Lower` | `the_good_chest_was_lower.json` | `inventory_changed` for ancient debris or a Totem of Undying from deep/boss loot |
| `The Last Wave Broke` | `the_last_wave_broke.json` | Challenge custom `raid_completed`, fired for nearby players when wave 5 is cleared |
| `No Clan, Still Capitalism` | `no_clan_still_capitalism.json` | Custom `exiled_trade`, fired from `ExiledPiglinTraderEntity.notifyTrade(...)` |
| `Paid in Scar Tissue` | `paid_in_scar_tissue.json` | `inventory_changed` for netherite ingots from the boss reward |
| `Nether Bond` | `nether_bond.json` | `inventory_changed` for `ruined_portal_overhaul:nether_conduit` |
| `Fully Awakened` | `fully_awakened.json` | Challenge custom `nether_conduit_level_2`, fired when ancient debris upgrades a conduit to level 2 |
| `Draped in Sorrow` | `draped_in_sorrow.json` | Custom `ghast_tear_necklace_equipped`, fired from `GhastTearNecklaceItem.onEquip(...)` |
| `Ghost Fire` | `ghost_fire.json` | Custom `nether_fireball_used`, fired after the server spawns the necklace fireball |
| `The Final Offering` | `the_final_offering.json` | Challenge custom `nether_crystal_ritual_complete`, fired when all four pedestal crystals complete the ritual |
| `Here Be Dragons` | `here_be_dragons.json` | Challenge custom `nether_dragon_defeated`, fired when the Nether Dragon death rewards are handled |

## Exiled Piglin Trades

| Cost | Result | Stock |
|---|---|---|
| 8 gold ingots | 3 magma cream | 5 |
| 12 gold ingots | 2 crying obsidian | 4 |
| 6 gold ingots | 16 nether brick | 8 |
| 10 gold ingots | 4 blaze powder | 6 |
| 18 gold ingots | 3 obsidian | 4 |
| 24 gold ingots | 1 golden apple | 2 |
| 2 gold blocks | 1 netherite upgrade smithing template | 1 |
| 32 gold ingots | 1 ancient debris | 1 |

The magma cream trade is not a gold loop because vanilla has no crafting path from magma cream or magma blocks back into gold.

## Global Worldgen

`ModWorldGen.initialize()` injects these low-density overworld lore features:

- `ruined_portal_overhaul:underground_netherrack_blob`
- `ruined_portal_overhaul:underground_soul_sand_pocket`
- `ruined_portal_overhaul:underground_blackstone_vein`

It also injects rare overworld monster spawns:

- Zombified Piglin, weight 1, group 1-2
- Blaze, weight 1, group 1

Do not use global biome modifications for structure-local proximity gradients.

## Metadata

`fabric.mod.json` is release-oriented:

- Version: `1.0.0`
- Author: `Dhruv Sarkar`
- Sources: `https://github.com/dhruvin-sarkar/Ruined-Portal-Overhaul-Mod`
- Issues: `https://github.com/dhruvin-sarkar/Ruined-Portal-Overhaul-Mod/issues`
- Client entrypoint: `com.ruinedportaloverhaul.client.RuinedPortalOverhaulClient`
- Required dependencies: Minecraft, Fabric Loader, and Fabric API
- Common mixin config: `ruined_portal_overhaul.mixins.json`
- Client mixin config: `ruined_portal_overhaul.client.mixins.json`

## Implementation Notes

- Use `Identifier.fromNamespaceAndPath(...)` in this Mojang-mapped codebase.
- Use Mojang names from local mappings. Do not paste Yarn-only method or field names into this project.
- Use `level()` / `ServerLevel` guarded server logic.
- Use `Attributes.MAX_HEALTH`, `Attributes.MOVEMENT_SPEED`, and `Attributes.ATTACK_DAMAGE`, not old `GENERIC_*` names.
- Use the 1.21.11 server damage override pattern `hurtServer(ServerLevel, DamageSource, float)`.
- Use `loot_table/`, not `loot_tables/`.
- Do not import `net.minecraft.client.*` outside `com.ruinedportaloverhaul.client`.
- Register common content from `RuinedPortalOverhaul.onInitialize()`.
- Register common play payload types through `ModNetworking` before server code sends them.
- Register renderers, client packet receivers, HUD atmosphere, and client mixins only from client-side configuration.
- Do not reintroduce Accessories until a matching `1.21.11` Accessories release has been verified in Lunar Client.
- Necklace behavior is implemented through native carried-item scanning, not external accessory slots.
- Item stack cooldown state for the necklace belongs in `ModDataComponents`, not ad hoc NBT helpers.
- Keep the Nether Conduit lava movement changes in the common mixin config. Client-only mixins cannot change server movement.
- Use `EndCrystal` and `EnderDragon` Mojang classes for the Nether Crystal/Dragon implementation.
- Retrieve persistent raid state from `server.overworld()`.
- Call `setDirty()` after every persistent state mutation.

## Project Progress

| Component | Status |
|---|---|
| Build config for MC 1.21.11, Java 21, Mojang mappings | COMPLETE |
| Main initializer and client initializer | COMPLETE |
| Client red storm packet receiver, overlay, music, thunder, sky, fog, weather mixins | COMPLETE |
| 7 combat entities plus Exiled Piglin registration | COMPLETE |
| Entity renderer registration | COMPLETE |
| Non-solid generated entity texture sheets | COMPLETE |
| Structure generation and vanilla ruined portal replacement hooks | COMPLETE |
| Calm low-frequency surface height variation around stable ritual core | COMPLETE |
| Graph-based cave nodes, noisy worm tunnels, side pockets, lava features, and ghast-ready deep caverns | COMPLETE |
| Runtime structure-local portal-zone ambient spawns and anchored ghasts | COMPLETE |
| Depth-escalating pre-raid spawners | COMPLETE |
| Proximity-only raid trigger, no gold armor, no Bad Omen | COMPLETE |
| Persistent completed portal tracking, active raid metadata, restart rehydration | COMPLETE |
| Multiplayer atomic raid start guard and boss-bar membership sync | COMPLETE |
| Hard difficulty +50% health/+60% attack scaling and mob weapon upgrades | COMPLETE |
| Exiled Piglin trades, invulnerability, restock, despawn, messages, and single-customer gate | COMPLETE |
| Loot table reward arc: surface, deep, boss, and richer mob drops | COMPLETE |
| Territory aether boon and once-per-portal starter totem | COMPLETE |
| Advancement tree and custom portal event triggers | COMPLETE |
| Lunar-compatible native carried-necklace support | COMPLETE |
| Post-raid hostile spawn suppression in completed portal areas | COMPLETE |
| Nether Star loot economy for raid mobs and chests | COMPLETE |
| Nether Conduit block, block entity, activation frame, effects, upgrades, sleep, lava movement, and attacks | COMPLETE |
| Guaranteed Nether Conduit structure chest insertion plus 2% raid-mob conduit drops | COMPLETE |
| Ghast Tear Necklace carried charm, passive effects, server fireball ability, cooldown component, keybind, and recipe | COMPLETE |
| Improved surface, deep, and boss chest loot tables | COMPLETE |
| Nether Crystal entity, tinted renderer, placement item, and recipe | COMPLETE |
| Netherite pedestal structure generation and persisted ritual tracking | COMPLETE |
| Nether Dragon entity, summoning sequence, boss bar, death rewards, and pedestal shattering | COMPLETE |
| New advancements for conduit, necklace, crystal ritual, and dragon fight | COMPLETE |
| `./gradlew build` with Java 21 | COMPLETE |
| Full interactive `runClient` survival smoke test with log review | PENDING |

## Known Limitations

- The entity textures are simple generated release textures, not hand-painted final art.
- The mod uses vanilla Minecraft sound events rather than shipping custom `.ogg` assets.
- The storm renderer now uses client mixins for sky, fog, rain, and weather state. These hooks are version-sensitive and should be rechecked whenever updating Minecraft mappings.
- A full interactive `runClient` survival smoke test still needs to be performed before final submission.

## What Not To Do

- Do not reintroduce the old ranged/brute/chief/shaman roster or Nether Shaman as canonical entities.
- Do not target Fabric 1.21.1 or Fabric API 0.100.x.
- Do not use Yarn names when the build uses Mojang mappings.
- Do not create loot tables under `loot_tables/`.
- Do not put entity renderer registration in the common initializer.
- Do not store direct entity references in raid state.
- Do not count persisted wave mob UUIDs as dead while the portal area is not entity-ticking.
- Do not restore the old gold-armor or Bad Omen raid trigger.
- Do not spawn raid guard entities from `PortalDungeonPiece` world generation.
- Do not mutate `PortalRaidState` from structure chunk generation.
- Do not re-enable pre-raid spawners after the raid starts or completes.
- Do not use biome modifications for structure-local proximity gradients.
- Do not remove the Java 21 toolchain configuration.
- Do not reintroduce Accessories or necklace slot/tag data files until a compatible `1.21.11` build has been verified in Lunar Client.
- Do not add custom PNGs for the new conduit, necklace, or crystal systems unless the asset policy explicitly changes.
- Do not make the Nether Conduit craftable; it is intentionally structure/drop gated.
- Do not let the Nether Dragon heal from End Crystals or spawn an End portal on death.
