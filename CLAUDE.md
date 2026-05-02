# Ruined Portal Overhaul - Canonical Project Context

Last reconciled: 2026-05-02. Current build status: `./gradlew build` succeeds with Java 21 when `JAVA_HOME` points at `C:\Users\dhruv\.codex\jdks\temurin-21`; resource processing, language keys, sound subtitles, GeckoLib assets, custom particles, Patchouli data files, loot tables, and recipe data all pass the Gradle build pipeline and static JSON/resource checks.

This file is the single source of truth for the project. `SPEC.md` is the concise companion and must stay aligned with this file.

## Target Stack

- Minecraft: `1.21.11`
- Fabric Loader: `0.18.6`
- Fabric API: `0.141.3+1.21.11`
- Loom: declared as `1.15-SNAPSHOT`, resolved by Gradle as Fabric Loom `1.15.5`
- Java: 21 toolchain, `sourceCompatibility`, and `targetCompatibility`
- Mappings: Mojang official mappings through `mappings loom.officialMojangMappings()`
- External accessory dependencies: none
- Mod ID: `ruined_portal_overhaul`
- Package root: `com.ruinedportaloverhaul`

Lunar compatibility note:

The original expansion targeted Accessories API, but the official Wisp Maven metadata still has no `1.21.11` Accessories build as of 2026-04-23. The newest available line remains `1.4.3-beta+1.21.10`, and that published jar still declares `"minecraft": "~1.21.10"` plus `"owo": ">=0.12.24+1.21.9"` inside its own `fabric.mod.json`. That is not a truthful dependency match for this `1.21.11` branch, and the same jar also crashes Lunar Client `1.21.11` while applying `accessories-common.mixins.json:client.InventoryScreenMixin` to `net.minecraft.class_490`. Keep this project free of the Accessories dependency until a matching `1.21.11` build has been verified in Lunar.

## Source Layout

```text
src/main/java/com/ruinedportaloverhaul/RuinedPortalOverhaul.java
src/main/java/com/ruinedportaloverhaul/client/RuinedPortalOverhaulClient.java
src/main/java/com/ruinedportaloverhaul/client/atmosphere/PortalAtmosphereClient.java
src/main/java/com/ruinedportaloverhaul/client/mixin/ClientLevelStormMixin.java
src/main/java/com/ruinedportaloverhaul/client/mixin/FogRendererMixin.java
src/main/java/com/ruinedportaloverhaul/client/mixin/SkyRendererMixin.java
src/main/java/com/ruinedportaloverhaul/client/mixin/WeatherEffectRendererMixin.java
src/main/java/com/ruinedportaloverhaul/client/particle/*.java
src/main/java/com/ruinedportaloverhaul/client/render/*.java
src/main/java/com/ruinedportaloverhaul/client/render/geo/*.java
src/main/java/com/ruinedportaloverhaul/client/render/geo/model/*.java
src/main/java/com/ruinedportaloverhaul/client/render/geo/RuinedPortalGeoRenderData.java
src/main/java/com/ruinedportaloverhaul/advancement/*.java
src/main/java/com/ruinedportaloverhaul/block/*.java
src/main/java/com/ruinedportaloverhaul/block/entity/*.java
src/main/java/com/ruinedportaloverhaul/component/*.java
src/main/java/com/ruinedportaloverhaul/damage/*.java
src/main/java/com/ruinedportaloverhaul/entity/*.java
src/main/java/com/ruinedportaloverhaul/entity/TextureVariantHelper.java
src/main/java/com/ruinedportaloverhaul/entity/TextureVariantMob.java
src/main/java/com/ruinedportaloverhaul/item/*.java
src/main/java/com/ruinedportaloverhaul/mixin/LivingEntityLavaMovementMixin.java
src/main/java/com/ruinedportaloverhaul/network/ModPackets.java
src/main/java/com/ruinedportaloverhaul/network/PortalAtmospherePayload.java
src/main/java/com/ruinedportaloverhaul/network/NetherFireballPayload.java
src/main/java/com/ruinedportaloverhaul/network/NetherFireballHandler.java
src/main/java/com/ruinedportaloverhaul/raid/GoldRaidManager.java
src/main/java/com/ruinedportaloverhaul/raid/NetherDragonRituals.java
src/main/java/com/ruinedportaloverhaul/raid/PortalRaidState.java
src/main/java/com/ruinedportaloverhaul/block/NetherConduitChestPlacement.java
src/main/java/com/ruinedportaloverhaul/structure/PortalDungeonPiece.java
src/main/java/com/ruinedportaloverhaul/structure/PortalDungeonStructure.java
src/main/java/com/ruinedportaloverhaul/structure/PortalDungeonVariant.java
src/main/java/com/ruinedportaloverhaul/structure/StructureBlockPalette.java
src/main/java/com/ruinedportaloverhaul/structure/PortalStructureHelper.java
src/main/java/com/ruinedportaloverhaul/mixin/NaturalSpawnerSuppressionMixin.java
src/main/java/com/ruinedportaloverhaul/world/ModStructures.java
src/main/java/com/ruinedportaloverhaul/world/ModParticles.java
src/main/java/com/ruinedportaloverhaul/world/ModNaturalSpawnGuards.java
src/main/java/com/ruinedportaloverhaul/world/ModWorldGen.java
src/main/resources/fabric.mod.json
src/main/resources/ruined_portal_overhaul.mixins.json
src/main/resources/ruined_portal_overhaul.client.mixins.json
src/main/resources/assets/ruined_portal_overhaul/lang/en_us.json
src/main/resources/assets/ruined_portal_overhaul/geckolib/animations/block/*.json
src/main/resources/assets/ruined_portal_overhaul/geckolib/animations/entity/*.json
src/main/resources/assets/ruined_portal_overhaul/geckolib/models/block/*.geo.json
src/main/resources/assets/ruined_portal_overhaul/geckolib/models/entity/*.geo.json
src/main/resources/assets/ruined_portal_overhaul/models/block/*.json
src/main/resources/assets/ruined_portal_overhaul/models/item/*.json
src/main/resources/assets/ruined_portal_overhaul/particles/*.json
src/main/resources/assets/ruined_portal_overhaul/textures/entity/*.png
src/main/resources/assets/ruined_portal_overhaul/textures/particle/*.png
src/main/resources/assets/ruined_portal_overhaul/patchouli_books/corrupted_chronicle/en_us/**/*.json
src/main/resources/data/minecraft/worldgen/structure/ruined_portal*.json
src/main/resources/data/minecraft/worldgen/structure_set/ruined_portals.json
src/main/resources/data/ruined_portal_overhaul/advancement/*.json
src/main/resources/data/ruined_portal_overhaul/damage_type/*.json
src/main/resources/data/ruined_portal_overhaul/jukebox_song/*.json
src/main/resources/data/ruined_portal_overhaul/loot_table/chests/*.json
src/main/resources/data/ruined_portal_overhaul/loot_table/entities/*.json
src/main/resources/data/ruined_portal_overhaul/patchouli_books/corrupted_chronicle/book.json
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
6. Masterwork rewards now extend the dragon and boss chest loop with Portal Shard discovery, Corrupted Netherite armor, rare Nether Tide music, optional Patchouli guide data, custom particles, and special underground room templates.

No new hand-painted PNG assets were added for the conduit, necklace, crystal, or dragon systems. The mob roster now also ships generated placeholder GeckoLib texture variants derived from the base entity sheets for visual variety without introducing bespoke art dependencies.

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
- `RuinedPortalOverhaulClient`, `NetherConduitGeoRenderer`, `NetherConduitInnerGlowLayer`, and `NetherConduitGeoModel`: register and render the GeckoLib conduit block entity animation on the client.
- `NetherConduitPowerTracker`: short-lived per-player lava movement boost state used by the lava movement mixin and cleared on server stop so integrated-server world swaps cannot inherit stale conduit boosts.
- `NetherConduitEvents`: allows sleeping in Nether-like dimensions when an active conduit is within 16 blocks.
- `LivingEntityLavaMovementMixin`: reduces lava movement drag/acceleration penalties while `NetherConduitPowerTracker` is active.
- `ModDamageTypes` plus `data/ruined_portal_overhaul/damage_type/nether_conduit.json`: custom damage source for conduit attacks.
- `NetherConduitChestPlacement`: deterministic exactly-one-per-structure chest insertion helper.

Activation and levels:

- Active frame requires all 12 regular `minecraft:nether_bricks` blocks in the flat horizontal ring two blocks out from the conduit; random 3D nether-brick rubble no longer powers the block.
- `NetherConduitBlock.ACTIVE` is mirrored into blockstate whenever the frame scan changes so GeckoLib can swap between idle and active spin loops from synced state instead of stale client memory.
- The conduit uses vanilla obsidian for the main placeholder model, vanilla netherrack for the emissive inner-core overlay, grey inactive tint, and warmer active tint by upgrade level. `RuinedPortalGeoRenderData.CONDUIT_ACTIVE` and `CONDUIT_LEVEL` bridge block-entity state into the GeckoLib render layer.
- Level 0: Fire Resistance I, Haste I, Regeneration I, 8-block support/attack radius, 4 conduit damage.
- Level 1: Fire Resistance II, Haste II, Regeneration II, 12-block support/attack radius, 6 conduit damage.
- Level 2: Fire Resistance II, Haste II, Regeneration II, 16-block support/attack radius, 8 conduit damage, near-zero lava movement penalty.
- Upgrades consume ancient debris: 1 for level 0 to 1, 2 for level 1 to 2.
- Effects refresh every 20 ticks with 40-tick duration. Mob attacks scan every 30 ticks and emit `ParticleTypes.ELECTRIC_SPARK`.
- Attack targets include Zombie Piglin, Piglin, Piglin Brute, Blaze, Wither Skeleton, Ghast, Hoglin, Magma Cube, and all seven custom combat mobs.

Acquisition:

- Exactly one Nether Conduit is inserted directly into a generated deep structure chest during structure generation.
- Each of the seven custom raid mobs has a 2% killed-by-player Nether Conduit drop pool.
- There is no crafting recipe for the Nether Conduit.

## Ghast Tear Necklace

The Ghast Tear Necklace is a native carried charm:

- `ModItems`: registers `ghast_tear_necklace`, stack size 1, fire resistant.
- `GhastTearNecklaceItem`: extends `Item`, exposes carried-stack lookup helpers, and applies Speed II plus Jump Boost II.
- `GhastTearNecklaceEvents`: server tick hook that applies passive effects while carried and triggers the first-carry advancement.
- `ModDataComponents`: registers `last_necklace_fireball_tick` as a persistent/networked long component on the carried stack.
- `NetherFireballPayload`: C2S keybind payload carrying the client look vector sampled at keypress time.
- `ModPackets`: central common packet hub. It registers typed 1.21.11 play payloads and wraps the fireball C2S receiver back onto the server executor before gameplay state changes.
- `NetherFireballHandler`: server-side carried-stack lookup, alive/spectator validation, cooldown check, finite look-vector validation, server-look alignment fallback, fireball spawn, sound, stack component update, and advancement trigger. Invalid or strongly misaligned C2S requests are silently dropped back to the server-authoritative look direction.
- `NetherFireballKeybinds`: client keybind registration, default key `G`, sends the payload only when the server supports it.
- `RuinedPortalOverhaulClient`: registers the keybind from the client initializer.

Passive effects refresh every 40 ticks with 80-tick duration while the necklace is carried. The fireball ability has a 2400-tick cooldown, spawns a vanilla `SmallFireball` owned by the player from the current server-side look direction, persists cooldown state on the carried stack component, and routes audio through `ModSounds.ITEM_GHAST_TEAR_NECKLACE_FIREBALL`.

## Nether Crystal And Nether Dragon

The Nether Crystal ritual is the endgame loop:

- `NetherCrystalEntity`, `NetherCrystalGeoRenderer`, and `NetherCrystalGeoModel`: GeckoLib-powered ritual crystal using the vanilla end-crystal texture, a dark red render tint, and a looping pulse/rotation animation.
- `NetherCrystalItem`: places crystals only on `minecraft:netherite_block` or `minecraft:obsidian`, spawns `NetherCrystalEntity`, consumes the stack only from the server side while respecting creative mode, and calls the ritual tracker.
- `ModEntities`: registers `nether_crystal` and `nether_dragon`.
- `PortalStructureHelper`: places four netherite pedestals at offsets north/south/east/west six blocks from the portal center and exposes `ritualPedestalPositions(...)`.
- `PortalRaidState`: persists discovered portal variants, filled ritual pedestals, and active dragon portals.
- `NetherDragonRituals`: tracks crystal placement, starts the summoning sequence, keeps ritual titles, advancement triggers, and the Nether Dragon boss bar aligned to the same 96-block horizontal portal fight radius, shatters pedestals at the opening death beat, drops death rewards on the later reward beat, and clears ritual state once rewards are handled.
- `NetherDragonEntity`, `NetherDragonGeoRenderer`, and `NetherDragonGeoModel`: extend vanilla `EnderDragon` for combat semantics, suppress End fight hooks and crystal healing, purge nearby vanilla `minecraft:end_crystal` entities every second while preserving this mod's Nether Crystal ritual entities, set 300 HP, delegate the staggered death finale to `NetherDragonRituals`, apply permanent movement and flying-speed boosts during phase two, send the phase-two flash to players by horizontal portal distance, and render through GeckoLib with a vanilla dragon texture, render-state crimson/enraged tint, flight loop, phase-two transition trigger, and Nether Slam trigger.
- Phase two starts at 150 HP. Nether Slam uses a visual-only non-griefing explosion and then applies the intended 15 damage once through the explicit six-block radius hit, so players are not double-hit by vanilla explosion damage plus scripted damage.

Ritual conditions:

- The portal must be completed/lit.
- A Nether Crystal must be placed on top of each generated pedestal.
- No dragon may already be active for that portal.
- If the four crystals were staged before raid completion, the completion handoff now backfills ritual progress from the loaded pedestals immediately instead of waiting for an extra crystal placement after the raid.
- If the Nether Dragon config toggle is disabled, completed ritual pedestals reject new crystal placements and show action-bar feedback instead of consuming the offering for a boss that cannot spawn.

Summoning sequence:

- Tick 0: Wither spawn sound plus flame and large smoke sphere around the portal.
- Tick 40: Nearby title `The Nether Dragon Awakens`, subtitle `Flee or fight.`
- Tick 80: Nether Dragon spawns at portal center plus `(0, 10, 0)` and plays the dragon growl.
- Ritual crystal state must be reconciled from the actual loaded pedestal crystals. Breaking a crystal before completion cancels pending summoning, and restart recovery may only resume the summoning pulse when all four crystals are still physically present. Persistent `activeDragonPortals` means a live dragon, not merely a started summoning animation.

Death behavior:

- The death finale is staggered: around death tick 60 it removes the four Nether Crystals, shatters the four netherite pedestals, and plays the ritual victory sting; around tick 90 it drops 2 Nether Stars, 1-3 Ancient Debris, dragon rewards, and nearby advancement credit; around tick 120 it removes the dragon entity.
- The dragon also drops 1-2 Corrupted Netherite Ingots, has a 30% Nether Dragon Scale roll, and performs the world's one Nether Tide disc roll at 15% through `PortalRaidState.nether_tide_disc_rolled`.
- Awards a 1500 XP burst from `NetherDragonEntity.tickDeath()` plus challenge advancement XP for the ritual and dragon milestones.
- Does not spawn an End portal and does not use End-crystal healing.
- The Nether Dragon boss bar is rebuilt from players horizontally within 96 blocks of the portal each tick, so pit and cave players keep the bar while walking away or changing dimensions still removes the viewer cleanly.

## Recipes

Recipes live in singular `data/ruined_portal_overhaul/recipe/`.

- `ghast_tear_necklace.json`: shaped `TST/G G/TST`, where `T = minecraft:ghast_tear`, `S = minecraft:nether_star`, and `G = minecraft:gold_ingot`. This exact grid uses four ghast tears, two nether stars, and two gold ingots.
- `nether_crystal.json`: shaped `CSC/SNS/ISI`, where `C = minecraft:crying_obsidian`, `S = minecraft:nether_star`, `N = ruined_portal_overhaul:nether_conduit`, and `I = minecraft:netherite_ingot`.
- `corrupted_netherite_helmet/chestplate/leggings/boots.json`: smithing transform recipes. Template is `ruined_portal_overhaul:corrupted_netherite_ingot`, base is the matching vanilla netherite armor piece, addition is `minecraft:echo_shard`, and result is the matching Corrupted Netherite piece.

## Masterwork Rewards And Discovery

- `ModItems` registers `corrupted_netherite_ingot`, four Corrupted Netherite armor pieces, `portal_shard`, `nether_dragon_scale`, and `music_disc_nether_tide`.
- `CorruptedNetheriteIngotItem` carries `DataComponents.CUSTOM_DATA` key `ruined_portal_overhaul:dragon_infused` and tooltips the smithing path.
- Corrupted Netherite armor uses vanilla netherite item/armor components plus both `ModDataComponents.CORRUPTED_NETHERITE` and `DataComponents.CUSTOM_DATA` key `ruined_portal_overhaul:corrupted`, so set detection is data-driven rather than tied to class identity alone.
- `CorruptedNetheriteEvents` checks armor every 20 ticks. Two pieces grant Fire Resistance, three pieces add Resistance I, and four pieces add a transient +4 armor toughness modifier plus `nether_ember` shimmer particles.
- `PortalShardItem` stores a 600-tick cooldown in `ModDataComponents.LAST_PORTAL_SHARD_USE_TICK`, searches `StructureTags.RUINED_PORTAL` server-side for the nearest uncompleted portal candidate within 10,000 blocks, shows an action-bar bearing/distance, and emits `corruption_rune` guide particles.
- `music_disc_nether_tide` uses `NetherTideDiscItem`, the `ruined_portal_overhaul:nether_tide` jukebox song JSON, and `ModSounds.MUSIC_DISC_NETHER_TIDE`, mapped to vanilla `music_disc.13` as placeholder audio. Piglin Evokers have a separate 5% runtime drop chance. Jukeboxes playing the disc within 64 blocks of a completed portal emit `nether_ember` particles.
- `Nether Dragon Scale` is intentionally a trophy item in this Lunar-compatible branch, not an Accessories back-slot renderer, until a verified Accessories release exists for 1.21.11.

## Custom Particles

- `ModParticles` registers `nether_ember`, `corruption_rune`, and `dragon_blood` with `FabricParticleTypes.simple()`.
- Client factories live in `client/particle/` and are registered from `RuinedPortalOverhaulClient`.
- Particle sprite JSON lives under `assets/ruined_portal_overhaul/particles/`; `nether_ember.png` and `corruption_rune.png` are generated 8x8 placeholder sprites under `textures/particle/`.
- Current uses: portal frame ambience, raid start cues, armor set shimmer, Ravager roar cues, dragon phase/slam accents, and Nether Tide jukebox effects use `nether_ember`; Portal Shard trails, Evoker casting cues, and ritual pedestal shatter use `corruption_rune`; `dragon_blood` emits when the Nether Dragon takes damage during or entering phase two.

## Patchouli Guide Data

- Patchouli remains optional and is listed under `suggests`, not `depends`.
- The data-driven registration file lives at `data/ruined_portal_overhaul/patchouli_books/corrupted_chronicle/book.json` and sets `use_resource_pack: true`.
- English categories/entries live under `assets/ruined_portal_overhaul/patchouli_books/corrupted_chronicle/en_us/`, matching Patchouli's resource-pack loading path.
- Dragon-category entries are locked with the entry-level `advancement` field against `ruined_portal_overhaul:the_final_offering`; do not use `flag` for advancement gates.
- `ModLootEvents` adds a Patchouli guide stack with `patchouli:book = ruined_portal_overhaul:corrupted_chronicle` to surface and boss chest drops only when Patchouli is actually loaded and a guide item exists. It checks `patchouli:guide_book` first and falls back to `patchouli:book`, keeping the optional integration tolerant of Patchouli item id drift without making Patchouli a hard dependency.

## Structure Generation

`PortalDungeonStructure` creates one `PortalDungeonPiece` at the chunk center. `PortalDungeonPiece` owns generation order, saves a locked `PortalDungeonVariant`, and delegates block placement to `PortalStructureHelper`.

Implemented structure variants:

- `Crimson Throne`: baseline original scar layout.
- `Sunken Sanctum`: lowers the ritual core four blocks into a bowl, pushes more soul sand and soul soil into the middle zone, and adds a collapsed blackstone arch on the north rim.
- `Basalt Citadel`: swaps the inner zone to blackstone, widens the ritual platform to a 7-block radius, adds four basalt corner columns around the frame, and places a mid-depth pit lava moat.

Variant selection is deterministic from the structure chunk through `PortalDungeonVariant.selectForChunk(...)`. `GoldRaidManager` caches discovered variants into `PortalRaidState` the first time a portal piece is seen so later raid and ritual hooks can query a portal's form without mutating persistent state during chunk generation.

The generated piece uses a radius-136 surface footprint and a depth-45 underground rupture:

- Inner zone, radius `0-15`: stable ritual core with netherrack ground, blackstone brick platform, valid 4x5 or 6x7 portal frame, chains, and Exiled Piglin anchor. The ritual platform and frame stay at a consistent readable height.
- Middle zone, radius `15-52`: netherrack-dominant Nether scar with readable cardinal material sectors: soul sand/soil to the north, netherrack to the south, blackstone to the east, and crimson nylium patches to the west. The ground uses deterministic low-frequency height variation outside the ritual core, clamped to roughly `-3` to `+3` blocks, so the surface reads as a mostly flat Nether plain with organic undulation rather than jagged stacked terrain.
- Outer zone, radius `52-136`: lower-density netherrack corruption scatter with rare crying obsidian flecks using the same calm surface sculpting, so outer patches inherit gentle rises and depressions.
- Underground pit: protected original-style ragged mouth, organic shaft, 12 lower lava seeps, mixed basalt/blackstone/netherrack/soul-soil rim rubble, and Nether material conversion around carved space.
- Primary chamber: large blackstone/basalt/netherrack cavern with lava lake, vents, glowstone clusters, stalactites, and basalt/blackstone spikes.
- Cave network: denser graph-based cave nodes connected by worm-carved organic tunnels. Each tunnel advances by a gradually blended noisy direction, varies radius from roughly 2-6 blocks with an independent smooth noise signal, drifts vertically, and creates side pockets. Deep nodes are larger and taller, including ghast-ready caverns with high ceilings, more blackstone/basalt material, lava runs, ceiling drips, glowstone pockets, and frequent underground cache pads.
- Masterwork room templates: after cave graph connection, `PortalStructureHelper` places a guaranteed 7x7x5 Wither Shrine at the deepest node with soul terrain, a skull totem, deep loot, and four Wither Skeleton spawners. Selected branch nodes add Gold Vault, Blaze Chamber, or hidden Ancient Vault rooms with designed floors, false walls, embedded ore/debris, deep chest positions, and room-specific spawners.
- Altar and caches: blackstone brick altar with crying obsidian focus, two altar `portal_deep` chests, plus many cave-node `portal_deep` caches distributed through shallow, middle, and deep branches.
- Ritual pedestals: four `minecraft:netherite_block` pedestals are placed at ground level exactly 6 blocks north, south, east, and west of the portal center. These are generated by the structure and are the only canonical Nether Dragon ritual pedestals.
- Nether Conduit guarantee: after structure chest placement, `NetherConduitChestPlacement` deterministically picks one generated deep chest and inserts exactly one Nether Conduit directly into that chest inventory/NBT. This is not loot-table driven and never waits for the post-raid boss chest.

Pit, chamber, and tunnel carvers replace adjacent overworld stone, deepslate, dirt, and common overworld ore/geology blocks with Nether materials so natural cave intersections read as corrupted Nether geology. Structure-local water encountered in transformed terrain is converted into lava with a bounded 8-block vertical clear. All writes are bounded by both the structure piece box and current chunk box.

`StructureBlockPalette` centralizes YUNG-style weighted block variation for nether walls, nether bricks, deep walls, and ritual floors. New cache pads, ritual floors, altar blocks, room shells, and selected cave wall fallbacks use the palette so repeated rooms read as aged material instead of single-block fills.

Overworld ruined portal structure JSON files are overridden to use `ruined_portal_overhaul:portal_dungeon`. Their biome predicate now points at `#ruined_portal_overhaul:portal_dungeon_overworld`, a project tag backed by Fabric's conventional `#c:is_overworld` biome tag, so TerraBlender biome packs are included through convention tags instead of hardcoded vanilla ruined-portal tags. The matching `data/minecraft/worldgen/structure_set/ruined_portals.json` override now contains only the six overworld ruined portal variants, uses a minimum spacing grid of `16`, separation `8`, and a four-chunk exclusion zone against `minecraft:villages`; `PortalDungeonStructure.findGenerationPoint(...)` then deterministically thins candidate chunks from the live `ModConfigManager.structureRarity()` value so pack authors can tune average rarity without a custom datapack.

Vanilla Nether ruined portals are preserved through `data/minecraft/worldgen/structure_set/ruined_portals_nether.json`, which keeps `minecraft:ruined_portal_nether` on Mojang's original `spacing = 40`, `separation = 15`, and `salt = 34222645` placement. This avoids accidentally applying the overworld dungeon spacing/exclusion tuning to Nether generation while still letting `/locate structure minecraft:ruined_portal_nether` behave like vanilla.

## Entity Presentation

GeckoLib now renders all combat mobs, the Exiled Piglin, the Nether Crystal, the Nether Conduit block entity, and the Nether Dragon. Assets use the GeckoLib 5 layout: models live under `assets/ruined_portal_overhaul/geckolib/models/`, animations live under `assets/ruined_portal_overhaul/geckolib/animations/`, and the GeoModel resource IDs use the bare `entity/...` or `block/...` subpath without the directory prefix or file suffix. The Nether Conduit swaps slow/fast inner-core spin loops from synced blockstate, returns `RenderShape.INVISIBLE` so the registered block entity renderer owns the visual in the current Mojang mappings, and adds an active-only emissive inner-core render layer. The Dragon no longer uses the vanilla `EnderDragonRenderer`; its placeholder model lives at `assets/ruined_portal_overhaul/geckolib/models/entity/nether_dragon.geo.json`, and its flight, phase-two, and slam animations live at `assets/ruined_portal_overhaul/geckolib/animations/entity/nether_dragon.animation.json`. Piglin Pillager melee fallback, Piglin Vindicator, Brute Pillager melee fallback, Piglin Vex, and Piglin Ravager successful melee hits now trigger their GeckoLib attack clips, while Piglin Ravager Hard wall-impact roars trigger the matching `action.roar` animation in addition to the custom roar sound and Slowness II pulse. In 1.21.11, vanilla swing duration is read from the held item's `DataComponents.SWING_ANIMATION` value instead of an overridable entity method, so the custom pillager, vindicator, and brute melee loadouts stamp a 10-tick whack swing to match the 0.5-second humanoid attack clip.

Visual variant support is active for:

- `PiglinPillagerEntity`: 3 synced texture variants
- `PiglinVindicatorEntity`: 3 synced texture variants
- `PiglinBrutePillagerEntity`: 3 synced texture variants
- `PiglinIllusionerEntity`: 2 synced texture variants
- `PiglinEvokerEntity`: 2 synced texture variants

Implementation notes:

- Each eligible mob uses synced entity data plus save data under `Variant`, so the chosen texture survives saves and renders consistently on clients.
- Variant selection is deterministic from the entity UUID through `TextureVariantHelper`.
- GeckoLib texture selection is driven through `RuinedPortalGeoRenderData.TEXTURE_VARIANT`, captured during model state compilation.
- Nether Conduit glow state is driven through `RuinedPortalGeoRenderData.CONDUIT_ACTIVE` and `CONDUIT_LEVEL`, captured during block-entity render compilation.
- Nether Conduit, Nether Crystal, and Nether Dragon tint values are written to `DataTickets.RENDER_COLOR` during GeckoLib render-state compilation instead of consulting the entity again during the later render pass.
- Piglin Vex uses `move.fly` while moving and `misc.idle.flying` while hovering; the shared `flyIdleController()` is intentionally custom because GeckoLib's stock helper falls back to `misc.idle`.
- Placeholder `_0/_1/_2` PNGs are generated derivatives of the base texture sheets and are safe for later artist replacement.

## Spawners And Spawn Pressure

Structure generation places deterministic pre-raid spawners, then `GoldRaidManager` scans/persists those positions when a portal is approached and deletes them when the raid starts. Spawners are not re-enabled after the raid starts or completes. On raid completion, known structure spawners are permanently disabled again before the portal is marked complete.

Spawner pressure now escalates by depth:

- Surface spawners: zombified piglins, magma cubes, piglin pillagers, piglin vindicators, and one blaze slot across a ring around the portal.
- Primary chamber spawners: magma cube, blaze, wither skeleton, piglin brute pillager, piglin pillager, and piglin vindicator positions.
- Tunnel and cave spawners: up to 28 positions from node centers, tunnel midpoints, and branch endpoints. Upper tunnels use magma cubes, pillagers, vindicators, blazes, and wither skeletons. Lower tunnels add brutes and illusioners. Deep cave spawners add ghasts, evokers, more brutes, and stronger blaze/wither pressure.
- Ghast spawners use low spawn count, larger spawn range, larger player range, and deep cavern placement so ghasts have space to move and fire.

Runtime structure-local ambient spawning is also owned by `GoldRaidManager`, not `BiomeModifications`:

- `ModWorldGen` adds only the underground corruption features to compatible overworld biomes. It no longer injects blaze or zombified piglin natural-spawn tables into global biome data.
- Ground ambient cap: `180` tagged mobs per portal footprint.
- Ambient tick interval: every `10` server ticks.
- The live `enableAmbientNetherSpawns` config gates the custom portal ambient loop directly, so pack admins can fully disable the extra Nether pressure without touching biome data.
- Runtime portal-origin scans must only inspect chunks that are already loaded. Do not let proximity, atmosphere, or ambient-spawn scans force-load structure chunks; Save and Exit must not inherit pending chunk discovery work.
- Wave spawn heightmap lookups and fallback portal-frame scans must also check loaded chunks before reading terrain or block states.
- Server-stop cleanup clears runtime-only raid, ambient-spawn, ghast-anchor, summoning, and boss-bar maps. Persistent progress belongs only in `PortalRaidState`.
- Burst size: `8-12` on the surface, `10-14` in lower caves, and `12-16` in deepest caves.
- Outer surface pool: zombified piglins, piglin pillagers, piglin vindicators, magma cubes, rare blazes.
- Middle pool: piglin pillagers, vindicators, brutes, blazes, wither skeletons, magma cubes, zombified piglins.
- Inner pool: blazes, wither skeletons, brutes, vindicators, magma cubes, illusioners, pillagers.
- Lower underground pool: blazes, wither skeletons, magma cubes, brutes, illusioners, vindicators, pillagers.
- Deep underground pool: wither skeletons, blazes, magma cubes, brutes, illusioners, evokers, vindicators.
- Anchored ghasts: cap `16`, spawn every `25-55` ticks when available, use a clear-volume check, are tagged to their portal origin, and are discarded if they drift beyond the anchor radius or timeout.

Completed portal suppression:

- `GoldRaidManager.initialize()` registers a server entity-load hook that discards hostile mobs loaded inside completed portal footprints.
- `NaturalSpawnerSuppressionMixin` wraps `NaturalSpawner.SpawnPredicate#test(...)` so completed portal territories reject natural spawn attempts before a mob entity is created. This is the authoritative post-raid suppression path; the entity-load hook remains a fallback for already-loaded or externally-created mobs.
- `ModNaturalSpawnGuards` owns the shared suppression rules. It checks only the overworld, uses horizontal squared distance against `PortalStructureHelper.OUTER_RADIUS`, and honors `ModConfigManager.enablePostRaidSuppression()` at runtime.
- Suppression intentionally skips the Exiled Piglin trader and mobs requiring persistence so reward/trader behavior is not broken.
- Runtime ambient portal spawning already ignores completed portals, so completed structures stay quiet.

## Raid Trigger And Flow

The manager class is still named `GoldRaidManager` for compatibility with the existing codebase, but the raid trigger is no longer gold armor and no Bad Omen is involved.

`GoldRaidManager` runs from `ServerTickEvents.END_SERVER_TICK`, only in the overworld, and scans players every `10` server ticks.

Recurring raid, boss-bar, ambient, spawn-suppression, and ghast range gates use horizontal squared-distance checks; code only takes square roots when a real distance is needed for intensity curves or spawn band selection. Ambient ground/underground spawn retries and anchored-ghast cap/cleanup checks must stay squared because those loops can run many candidates per portal tick.

Trigger phases:

- Approach zone: any player horizontally within `136` blocks of an uncompleted generated portal activates the portal once. The player receives `...something stirs.` and the portal plays a low portal ambient sound.
- Raid trigger: any player horizontally within the configured raid trigger radius starts the raid. The built-in default is `24` blocks, clamped to `12-48`, which requires entering the ritual area without springing the raid from the far outer scar.
- Distance checks for approach, atmosphere, boss bars, completion feedback, and raid trigger use horizontal X/Z distance where portal-zone membership matters, so the storm and raid work in the pit and cave system below the frame.

Raid start:

- Uses `PortalRaidState.beginRaid()` as the active flag check-and-set.
- Plays raid start sounds and particles.
- Sends title `The Red Storm Breaks` and subtitle `Survive the waves...` to nearby players.
- Deletes known/scanned pre-raid spawners.
- Creates a `ServerBossEvent` and spawns wave 1 in an adaptive 14-24 block ring using `Heightmap.Types.MOTION_BLOCKING`.
- Runtime structure discovery also remembers the portal's `PortalDungeonVariant` in `PortalRaidState`.

Current wave table:

| Wave | Boss Bar Label | Composition |
|---|---|---|
| 1 | `The Red Storm Breaks` | 12x PiglinPillager, 8x PiglinVindicator |
| 2 | `They Grow Bolder` | 14x PiglinPillager, 8x PiglinVindicator, 5x PiglinBrutePillager |
| 3 | `The Brutes Arrive` | 12x PiglinPillager, 9x PiglinVindicator, 8x PiglinBrutePillager, 5x PiglinIllusioner |
| 4 | `Chaos Unleashed` | 6x PiglinPillager, 8x PiglinBrutePillager, 6x PiglinIllusioner, 8x PiglinVindicator, 1x PiglinRavager with mounted PiglinVindicator, 1x PiglinEvoker |
| 5 | `The Evoker Awakens` | 10x PiglinPillager, 9x PiglinVindicator, 8x PiglinBrutePillager, 5x PiglinIllusioner, 2x PiglinRavager, 3x PiglinEvoker |

Completion order:

1. Remove all boss-bar players and hide the bar.
2. Disable any remaining known/scanned pre-raid spawner blocks without re-adding spawner positions to persistent state.
3. Play completion fanfare and completion title immediately.
4. Tick 20: ignite the portal with portal particles and sound.
5. Tick 40: spawn the boss reward chest with a separate reward burst.
6. Tick 60: spawn the Exiled Piglin trader leashed to the nether-brick fence anchor.
7. Mark the portal completed in persistent state and remember the trader spawn time.
8. Grant players who are still within the 48-block boss-bar radius the raid-complete custom advancement trigger and action-bar feedback: `The tribute is over. The scar remains.`
9. Reconcile any already-placed ritual crystals on the four pedestals so a pre-built offering can flow straight into the Nether Dragon summoning sequence.

## Persistence And Multiplayer

- Completed portals, active raids, approach activations, discovered structure variants, ritual crystal fills, active dragon portals, known pre-raid spawner positions, and Exiled Piglin trader spawn game times are tracked per portal `BlockPos`.
- `PortalRaidState.CODEC` uses save-compatible optional defaults for newer fields.
- Active wave mobs are stored as UUIDs, never direct entity references.
- Active raids rehydrate from persistent state after server restart.
- A saved `current_wave_number` of `0` is treated as the pre-wave-1 sentinel during restore, so a crash after raid activation but before the first wave mob UUIDs are written resumes at wave 1 instead of skipping ahead.
- Restored raids preserve the configured inter-wave delay whenever saved wave UUIDs exist, even if those entities are not loaded by the time the runtime manager rebuilds its in-memory view, so restarting between waves cannot instantly dump the next wave on returning players.
- Active raids pause mob-death evaluation while the portal area is not entity-ticking, so unloaded mobs are not counted as dead.
- Boss bars track all players horizontally within 48 blocks of the active portal and remove players who leave range or disconnect. The same disconnect hook releases any loaded Exiled Piglin trader customer lock for the departing player so a mid-trade disconnect cannot leave the reward trader permanently busy.
- Ritual state persists as portal-origin to filled-pedestal sets and only mutates when a placed crystal matches one of that portal's generated pedestal positions. Dragon activity is stored separately so placing replacement crystals cannot start duplicate fights while a dragon is active. Exiled Piglin lifetime still lives on the entity NBT for actual despawn behavior, while `PortalRaidState` now mirrors the spawn game time for portal-owned audit and recovery hooks.
- `ExiledPiglinTraderEntity` also persists its fence-post anchor on the entity itself, reapplies its home radius on load, and recreates the leash knot if chunk or save timing drops the leash holder.
- Older saves that predate `portal_variants` fall back to `PortalDungeonVariant.selectForOrigin(...)`, so variant lookups stay deterministic even before runtime discovery repopulates the saved field.

## Red Storm And Audio

The red storm is a client-side visual/audio system driven by server proximity packets:

- `GoldRaidManager` sends `PortalAtmospherePayload` every `20` ticks while a player is horizontally inside the radius-136 zone, including after raid completion so the claimed portal remains visibly corrupted.
- Packet intensity uses horizontal distance and never falls below `0.22` while in-zone.
- Packet descent uses how far the player is below the portal frame, making pit and cave atmosphere tighter and more intense.
- `PortalAtmosphereClient` eases target intensity/descent, fades when packets stop, applies a 2.8-second breathing pulse, and renders the HUD tint. Current tint strength is about 15-20% more pronounced than the earlier storm pass.
- `ClientLevelStormMixin` makes the local client report rain/thunder gradients during the storm and now ignores integrated-server `Level` instances so fake weather stays client-visual only.
- `WeatherEffectRendererMixin` forces red rain visuals.
- `SkyRendererMixin` tints the sky toward a dark red storm color and dims rain brightness.
- `FogRendererMixin` tints fog red and tightens fog distance, especially underground.
- Red thunder is generated on a client-side storm timer that is roughly twice as frequent as the earlier storm pass. Thunder uses a 2-3 tick deep-red HUD flash and now routes all layered thunder accents through `ModSounds`, so packs can replace the storm stack without touching logic. It does not rely on real world weather.
- Storm music starts when storm intensity rises through the custom `weather.red_storm.music` event and is stopped when the player leaves the zone, the portal is completed, or the client world/player unloads. Completed portal packets carry a `completed` flag so the red weather remains while combat music and territory boon effects stop.
- A separate `weather.red_storm.rumble` tickable client sound loops while storm intensity is active, follows the player, scales volume and pitch from storm intensity/pulse/descent, and settles to a lower volume for completed portals so the claimed scar still feels corrupted without combat music.
- Custom mob voices use mod-owned sound ids plus explicit volume and pitch overrides, avoiding inherited pillager/illager identity audio while keeping resource-pack replacement simple.
- `assets/ruined_portal_overhaul/sounds.json` maps every custom sound id to vanilla fallback sound events with `"type": "event"`, so the mod has audible defaults without bundling `.ogg` files and resource packs can still replace each id cleanly.

Server-side atmosphere remains active too: ash, crimson spores, smoke, lava drips, frame particles, lava ambience, raid start bursts, inter-wave pulses, completion particles, mob spawn sounds, ritual breaks, dragon victory cues, and necklace fireball launches are all routed through server-side effects and mod-owned sound ids.

## Territory Boon

Incomplete portal territories now give players a protective red-aether boon while they remain horizontally inside the radius-136 zone:

- Regeneration II, Resistance I, Fire Resistance I, and Absorption IV are reapplied every 20 server ticks with a 260-tick duration, matching the functional protection profile of an enchanted golden apple for as long as the player stays in the territory.
- The effect is tied to the same horizontal portal-zone logic as the atmosphere, so it applies on the surface, in the pit, and throughout the caves below the portal.
- The first time each player enters a specific uncompleted portal territory, the portal grants one Totem of Undying. This is protected by the per-player entity tag `rpo_totem_granted_<portalOriginLong>` so each portal gives each player one starter totem, not an infinite farm.
- The boon fires the `aether_boon` custom advancement trigger, and the starter totem fires the `territory_totem` custom advancement trigger.

## Mob Roster

| Entity ID | Class | Base | Behavior |
|---|---|---|---|
| `piglin_pillager` | `PiglinPillagerEntity` | Pillager | Fire immune, Quick Charge III crossbow with Piercing II or Multishot variation, crossbow loadouts kite away when players get inside 7 blocks, melee fallback runs only for sword/axe loadouts, 44 HP, 9.5 arrow damage |
| `piglin_vindicator` | `PiglinVindicatorEntity` | Vindicator | Fire immune, golden axe/sword, guaranteed Sharpness III-V, chance of Fire Aspect and Knockback, successful melee hits trigger `attack.swing`, 58 HP, 16.5 attack |
| `piglin_brute_pillager` | `PiglinBrutePillagerEntity` | Pillager | Fire immune, mostly melee golden axe/sword loadouts with Sharpness IV-V, rare Multishot + Quick Charge II crossbow variant, priority-2 close-range melee fallback for crossbow brutes so swimming/float movement remains unblocked, successful melee hits trigger `attack.swing`, 88 HP, 20 attack, 11 arrow damage |
| `piglin_illusioner` | `PiglinIllusionerEntity` | Illusioner | Fire immune, Flame + Power III bow with Punch I variation, 54 HP, 8 arrow damage, and Nether combat sounds for arrows |
| `piglin_evoker` | `PiglinEvokerEntity` | Evoker | Fire immune, 70 HP, 10-fang Magma Eruption spawned through `EntityType.EVOKER_FANGS` with blaze/lava cue every 160 ticks, ignites targets, summons 4 Piglin Vexes every 220 ticks, summons 2 desperation Piglin Vexes below 50% HP with NBT persistence |
| `piglin_ravager` | `PiglinRavagerEntity` | Ravager | Fire immune, 210 HP, 24 attack, half projectile damage, Hoglin sound set, Slowness roar, wave-4 rider owned by `GoldRaidManager`, successful melee hits trigger `attack.slam`, Hard wall/obsidian charge roar applying Slowness II for 60 ticks and triggering `action.roar` |
| `piglin_vex` | `PiglinVexEntity` | Vex | Fire immune, 28 HP, 10 attack, successful strikes trigger `attack.flying_attack`, limited life of 1200 ticks, summoned by Piglin Evoker |
| `exiled_piglin` | `ExiledPiglinTraderEntity` | Wandering Trader | Invulnerable reward trader with Piglin sounds, one customer at a time, action-bar messages, restock every 40000 ticks, despawn after 72000 world ticks |

All seven combat mobs call `PiglinDifficultyScaler.applySpawnScaling(...)` from their 1.21.11 spawn initialization hook. The scaler reads `ModConfigManager` at spawn time and composes config multipliers with world difficulty: Easy/Peaceful uses 0.75x health and damage, Normal uses 1.0x, and Hard uses 1.25x health plus 1.5x damage when the attack attribute exists.

## Loot Tables
Loot table files contain `_comment` fields documenting reward intent.

- `chests/portal_surface`: `9-12` rolls. Surface generation now places six prep chests around the scar. Loot includes more gold/iron/obsidian, Fire Resistance, Strength II, Regeneration II, healing splash potions, golden apples/carrots, rare enchanted golden apples, emergency totems, enchanted gold gear, Fire Protection IV books, diamonds, and rare netherite scrap. Golden apple weight is increased to 7.
- `chests/portal_deep`: `12-16` rolls. Underground generation now adds frequent cave caches beyond the two altar chests. Loot includes larger stacks of netherite scraps, ancient debris, upgrade templates, rare netherite ingots, diamonds, enchanted diamond gear, totems, enchanted golden apples, Mending, Efficiency V, Sharpness V, Protection IV, Thorns III, Sharpness III diamond swords, a rare Nether Star weight, other strong books, and survival potions.
- `chests/portal_boss_reward`: `16-20` rolls plus a two-roll high-weight totem/enchanted-golden-apple bonus pool, a guaranteed Nether Star pool, and a guaranteed `Portal Shard`. Includes 1-2 netherite ingots, larger netherite scrap and ancient debris stacks, multiple upgrade templates, enchanted golden apples, common totems, diamonds, gold blocks, high-tier books, enchanted diamond gear, and a weighted named `Corrupted Portal Key`.
- Entity loot tables are under `data/ruined_portal_overhaul/loot_table/entities/`. All custom combat mob drops are now richer, with more gold, food, ammunition, gear, potion ingredients, books, totems from high-threat mobs, and rare netherite/debris drops from deeper wave threats.
- Named reward artifacts now use translated `set_name` plus `set_lore` loot functions with `mode: "replace_all"` instead of raw `minecraft:custom_name` strings, so the boss key, Nether shard, ravager hide, embered grimoire, and voidash powder all localize cleanly and carry multi-line atmospheric lore.
- Enchantment loot functions use namespaced `minecraft:set_enchantments` with explicit `"add": false`, matching vanilla 1.21.11 data shape instead of relying on parser defaults.
- Nether Star entity drops: `ModLootEvents` adds runtime-configured killed-by-player Nether Star rolls after entity loot generation. Base odds remain Piglin Evoker 5%, Piglin Ravager 3%, and Piglin Illusioner 1%, multiplied by `ModConfigManager.netherStarDropRate()` so ModMenu/Cloth changes can reduce or boost the economy without editing data packs.
- Nether Conduit entity drops: all seven custom combat mobs have a 2% killed-by-player chance.
- Nether Tide entity drops: Piglin Evokers have a separate killed-by-player 5% runtime roll for `music_disc_nether_tide`.
- Optional Patchouli guide reward: when Patchouli is loaded, surface and boss reward tables receive the Corrupted Chronicle guide book from `ModLootEvents` without making Patchouli a hard dependency.

## Advancements

Advancements live under `data/ruined_portal_overhaul/advancement/`. Vanilla criteria cover inventory and kill events; custom player-event triggers live in `com.ruinedportaloverhaul.advancement`.

All advancement display JSON now points at `advancement.ruined_portal_overhaul.*` translation keys in `en_us.json` instead of hardcoded English text, so titles and descriptions stay consistent with the canonical names listed below and remain localization-ready.

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
| `Draped in Sorrow` | `draped_in_sorrow.json` | Custom `ghast_tear_necklace_equipped`, fired from `GhastTearNecklaceEvents.tick(...)` the first time a player carries the necklace |
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

The trader is meant to read as a captive post-raid prize rather than a free-roaming villager clone. Raid completion now spawns it already chained to the ritual fence post, and reload recovery restores that leash if the knot entity is missing while the anchor block still exists.

## Global Worldgen

`ModWorldGen.initialize()` injects only these low-density overworld lore features:

- `ruined_portal_overhaul:underground_netherrack_blob`
- `ruined_portal_overhaul:underground_soul_sand_pocket`
- `ruined_portal_overhaul:underground_blackstone_vein`

Global biome modifications no longer add blaze or zombified piglin spawn tables. Ambient Nether pressure is structure-local and lives in `GoldRaidManager.tickPortalZoneNaturalSpawns(...)`, where `ModConfigManager.enableAmbientNetherSpawns()` can disable the loop at runtime without rebuilding biome data.

`ModNaturalSpawnGuards` is now strictly the post-raid suppression layer. It only blocks natural hostile spawns inside completed overworld portal territories when `ModConfigManager.enablePostRaidSuppression()` is enabled, and it does not participate in pre-completion ambient portal spawning.

Global terrain corruption intentionally excludes Terralith skylands and Terralith cave biomes so the underground lore features stay on grounded overworld terrain. The compat filter checks `#terralith:skylands`, `#terralith:all_skylands`, `#terralith:caves`, `#terralith:all_caves`, and id fallbacks for older/newer Terralith packs. The portal dungeon structure performs the same center-biome compatibility check from `PortalDungeonStructure.findGenerationPoint(...)` after honoring the structure JSON biome predicate, so the full dungeon replacement also avoids floating skylands and underground Terralith cave biomes.

Do not use global biome modifications for structure-local proximity gradients.

Structure rarity note:

- The prompt-level default rarity is 32 chunks. The vanilla ruined portal replacement structure set is data-driven at `data/minecraft/worldgen/structure_set/ruined_portals.json`, now using minimum `spacing` 16 so the code can support the full 16-64 config range.
- `ClothRuntimeConfig` groups the ModMenu/AutoConfig screen into `world_generation`, `raid`, `atmosphere`, `difficulty`, and `rewards` categories, with English category keys in `assets/ruined_portal_overhaul/lang/en_us.json`.
- `ClothRuntimeConfig.validatePostLoad()` normalizes loaded config files into their documented ranges. Runtime getters still clamp values defensively so hand-edited files, reloads, and optional Cloth Config absence all converge on safe gameplay numbers.
- `ModConfigManager.structureRarity()` is read inside `PortalDungeonStructure.findGenerationPoint(...)` for new candidate chunks. Values above 16 deterministically thin the minimum placement grid using the world seed and chunk position; already-generated chunks are not rewritten.
- `ModConfigManager.enableOuterZoneScatter()` is read directly by `PortalStructureHelper.buildOuterScatter(...)`. Turning it off suppresses only the sparse radius-52-to-136 netherrack/crying-obsidian outer scatter while preserving the portal core, middle scar, caves, raid, and ritual layout.

## Metadata

`fabric.mod.json` is release-oriented:

- Version: `1.0.0`
- Author: `Dhruv Sarkar`
- Description now covers the current release scope: Procedural Portal Dungeon, Gold Tribute Raid, Exiled Piglin Trader, GeckoLib animated/variant mobs, Nether Conduit, Ghast Tear Necklace, Nether Crystal ritual, Nether Dragon phase-two boss, Corrupted Netherite rewards, Portal Shard discovery, Nether Tide music, custom particles, optional ModMenu/Cloth Config, optional Patchouli, and optional REI progression info.
- Sources: `https://github.com/dhruvin-sarkar/Ruined-Portal-Overhaul-Mod`
- Issues: `https://github.com/dhruvin-sarkar/Ruined-Portal-Overhaul-Mod/issues`
- Client entrypoint: `com.ruinedportaloverhaul.client.RuinedPortalOverhaulClient`
- Required dependencies: Minecraft, Fabric Loader, Fabric API, and GeckoLib
- Suggested compatibility mods: Cloth Config, Mod Menu, Patchouli, Roughly Enough Items, Terralith, Biomes O' Plenty, and Regions Unexplored
- Optional REI integration is isolated behind the `rei_client` entrypoint and an explicit `FabricLoader.isModLoaded("roughlyenoughitems")` guard. The plugin adds progression information pages for the Conduit, Crystal, Necklace, Nether Star drops, Portal Shard, Corrupted Netherite, and Nether Tide while normal recipes remain data-driven JSON.
- Common mixin config: `ruined_portal_overhaul.mixins.json`
- Client mixin config: `ruined_portal_overhaul.client.mixins.json`
- Minecraft `1.21.11` reports pack versions `resource 75.0` and `data 94.1` in its local `version.json`. This mod intentionally does not commit a single root `pack.mcmeta` because the same mod jar carries both `assets/` and `data/`; one root `pack` section would necessarily advertise either the resource format or the data format incorrectly. Keep this absent unless the project later splits client resources and server data into separate explicit pack roots.

## Implementation Notes

- Use `Identifier.fromNamespaceAndPath(...)` in this Mojang-mapped codebase.
- Use Mojang names from local mappings. Do not paste Yarn-only method or field names into this project.
- Research references checked during the 2026-05-02 pass: GeckoLib 5 RenderStates (`https://wiki.geckolib.com/docs/geckolib5/concepts/rendering/renderstates/`), Fabric networking (`https://docs.fabricmc.net/develop/networking`), Fabric saved data (`https://docs.fabricmc.net/develop/saved-data`), Cloth Config AutoConfig (`https://shedaniel.gitbook.io/cloth-config/auto-config/registering-the-config`), Minecraft loot tables (`https://minecraft.wiki/w/Loot_table`), advancement JSON (`https://minecraft.wiki/w/Advancement/JSON_format`), conduit mechanics (`https://minecraft.wiki/w/Conduit`), Ender Dragon mechanics (`https://minecraft.wiki/w/Ender_Dragon`), pack metadata/version data (`https://minecraft.wiki/w/Pack.mcmeta` plus the local `1.21.11` `version.json`), and Terralith biome/compat context (`https://modrinth.com/mod/terralith` and `https://stardustlabs.miraheze.org/wiki/Terralith`).
- GeckoLib 5 render-pass data that affects texture, glow, or tint should be copied into `GeoRenderState` through data tickets during state compilation; do not add new renderer logic that depends on consulting the live entity during the later render pass.
- Use `level()` / `ServerLevel` guarded server logic.
- Use `Attributes.MAX_HEALTH`, `Attributes.MOVEMENT_SPEED`, and `Attributes.ATTACK_DAMAGE`, not old `GENERIC_*` names.
- Use the 1.21.11 server damage override pattern `hurtServer(ServerLevel, DamageSource, float)`.
- Use `loot_table/`, not `loot_tables/`.
- Do not import `net.minecraft.client.*` outside `com.ruinedportaloverhaul.client`.
- Register common content from `RuinedPortalOverhaul.onInitialize()`.
- Register common play payload types through `ModPackets` before server code sends them.
- Register renderers, client packet receivers, HUD atmosphere, and client mixins only from client-side configuration.
- Do not reintroduce Accessories until a matching `1.21.11` Accessories release has been verified in Lunar Client.
- Necklace behavior is implemented through native carried-item scanning, not external accessory slots.
- Item stack cooldown state for the necklace belongs in `ModDataComponents`, not ad hoc NBT helpers.
- Keep the Nether Conduit lava movement changes in the common mixin config. Client-only mixins cannot change server movement.
- Use `EndCrystal` and `EnderDragon` Mojang classes for the Nether Crystal/Dragon implementation.
- Retrieve persistent raid state from `server.overworld()`.
- Call `setDirty()` after every persistent state mutation.
- During structure worldgen, only call `WorldGenLevel#getBlockEntity` after verifying the target position is inside both the structure piece box and the current `chunkBox`; otherwise Minecraft can throw `Requested chunk unavailable during world generation`.
- During runtime proximity scans, guard structure-manager and heightmap checks with loaded-chunk checks so gameplay effects do not force new chunk work during save/quit.

## Project Progress

| Component | Status |
|---|---|
| Build config for MC 1.21.11, Java 21, Mojang mappings | COMPLETE |
| Main initializer and client initializer | COMPLETE |
| Client red storm packet receiver, overlay, music, thunder, sky, fog, weather mixins | COMPLETE |
| 7 combat entities plus Exiled Piglin registration | COMPLETE |
| GeckoLib entity and block renderer registration | COMPLETE |
| Synced GeckoLib mob texture variants and generated placeholder sheets | COMPLETE |
| Deterministic portal dungeon structure variants with runtime persistence | COMPLETE |
| Structure generation and vanilla ruined portal replacement hooks | COMPLETE |
| Structure-set village exclusion for ruined portal dungeons | COMPLETE |
| Calm low-frequency surface height variation around stable ritual core | COMPLETE |
| Graph-based cave nodes, noisy worm tunnels, side pockets, lava features, and ghast-ready deep caverns | COMPLETE |
| Runtime structure-local portal-zone ambient spawns and anchored ghasts | COMPLETE |
| Depth-escalating pre-raid spawners | COMPLETE |
| Proximity-only raid trigger, no gold armor, no Bad Omen | COMPLETE |
| Persistent completed portal tracking, active raid metadata, restart rehydration | COMPLETE |
| Multiplayer atomic raid start guard and boss-bar membership sync | COMPLETE |
| Config-aware Easy/Normal/Hard mob stat scaling, wave scaling, and mob weapon upgrades | COMPLETE |
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
| Portal Shard boss locator item and cooldown component | COMPLETE |
| Corrupted Netherite ingot, armor set, smithing recipes, and set bonuses | COMPLETE |
| Nether Dragon Scale trophy item | COMPLETE |
| Optional Patchouli Corrupted Chronicle data plus surface/boss chest injection | COMPLETE |
| StructureBlockPalette and Masterwork underground room templates | COMPLETE |
| Custom registered particles and generated placeholder sprites | COMPLETE |
| Nether Tide music disc, drops, jukebox particle effect, and jukebox song metadata | COMPLETE |
| Expanded optional REI information pages for Masterwork progression | COMPLETE |
| New advancements for conduit, necklace, crystal ritual, and dragon fight | COMPLETE |
| `./gradlew build` with Java 21 | COMPLETE |
| Full interactive `runClient` survival smoke test with log review | PENDING |

## READY FOR IN-GAME VERIFICATION

Current build/static verification:

- `./gradlew build` succeeds on Java 21 with `JAVA_HOME=C:\Users\dhruv\.codex\jdks\temurin-21`.
- JSON resources parse successfully, registered sounds match `sounds.json`, sound subtitles exist in `en_us.json`, advancement custom triggers match registration, and enchantment loot functions explicitly use `add: false`.
- GeckoLib assets use the 5.x `geckolib/models` and `geckolib/animations` layout, with render-pass tint data moved into render-state tickets.
- Minecraft `1.21.11` pack versions were verified from the local game jar as resource `75.0` and data `94.1`; no root `pack.mcmeta` is committed for the combined mod jar because it cannot truthfully declare both formats at once.

Implemented but still needs an interactive in-game smoke pass:

- Full survival path from `/locate structure minecraft:ruined_portal` through approach storm, five-wave raid, completion beats, Exiled Piglin trade, Nether Conduit use, Ghast Tear Necklace fireball, crystal ritual, Nether Dragon phase two, and dragon death finale.
- Dedicated server multiplayer checks for two players entering the raid trigger together, disconnecting during boss bars/trades, and participating in the dragon fight from different heights in the portal cave stack.
- Visual review for generated textures, placeholder sound mappings, red storm sky/fog/rain mixins, GeckoLib entity animations, and conduit block-entity rendering.

Remaining known limitations and future polish:

- The entity texture variants and particle sprites are generated release art, not hand-painted final art.
- The mod registers replaceable sound events with vanilla fallback event mappings instead of shipping bespoke `.ogg` assets.
- The Nether Dragon Scale remains a trophy item until a verified Accessories-compatible `1.21.11` release exists for this branch; its tooltip now states that the back-slot role is intentionally deferred.

Recommended first in-game test:

1. Start a fresh overworld, run `/locate structure minecraft:ruined_portal`, teleport to the result, and walk from the outer scar into the pit to verify storm activation, horizontal-distance behavior underground, structure blending, pre-raid spawners, and approach feedback.
2. Trigger and finish the five-wave raid, then confirm the staggered completion sequence: boss bar removal, fanfare/title, portal lighting, boss chest burst, Exiled Piglin spawn, spawner silence, and no post-raid hostile repopulation.
3. Place four Nether Crystals on the generated pedestals, fight the dragon through phase two, kill it, and confirm the pedestal shatter, delayed reward drop, advancement credit for nearby players, and absence of any End portal blocks.

## Known Limitations

- The entity textures and variant sheets are simple generated release art, not hand-painted final textures.
- The mod registers custom sound events with vanilla fallback mappings rather than shipping bespoke `.ogg` assets. Nether Tide intentionally uses `minecraft:music_disc.13` as placeholder audio for resource-pack replacement.
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
