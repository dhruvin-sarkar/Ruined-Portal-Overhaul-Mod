# Changelog

This changelog is written from the live repository history and current project docs. It favors player-visible behavior, compatibility notes, and verification status over raw commit labels. Older one-line entries are kept in the commit ledger at the bottom so pack makers and contributors can still trace when a change entered the project.

## Unreleased - 2026-05-06

This update is a world-generation and presentation polish pass for the portal encounter. The main goal was to make the giant scar feel like it belongs in the terrain it lands in, make the pit less awkward on slopes and water edges, reduce raid fatigue, and make all lore rewards behave like real mod content.

### Terrain And Structure Blending

- Reworked the portal scar so the transition from native biome terrain into Nether corruption is wider and smoother. The outer ring now keeps far more of the original biome surface, the middle ring mixes native/scorched blocks, and the inner scar carries the strong Nether palette.
- Added a per-column terrain profile used by the structure helper. Each column records the natural surface, ocean floor, water depth, slope, nearby liquid state, and native block family before scar placement decides what to do.
- Kept structure JSON `terrain_adaptation: none`. Vanilla beard/bury adaptation is too broad for this custom radius-136 piece and can fight the sculpted scar, so `PortalStructureHelper` remains the terrain authority.
- Replaced blunt water-to-lava conversion with context-aware liquid handling. Shallow water becomes cooled basalt and magma shoreline; deeper water gets volcanic support caps and rims; existing lava remains contained instead of spreading into messy exposed pools.
- Added slope and cliff support skirts so corrupted edges are less likely to float, shear vertically, or expose ugly straight shafts when the structure cuts into uneven ground.
- Preserved the existing Terralith skyland and cave exclusions while making normal difficult terrain adapt better. The intended behavior is to handle plains, forests, deserts, swamps, shorelines, cliffs, and mountains rather than rejecting them.

### Pit And Surface Rewards

- Reworked the pit mouth to follow per-column terrain height instead of carving from one fixed origin Y. This keeps the opening more readable on slopes, shorelines, and cliff edges.
- Added cleaner rim rubble, basalt/blackstone support ribs, staggered ledges, lower lava seeps, and better water sealing around the pit mouth.
- Increased deterministic surface chest candidates to 16 and made those placements terrain/liquid aware through the same surface cache helper used by scar support work.
- Increased loot rolls modestly without making netherite trivial:
  - `portal_surface` now rolls `12-16`.
  - `portal_deep` now rolls `14-18`.
  - `portal_boss_reward` now rolls `18-22`.
- Surface loot is now a stronger raid-prep spread: potions, healing, food, arrows, emergency totems, gold gear, diamonds, and rare scrap show up often enough to make stopping at the portal feel worthwhile.

### Raid Balance

- Reduced the default raid roster by roughly 60 percent while keeping mob health, damage, difficulty scaling, and ambient portal-zone spawn pressure unchanged.
- New default tracked wave sizes before config/difficulty scaling are `5 / 8 / 9 / 10 / 12`.
- Increased Evoker presence in waves 3-5 so the encounter still escalates clearly despite the lower total mob count.
- Kept the boss bar, inter-wave pacing, spawn ring logic, and completion sequence intact.

### Lore Items And Creative Inventory

- Promoted lore artifacts from renamed vanilla loot stacks into real registered mod items:
  - Corrupted Portal Key
  - Shard of the Nether
  - Corrupted Ravager Hide
  - Embered Grimoire
  - Voidash Powder
- Added generated item models, rarity, fire resistance, localized display names, and three-line tooltips for each artifact.
- Updated loot tables to drop the real item IDs instead of vanilla items with `set_name`/`set_lore` functions.
- Added all new artifact keys to every locale stub so translators inherit the full key set.
- Completed creative inventory visibility:
  - The Ruined Portal Overhaul tab now contains every custom block and item.
  - Ingredients include crystals, ingots, scale, powders, hide, grimoire, and shard.
  - Tools and utilities include the Portal Shard, Ghast Tear Necklace, Nether Tide disc, and Corrupted Portal Key.
  - Combat includes the necklace and full Corrupted Netherite armor set.

### Atmosphere, Audio, And Docs

- Slightly increased red storm visual intensity without changing the completed-portal low-level state.
- Replaced generated/procedural audio as the final shipped soundscape with edited online CC0 sources from Kenney and OpenGameArt.
- Documented online audio sources and output mapping in `assets/audio_sources/ONLINE_AUDIO_ATTRIBUTION.md`.
- Kept the online-audio preparation script so the current soundscape can be rebuilt from the source files.
- Updated `CLAUDE.md`, `SPEC.md`, `README.md`, and this changelog to reflect the worldgen, raid, loot, lore item, and creative inventory pass.
- Corrected an old changelog typo in the 2026-04-22 history section.

### Validation

- `RESOURCE_POLISH_OK` resource audit passes for JSON parsing, lore item models, language keys, and loot references.
- `./gradlew.bat build` succeeds with Java 21.
- `git diff --check` reports no whitespace errors.
- Remaining verification is live gameplay review: inspect generated portals in plains, forests, deserts, swamps, shorelines/deep water, cliffs/mountains, and Terralith-style skyland/cave exclusion cases.

## v1.0.0 - 2026-05-03

This was the first full release candidate for the complete encounter chain: a corrupted overworld portal dungeon, five-wave Piglin Illager raid, post-raid trader, Nether Conduit progression, Ghast Tear Necklace, Nether Crystal ritual, and portal-anchored Nether Dragon boss.

### Release Readiness

- Completed the release presentation layer: README, Modrinth description, contribution guide, issue templates, and reconstructed changelog.
- Added English-valued localization skeletons for `de_de`, `es_es`, `fr_fr`, `ja_jp`, `pt_br`, `ru_ru`, and `zh_cn`. These files are intentionally English stubs so players do not see raw keys and translators have a full starting point.
- Added operator-only `/rpo` admin commands for testing and server support:
  - `/rpo locate`
  - `/rpo status`
  - `/rpo reset`
  - `/rpo wave <1-5>`
  - `/rpo complete`
  - `/rpo dragon`
- Recorded a dedicated-server dry start through Fabric, GeckoLib, and mod initialization. The server stopped at the expected unaccepted-EULA gate, which verifies class loading without accepting the EULA on the user's behalf.
- Recorded a bounded client startup smoke through client initializer, renderer/resource loading, sound engine startup, texture atlas creation, recipe and advancement loading, biome modifications, and integrated-server startup.
- Reconciled `CLAUDE.md`, `SPEC.md`, `README.md`, and release-facing text with the live source tree.
- Verified Java 21 build health and static resource shape, including language keys, sound subtitles, loot data, advancement triggers, GeckoLib assets, particles, Patchouli data, recipes, mixin references, and release artifact sanity.

### Portal Dungeons And World Generation

- Replaced overworld ruined portal encounters with large procedural portal dungeons while preserving vanilla Nether ruined portals.
- Added a radius-136 corrupted scar with a protected ritual core, ruined frame, Exiled Piglin anchor, surface caches, ambient details, and lower dungeon access.
- Added three deterministic structure variants:
  - Crimson Throne as the baseline scar form.
  - Sunken Sanctum with a lowered ritual bowl and heavier soul terrain.
  - Basalt Citadel with a wider blackstone platform and basalt vertical drama.
- Persisted discovered variants in `PortalRaidState` so runtime systems can query a portal's shape without mutating persistent state during chunk generation.
- Built a deep underground dungeon network with a large primary chamber, worm-carved organic tunnels, side pockets, ghast-ready deep caverns, lava runs, glowstone pockets, cache pads, and special rooms.
- Added Masterwork room templates, including the Wither Shrine, Gold Vault, Blaze Chamber, and hidden Ancient Vault.
- Hardened structure generation against chunk-boundary crashes by keeping block and block-entity access bounded by the structure piece and current chunk box.
- Ensured generated deep chests receive exactly one Nether Conduit through direct structure chest insertion while preserving normal loot table output.
- Added data-side village exclusion and broader Terralith skyland/cave biome exclusions.
- Added structure rarity and outer scatter config support for pack tuning.

### Red Storm Atmosphere

- Added red storm atmosphere tied to portal territory instead of normal biome weather.
- Implemented client sky, fog, rain, weather, thunder, overlay, and ambience hooks through the client initializer and client mixins.
- Used horizontal X/Z portal distance for zone membership so surface players, pit players, and cave players all stay attached to the same encounter.
- Added completed-portal low-level ambience so cleared scars remain eerie without acting like an active raid.
- Tuned storm pulse cadence to feel slower and less flickery.
- Added intensity-scaled storm rumble and cleaned up payload states with explicit dimension guards.
- Reduced recurring atmosphere range cost by throttling refresh work and using cheaper distance checks in hot paths.

### Gold Tribute Raid

- Added the five-wave Gold Tribute Raid with proximity activation, no Bad Omen requirement, custom wave pacing, boss bar tracking, inter-wave warnings, spawn ring placement, and completion rewards.
- Added restart recovery so saved active raids can rehydrate with wave number, active mob UUIDs, and remaining inter-wave delay.
- Staggered completion into readable beats: boss bar removal, spawner disable, fanfare, portal lighting, boss chest burst, Exiled Piglin spawn, and final state persistence.
- Expanded wave spawn retries outward when ring positions are blocked so a bad floor spot does not silently erase wave counts.
- Used squared distance checks in ambient spawn loops and guarded ambient spawning behind player proximity and configuration.
- Removed disconnecting players from all active raid boss bars and rebuilt boss bar viewer tracking around the portal radius.
- Kept raid completion synced with nearby player credit and post-raid spawn suppression.

### Custom Mobs And GeckoLib

- Added the Piglin Illager combat family: Piglin Pillager, Piglin Vindicator, Piglin Brute Pillager, Piglin Illusioner, Piglin Evoker, Piglin Ravager, and Piglin Vex.
- Added the Exiled Piglin as a post-raid Wandering Trader-derived reward entity.
- Moved custom mob rendering to GeckoLib 5.4.5 model and animation assets.
- Added synced texture variants for combat mobs so repeated waves have more visual variety.
- Added mob sound routing, voice pitch tuning, attack triggers, Vex flying idle, Ravager roar animation, melee swing timing matched to GeckoLib clips, and keyframe cues for important animation moments.
- Added Piglin Pillager crossbow kiting, safer Brute Pillager goal priority, limited-life Piglin Vex behavior, and portal-anchored Phase 2 guardians.
- Added generated entity texture variants and a reproducible Pillow post-process that improves contrast, edge shading, gold/orange readability, and small-scale sharpness without changing UV layouts.

### Rewards, Items, And Progression

- Added the core reward chain:
  - Portal Shard discovery item.
  - Nether Conduit structure reward and mob drop path.
  - Ghast Tear Necklace carried charm and fireball ability.
  - Nether Crystal ritual item.
  - Corrupted Netherite Ingot and armor set.
  - Nether Dragon Scale trophy.
  - Nether Tide music disc.
  - Shard of the Nether and Corrupted Portal Key lore rewards.
- Switched Portal Shard targeting from sampled world positions to saved `PortalRaidState` origins, then cross-checked already-loaded origins against live structure data before pointing players there.
- Added player-facing item tooltips and localized named artifacts.
- Improved surface, deep, boss, and entity loot tables, including nether star access for the Nether Crystal progression loop.
- Limited boss chest netherite ingots so the reward feels strong without skipping too much progression.
- Aligned enchantment loot functions with the current vanilla format.
- Clarified Dragon Scale as an intentional trophy/material item while Accessories compatibility remains blocked for Minecraft `1.21.11`.

### Nether Conduit

- Added the Nether Conduit block, block entity, and item.
- Implemented activation from a flat 12-nether-brick horizontal frame rather than vanilla conduit geometry.
- Added support effects, upgrade levels, lava movement help, Nether sleep support, and nether mob attack behavior.
- Moved the conduit to a GeckoLib animated block renderer with synced active/inactive state and inner glow.
- Preserved upgraded conduit level when broken.
- Cleared runtime lava movement boost state when the server stops.
- Capped conduit attack scans to prevent unbounded target processing.

### Ghast Tear Necklace

- Reworked the necklace as a native carried charm after removing the unverified Accessories dependency path.
- Added Speed II and Jump Boost II while carried.
- Added persistent fireball cooldown state through a data component on the carried stack.
- Added client keybind support and typed C2S packet handling.
- Validated fireball requests server-side: carried necklace, alive player, not spectator, cooldown elapsed, finite look vector, and reasonable server-look alignment.
- Rejected invalid fireball packets silently and used the server-authoritative look direction when needed.
- Released Exiled Piglin trade locks on player disconnect and cleared stale locks after restart.

### Nether Crystal Ritual And Nether Dragon

- Added Nether Crystal item placement and a GeckoLib-rendered Nether Crystal entity.
- Generated four ritual pedestals around each completed portal and tracked filled pedestals in persistent state.
- Allowed pre-staged crystals to start the ritual after raid completion.
- Stopped disabled dragon pedestals from consuming crystals when the dragon system is turned off.
- Added ritual recovery that reconciles saved pedestal state with loaded crystal entities.
- Added the Nether Dragon as a portal-anchored boss based on vanilla dragon combat semantics while suppressing End fight output.
- Kept the dragon from drifting toward overworld origin by actively anchoring it to the portal arena.
- Suppressed nearby vanilla End Crystals so Overworld crystal healing cannot affect the fight.
- Added phase-two enragement, flight boosts, Nether Slam, breath aura, boss bar tracking, XP behavior, advancement credit, and a staggered death finale.
- Prevented Nether Slam from double-damaging players and kept it non-griefing.
- Dropped dragon death loot at the portal ring instead of the corpse location.
- Kept active dragon locks while chunks are unloaded and granted fight credit across the full arena.

### Advancements, Text, Audio, And Discovery

- Added advancement milestones for portal approach, aether boon, territory totem, pit descent, deep storm, raid start, raid completion, Exiled Piglin trade, conduit use, necklace carry/fireball use, crystal ritual, dragon defeat, and late-game rewards.
- Localized advancement display text, raid labels, conduit status messages, item names, lore lines, sound subtitles, and player-facing action bar/title text.
- Added mod-owned sound events for raid, conduit, ritual, dragon, storm, music, item abilities, and reward moments.
- Fixed sound fallback behavior and routed encounter calls through the mod sound registry.
- Added optional REI progression pages for important systems and corrected recipe hints.
- Added optional Patchouli guide data and guarded runtime guide injection behind Patchouli availability.
- Removed internal draft wording from release-facing text before closeout.

### Compatibility, Configuration, And Safety

- Added Cloth Config and ModMenu support with grouped categories, live gameplay tuning, validation, and pack-tuning tooltips.
- Removed dead config options and aligned runtime behavior for structure rarity, combat scaling, scatter, nether star drops, portal biome rules, ambient spawns, and post-raid suppression.
- Guarded optional REI and Patchouli integration paths so neither becomes a hard dependency.
- Kept the project free of the unverified Accessories API runtime dependency on this Minecraft `1.21.11` branch.
- Added post-raid hostile spawn suppression for completed portal territories.
- Added safe persistent-state codec defaults for older saves and restart safety.
- Repaired save-and-exit chunk scanning and portal scans during save.
- Kept common code dedicated-server safe by moving client-only rendering, packets, and mixins into client entrypoints/configuration.

## Development History

### 2026-05-03 - Release Packaging And Verification

- Added release docs, contribution guide, Modrinth page text, issue templates, and a release README for testers.
- Added the `/rpo` admin command suite for live testing and server administration.
- Added localization skeletons for seven major locales.
- Recorded dedicated-server dry start verification and client startup smoke verification.
- Synced `SPEC.md`, `CLAUDE.md`, and README references with final verification notes.

### 2026-05-02 - Final Deep Audit And Release Polish

- Sanitized release-facing language and removed internal draft wording.
- Staggered raid completion and dragon death finale moments into clearer visual/audio beats.
- Added intensity-scaled red storm rumble.
- Validated necklace fireball direction against server-side look data.
- Aligned enchantment loot functions with vanilla format.
- Moved GeckoLib render tints into render-state data flow.
- Fixed horizontal Nether Tide portal checks.
- Switched Portal Shard lookup to saved portal state.
- Expanded raid wave spawn retries outward from blocked ring positions.
- Clarified underground ambient spawn floor checks.
- Anchored the Nether Dragon and suppressed nearby vanilla End Crystals.
- Added GeckoLib keyframe cues for key encounter animations.
- Cleaned storm payload state handling and added explicit dimension guarding.
- Added older-save-safe defaults for portal raid state.
- Cleared stale Exiled Piglin trade locks when entities load.
- Broadened Terralith cave biome exclusions.
- Clarified the Dragon Scale trophy role.
- Refreshed project docs after final validation.

### 2026-05-01 - Targeted Compliance And System Tightening

- Recorded May build validation.
- Aligned GeckoLib asset paths and loading behavior for version 5.
- Matched melee swing timing to GeckoLib attack clips.
- Carried necklace fireball aim through the packet.
- Made named loot rewards replace existing names cleanly.
- Rewarded the hardest advancement milestones with proportional XP.
- Stopped vanilla End Crystals from affecting the Nether Dragon.
- Added clearer config tooltips for modpack tuning.
- Released Exiled Piglin trade locks on disconnect.
- Tuned red storm pulse cadence.
- Switched portal generation to convention biome tags.
- Ignored invalid ritual pedestal state writes.
- Made Piglin Pillagers kite with crossbows.
- Expanded dragon fight credit across the full arena.

### 2026-04-30 - Patchouli Guide Stability

- Fixed Patchouli guide loading for the optional in-game book content.

### 2026-04-28 - Masterwork Continuation

- Added masterwork reward and discovery systems on top of the raid and dragon loop.
- Tightened packet validation, reward items, custom particles, room templates, resource data, and project docs for the masterwork pass.

### 2026-04-24 - Restart Safety And Runtime Fixes

- Kept the Exiled Piglin chained to its fence post.
- Let pre-staged crystals trigger the dragon ritual.
- Stopped disabled dragon pedestals from eating crystals.
- Made custom sound fallbacks actually play.
- Protected generated chest loot from conduit insertion side effects.
- Removed old vanilla renderer paths after the GeckoLib migration.
- Preserved conduit upgrade level when broken.
- Kept completed portal spawners cleared.
- Throttled dragon ritual scans.
- Preserved raid pacing after restart.
- Restored saved raid wave delays.
- Stopped storm music on world unload.
- Dropped dragon death loot at the portal ring instead of the corpse.
- Ignored local development database files.

### 2026-04-23 - Major Polish, Performance, And Compatibility Pass

- Added deterministic portal dungeon variants, shaped variant layouts, and persisted discovered variants.
- Routed encounter sounds through mod sound events and tuned custom mob sound mix.
- Added Nether Conduit GeckoLib animation and active core lighting.
- Localized named loot artifacts, advancement display text, raid text, and conduit status text.
- Added optional REI progression pages, corrected REI hints, and guarded optional REI displays.
- Limited ambient corruption and portal placement in Terralith biomes.
- Added synced GeckoLib mob variants and generated texture variation.
- Kept ruined portal dungeons away from villages.
- Layered Nether breath aura onto the enraged dragon.
- Anchored Phase 2 guardians to the portal.
- Animated Nether Crystal and Nether Dragon with GeckoLib.
- Aligned structure rarity, necklace recipe, combat scaling, scatter, nether star drops, and biome rule configuration.
- Guarded natural portal spawns and validated loaded config values.
- Triggered ravager roar, melee, pillager melee, and Vex idle animations.
- Ordered dragon death finale and boosted enraged dragon flight.
- Broadened Terralith exclusions and preserved completed-portal storm visibility.
- Persisted Exiled Piglin trader spawn times.
- Tightened conduit activation ring.
- Corrected crystal placement consumption and prevented Nether Slam double damage.
- Aligned raid completion ordering, wave documentation, and concise spec notes.
- Respected mob loot gamerule for dragon rewards.
- Kept active dragon lock while unloaded.
- Throttled boss bar player sync and atmosphere refresh.
- Guaranteed conduit placement in generated deep chests.
- Restored scar material sectors and added crying obsidian scatter.
- Reduced atmosphere range cost.
- Aligned Piglin Vex limited life and safer brute priority.
- Rebuilt dragon boss bar viewer management.
- Limited boss chest netherite ingots.
- Tightened conduit radius tiers.
- Respected ambient spawn config in portal loops.
- Resumed pre-wave raids at first wave.
- Used squared checks in hot ambient spawn loops.
- Rejected invalid fireball packets from dead or spectating players.
- Detached disconnecting players from active boss bars.
- Preserved vanilla Nether ruined portal placement.
- Kept portal ambience local to dungeon areas.
- Restored post-raid suppression toggle.
- Corrected ambient worldgen notes and Accessories blocker docs.
- Kept dragon ritual cues and raid completion synced to the portal footprint.

### 2026-04-22 - GeckoLib, Config, Optional Integrations, And Current Runtime Docs

- Updated docs to match the current runtime and dependency state.
- Added GeckoLib 5.4.5 and moved custom mob rendering onto GeckoLib model assets.
- Registered the Nether Dragon client renderer.
- Built out the Nether Dragon enraged fight.
- Added mod-owned sound events for raid, conduit, and ritual systems.
- Added optional config support and live gameplay tuning.
- Polished custom item tooltips.
- Showed brute armor and ravager saddle visuals.
- Added optional REI progression pages.
- Limited ambient corruption in Terralith biomes and advertised verified worldgen compatibility.
- Added synced GeckoLib mob variants.
- Kept ruined portal dungeons away from villages.
- Cleared conduit boost state when servers stop.
- Limited red storm weather overrides to the client world.

### 2026-04-21 - Ritual Recovery

- Repaired Nether Dragon ritual recovery after interrupted or inconsistent save states.

### 2026-04-20 - Lunar Compatibility And Save Hardening

- Added Lunar-compatible necklace integration and content-expansion tracking.
- Resolved main-branch merge context.
- Fixed deep chest world-generation crash.
- Fixed save-and-exit chunk scanning and hardened portal scans during save.

### 2026-04-19 - Master Content Expansion

- Added Nether Conduit lava movement, Nether sleep support, mob attacks, upgrade levels, structure chest placement, and mob drop acquisition.
- Added Ghast Tear Necklace item behavior, passive effects, fireball keybind, fireball packet, fireball spawn logic, recipe, and original accessory-slot binding.
- Improved loot tables across surface, deep, and boss chests.
- Added Nether Crystal entity, tinted renderer, item placement rules, recipe, generated netherite pedestals, and ritual detection.
- Added Nether Dragon entity, End-behavior suppression, summoning sequence, death drops, and pedestal shattering.
- Added advancements for conduit, necklace, crystal, and dragon progression.
- Documented the master content expansion.

### 2026-04-18 - Storm, Raid Activation, Balance, And Early Endgame Scaffold

- Overhauled music and raid activation.
- Added cave, storm, and achievement changes.
- Balanced wave counts and advancement behavior.
- Added completed-area spawn suppression.
- Added nether star rewards to key mobs and chests.
- Began Nether Conduit scaffolding, activation frame detection, and support effects.
- Briefly experimented with Accessories API dependency and necklace slot data before later removing it for compatibility.

### 2026-04-16 - Raid Logic And Weather Overlay

- Advanced raid logic and weather overlay behavior.
- Tuned difficulty and fog systems.

### 2026-04-14 - Chunk Generation And Terrain Stability

- Fixed chunk-generation breakage in structure placement.
- Tuned mob difficulty and terrain behavior.
- Added additional terrain and quality-of-life adjustments.

### 2026-04-13 - World Generation And Jar Prep

- Prepared the project for jar packaging.
- Sanitized docs.
- Fixed and overhauled world generation.
- Added raid difficulty logic.
- Continued world generation and encounter polish.

### 2026-04-12 - Mob And Raid Overhaul

- Added dungeon mob spawning around the Piglin Illager Shaman work.
- Overhauled mob and raid logic.
- Audited raid logic.
- Added quality-of-life fixes and missing implementation pieces.

### 2026-04-06 To 2026-04-07 - Foundation Content

- Scaffolded the Fabric mod baseline.
- Added decompiled Minecraft reference sources and supporting client, world, server, network, entity, renderer, UI, and utility source batches for local reference.
- Added the first portal dungeon structure and ruined portal replacement behavior.
- Added loot tables for portal dungeon structure chests.
- Added the Piglin Illager custom entity family.
- Audited Minecraft API usage and type safety.
- Added early dungeon mob spawning work.

### 2026-04-05 - Project Start

- Created the initial repository.

## Reconstruction Sources And History Notes

This changelog was expanded from these sources:

- Local `git log --all --reverse` after fetching the GitHub remote on 2026-05-04.
- GitHub remote `origin`, currently `https://github.com/dhruvin-sarkar/Ruined-Portal-Overhaul-Mod.git`.
- Local `main`, which includes the release documentation and changelog commits beyond the last fetched `origin/main` snapshot.
- Local historical branches for the decompile batches and master content expansion work.
- `CLAUDE.md`, the canonical project context, last reconciled 2026-05-03.
- Prior local Codex memory for the April 22, April 28, and May 1-2 audit and release validation passes.

Reference points at the start of this appendix pass:

- Earliest repository commit: `ae09309` on 2026-04-05.
- GitHub `origin/main` snapshot after fetch: `f794bdf`, the final validation docs refresh from 2026-05-02.
- Local release tip before this appendix: `051ec27`, the first reconstructed changelog pass from 2026-05-03.
- Local `v1.0.0` tag before this appendix was committed: annotated tag resolving to `051ec27`; later release-history commits should move the tag forward.
- Local-only release commits after `origin/main`: final verification docs, README, admin commands, localization skeletons, release presentation docs, and changelog reconstruction.

## Current Technical Surface Reference

These are the main code and resource surfaces represented by the v1.0.0 history:

- Entry points: `RuinedPortalOverhaul`, `RuinedPortalOverhaulClient`, `fabric.mod.json`, common mixin config, and client mixin config.
- Raid and persistence: `GoldRaidManager`, `PortalRaidState`, `NetherDragonRituals`, boss bar state, wave UUID tracking, raid restart recovery, and codec-backed world save data.
- World generation: `PortalDungeonStructure`, `PortalDungeonPiece`, `PortalDungeonVariant`, `PortalStructureHelper`, `StructureBlockPalette`, `ModStructures`, `ModWorldGen`, and biome/structure JSON data.
- Atmosphere and networking: `PortalAtmospherePayload`, `PortalAtmosphereClient`, `DragonPhaseFlashPayload`, `NetherFireballPayload`, `NetherFireballHandler`, `ModPackets`, and storm/fog/sky/weather client mixins.
- Custom entities: Piglin Pillager, Brute Pillager, Vindicator, Evoker, Illusioner, Vex, Ravager, Exiled Piglin Trader, Nether Crystal, and Nether Dragon.
- GeckoLib rendering: entity/block `.geo.json` models, entity/block animation JSON, Geo renderers, Geo models, render-state data tickets, mounted item layer, conduit glow layer, and animation keyframe cues.
- Items and rewards: Portal Shard, Ghast Tear Necklace, Nether Conduit, Nether Crystal, Corrupted Netherite armor and ingot, Nether Tide music disc, Dragon Scale, Shard of the Nether, and Corrupted Portal Key.
- Conduit systems: `NetherConduitBlock`, `NetherConduitBlockEntity`, `NetherConduitPowerTracker`, `NetherConduitEvents`, lava movement mixin, conduit damage type, activation frame, upgrade state, effects, and structure chest insertion.
- Optional discovery: REI client plugin, Patchouli guide book data, advancement tree, localized text, custom sound events with online-sourced `.ogg` assets, custom particles, and post-processed generated texture variants.
- Administration and testing: `/rpo` locate, status, reset, wave, dragon, and complete commands for operators and modpack debugging.
- Release packaging: README, Modrinth description, contribution docs, issue templates, localization skeletons, build validation notes, dedicated-server dry start, and client startup smoke notes.

## Complete Commit Ledger

This ledger preserves every visible commit from local history, including branch-only setup work and duplicate decompile-batch commits that existed before the project was cleaned into its main release line. Each entry is rewritten as a normal changelog sentence while keeping the short commit hash for reference.

### 2026-05-03

- `051ec27` - Reconstructed the changelog from project history through the first repository commit.
- `347accb` - Added release presentation documents, including the Modrinth description, contribution guide, and issue templates.
- `c5031a9` - Added English-valued localization skeleton files for seven major non-English locales.
- `6829dd4` - Added the `/rpo` admin command suite for fast portal, raid, and dragon testing.
- `88b0d6e` - Recorded client startup smoke verification for the release candidate.
- `c6a7274` - Linked the release README from project documentation.
- `1e5daa4` - Added a release README aimed at testers and pack users.
- `6cec645` - Recorded dedicated-server dry start verification through the expected EULA stop.
- `364e35f` - Synced `SPEC.md` with the final verification notes.

### 2026-05-02

- `a93d5a6` - Synced `CLAUDE.md` with final verification notes.
- `bb1d8b1` - Clarified that Dragon Scale is an intentional trophy and material item.
- `369b6b0` - Broadened Terralith cave-biome exclusions.
- `6467033` - Reworked portal chest potion loot to use vanilla potion loot functions.
- `ed0ea92` - Cleared stale Exiled Piglin trade locks when trader entities load.
- `999b4bf` - Made `PortalRaidState` load older saves safely with optional defaults.
- `7af7bea` - Cleaned up storm payload states and added explicit dimension guarding.
- `7e5c15d` - Added GeckoLib keyframe cues for encounter animations.
- `574f843` - Kept the Nether Dragon anchored to the portal arena and suppressed nearby End Crystals.
- `64f1322` - Clarified underground ambient spawn floor logic.
- `3ebce04` - Expanded raid wave spawn retries outward when blocked positions are found.
- `151c5c2` - Switched Portal Shard target lookup to saved portal state.
- `3c6649f` - Changed Nether Tide portal checks to horizontal distance.
- `f794bdf` - Refreshed project docs after the final validation pass.
- `f41cfa1` - Moved GeckoLib render tints into render-state data.
- `60958bc` - Aligned enchanted loot functions with vanilla format.
- `a652057` - Added intensity-scaled red storm rumble.
- `180e81a` - Split the Nether Dragon death finale into separate readable beats.
- `397bb49` - Validated necklace fireball direction against the server-side player look.
- `0f457c6` - Staggered raid completion into readable reward beats.
- `f603e25` - Removed internal release-draft wording from player-facing and release-facing text.

### 2026-05-01

- `b4ec882` - Expanded Nether Dragon fight credit across the full arena.
- `766e408` - Made Piglin Pillagers kite with crossbows.
- `ffc555a` - Ignored invalid ritual pedestal state writes.
- `298d499` - Switched portal generation to convention biome tags.
- `e5ac4cc` - Tuned red storm pulse cadence.
- `e2465a4` - Released Exiled Piglin trade locks when players disconnect.
- `50baac9` - Added clearer config tooltips for pack tuning.
- `e8a318d` - Stopped vanilla End Crystals from affecting the Nether Dragon.
- `1fb9bcd` - Added stronger rewards to the hardest advancement milestones.
- `56f5192` - Made named loot rewards replace existing names cleanly.
- `875c587` - Carried necklace fireball aim through the packet.
- `d308557` - Matched melee swing timing to GeckoLib attack clips.
- `e0468bc` - Aligned GeckoLib assets with version 5 loading expectations.
- `50c7770` - Recorded May build validation.

### 2026-04-30

- `4380796` - Kept the optional Patchouli guide loading correctly.

### 2026-04-28

- `68145c8` - Tightened masterwork continuation systems across networking, rewards, particles, room templates, resources, and docs.
- `aa6cd74` - Added masterwork reward and discovery systems on top of the main raid and dragon progression loop.

### 2026-04-24

- `751f6ad` - Removed an accidentally tracked local development database artifact from release history.
- `267db28` - Moved dragon death loot to the portal ring instead of the rising corpse position.
- `d615797` - Refreshed docs after raid and storm fixes.
- `de8ab4a` - Stopped storm music on world unload.
- `44591fe` - Preserved restored raid wave delays.
- `c7a9919` - Preserved raid pacing after restart.
- `73a1aa4` - Throttled dragon ritual scans.
- `555f3bb` - Kept completed portal spawners cleared.
- `49ed633` - Preserved upgraded conduits when broken.
- `1fbdbb9` - Removed old vanilla renderer paths after the GeckoLib migration.
- `0f39004` - Protected generated chest loot from conduit insertion side effects.
- `784d991` - Made custom sound fallbacks actually play.
- `d184e4e` - Kept `SPEC.md` aligned with the live ritual flow.
- `4f9221e` - Stopped disabled dragon pedestals from consuming crystals.
- `77702ba` - Allowed pre-staged crystals to start the dragon ritual.
- `3ecad98` - Kept the Exiled Piglin chained to its fence post.

### 2026-04-23

- `53ca042` - Kept raid completion synced with the boss bar radius.
- `82fcbc1` - Kept dragon ritual cues aligned to the portal footprint.
- `1482900` - Recorded the current Accessories compatibility blocker accurately.
- `bb84ecc` - Kept the portal scar threatening after raid completion.
- `e0e5588` - Corrected ambient world-generation notes.
- `9c2398e` - Restored the post-raid natural spawn suppression toggle.
- `b037b26` - Kept portal ambience local to the dungeon.
- `eb4ca75` - Preserved vanilla Nether ruined portal placement.
- `16f0535` - Removed disconnecting players from all active raid and dragon boss bars.
- `c89b3e2` - Rejected necklace fireball packets from dead or spectating players.
- `5e6f7c0` - Refreshed the release description.
- `c41e6b2` - Used squared checks in ambient spawn loops.
- `2be4e3d` - Resumed pre-wave raids at the first wave.
- `cd3a1cf` - Respected ambient spawn configuration inside portal loops.
- `fe8a70c` - Tightened Nether Conduit radius tiers.
- `c024e49` - Limited netherite ingots in boss chests.
- `476b41f` - Rebuilt Nether Dragon boss bar viewer management.
- `dcc02c0` - Used registered fangs and safer Brute Pillager priority.
- `f63d221` - Aligned Piglin Vex limited-life behavior.
- `676dab4` - Reduced recurring atmosphere range cost.
- `dc603b9` - Added crying obsidian to outer scatter.
- `346b122` - Restored middle scar material sectors.
- `b1b8999` - Guaranteed Nether Conduit placement in generated deep chests.
- `4c89543` - Throttled portal atmosphere refresh.
- `5ddcc38` - Throttled raid boss bar player sync.
- `630571b` - Kept active dragon locks while chunks are unloaded.
- `3ee6d5c` - Respected the mob loot gamerule for dragon rewards.
- `d5ab2a8` - Ordered Exiled Piglin trader persistence after completion.
- `930260d` - Aligned the concise spec with current systems.
- `faf3036` - Corrected raid wave documentation.
- `aab5119` - Aligned raid completion order.
- `ee46230` - Prevented double damage from Nether Slam.
- `e8a07a6` - Corrected crystal placement consumption.
- `71a14a2` - Cleaned renderer and tooltip compile notes.
- `4f3b418` - Tightened the conduit activation ring.
- `6543dbc` - Persisted Exiled Piglin trader spawn times.
- `3620e8d` - Corrected the conduit helper documentation path.
- `26aae78` - Kept completed portal storm effects visible.
- `93e0a06` - Avoided duplicate dragon XP rewards.
- `1987a6a` - Broadened Terralith biome exclusions.
- `d1f1424` - Set a custom mob sound mix.
- `6be2e06` - Boosted enraged dragon flight.
- `d4c1306` - Ordered the dragon death finale.
- `0013c6f` - Grouped config screen options.
- `242a54d` - Wired portal rarity configuration.
- `0858576` - Used Vex flying idle animation.
- `a51d471` - Animated Piglin Pillager melee hits.
- `9d7db5a` - Lit the Nether Conduit core.
- `3de5500` - Triggered melee attack animations.
- `ed07e0d` - Triggered ravager roar animation.
- `7226380` - Guarded optional REI displays.
- `d8e8983` - Validated loaded config values.
- `7a5cb2b` - Guarded natural portal spawns.
- `f1121ef` - Honored portal structure biome rules.
- `3fbf439` - Applied nether star drop configuration.
- `890742a` - Respected outer scatter configuration.
- `07d878d` - Clarified combat spawn scaling.
- `d7e33bd` - Aligned necklace recipe details.
- `db9e921` - Aligned structure rarity configuration.
- `148063b` - Animated the Nether Dragon with GeckoLib.
- `7daea88` - Animated Nether Crystal with GeckoLib.
- `97efdec` - Anchored Phase 2 guardians to the portal instead of the dragon.
- `61d8689` - Layered a Nether breath aura onto the enraged dragon.
- `7fb1821` - Tuned voice pitch per custom mob.
- `426ac2a` - Removed dead structure rarity config state.
- `9a4e905` - Dropped an unused Difficulty import from the scaler.
- `398aa73` - Dropped an unused ModItems import from the fireball handler.
- `d67ce51` - Corrected the ghast tear count in the REI recipe hint.
- `7e99d2b` - Localized advancement display text.
- `a4eb0fb` - Localized named loot artifacts.
- `3e1063b` - Added Nether Conduit GeckoLib animation.
- `5d03e91` - Routed encounter sounds through mod sound events.
- `ae3dee2` - Refreshed docs for portal dungeon variants.
- `9c5c32d` - Persisted discovered portal dungeon variants.
- `c732bea` - Shaped portal dungeon structure variants.
- `1092cd1` - Added deterministic portal dungeon variants.

### 2026-04-22

- `99ea9f7` - Refreshed `CLAUDE.md` with current expansion state.
- `d43e332` - Kept ruined portal dungeons away from villages.
- `59a0e2f` - Added synced GeckoLib mob variants.
- `eef482e` - Advertised verified world-generation compatibility.
- `055f6d1` - Limited ambient corruption in Terralith biomes.
- `5099705` - Added optional REI progression pages.
- `9f5cee9` - Translated raid and conduit status messages.
- `1f2f3b9` - Showed Brute armor and Ravager saddle visuals.
- `4c4e939` - Polished custom item tooltips.
- `6af5582` - Built out the Nether Dragon enraged fight.
- `eea67bc` - Registered the Nether Dragon client renderer.
- `6f66fa6` - Added optional config support and live gameplay tuning.
- `2417cdd` - Gave raid, conduit, and ritual systems mod-owned sound events.
- `78c4504` - Moved the custom mob renderer stack onto GeckoLib model and animation assets.
- `4ce65e0` - Added GeckoLib 5.4.5 for animated entities.
- `2171e05` - Updated project docs to match the current runtime and dependency state.
- `772c938` - Cleared conduit boost state when the server stops.
- `3c756be` - Limited red storm weather overrides to the client world.

### 2026-04-21

- `dca77c9` - Repaired dragon ritual recovery.

### 2026-04-20

- `2f8837f` - Hardened portal scans during save.
- `6814ec1` - Fixed save-and-exit chunk scanning.
- `034aa52` - Fixed deep chest world-generation crashes.
- `049c906` - Recorded an intermediate checkpoint after merge resolution.
- `4b04bc2` - Resolved the main-branch merge and context tracking.
- `51fe81b` - Added content expansion updates and a Lunar-compatible necklace direction.

### 2026-04-19

- `25a52c5` - Recorded an intermediate checkpoint in the expansion branch.
- `f985d24` - Added a large project context expansion.
- `83b78db` - Documented the master content expansion.
- `c4cf589` - Added advancements for conduit, necklace, crystal, and dragon progression.
- `ad04c71` - Implemented Nether Dragon death drops and pedestal shattering.
- `8eb3203` - Implemented the Nether Dragon summoning sequence.
- `7afb192` - Added `NetherDragonEntity` with End-behavior suppression.
- `deb6b81` - Implemented the ritual detection system for crystal placement.
- `53ac6d9` - Placed netherite pedestals during structure generation.
- `11268b8` - Added the Nether Crystal crafting recipe.
- `9408c92` - Added the Nether Crystal item and placement rules.
- `00392d7` - Added `NetherCrystalEntity` with a tinted renderer.
- `398366d` - Improved loot tables across surface, deep, and boss chests.
- `f97f663` - Bound the necklace to the original accessory-slot data before the later native-inventory rewrite.
- `7e6e7f8` - Added the Ghast Tear Necklace crafting recipe.
- `21af6c6` - Implemented necklace fireball spawn logic.
- `e86d998` - Implemented necklace fireball keybind and packet support.
- `fa181bc` - Implemented necklace Speed and Jump Boost effects.
- `40d1c10` - Added the first Ghast Tear Necklace item implementation.
- `4f8e28c` - Added Nether Conduit mob drop loot.
- `dd0c376` - Added Nether Conduit placement to structure chests.
- `632f728` - Implemented Nether Conduit level upgrades with ancient debris.
- `ba007f9` - Implemented the Nether Conduit mob attack system.
- `a56cc24` - Implemented Nether Conduit lava movement help and Nether sleep support.

### 2026-04-18

- `bd58024` - Implemented Nether Conduit fire resistance and haste effects.
- `b1f0ed7` - Implemented Nether Conduit activation frame detection.
- `96c9f97` - Added Nether Conduit block and block-entity scaffolding.
- `554d2d0` - Added nether star drops to Evokers, Ravagers, Illusioners, and chests.
- `d8edf31` - Suppressed mob spawning in completed portal areas.
- `9884c2b` - Reduced wave 4 and 5 mob counts by roughly 25 percent.
- `d23ce5b` - Defined the original necklace accessory slot data.
- `4017891` - Added the original Accessories dependency experiment and build configuration.
- `568b98e` - Balanced raid waves and advancement behavior.
- `0c73b31` - Added cave, storm, and achievement changes.
- `8b694fd` - Overhauled music and raid activation.

### 2026-04-16

- `0ad9f8b` - Overhauled raid logic and weather overlay behavior.
- `280b029` - Tuned difficulty and fog systems.

### 2026-04-14

- `1ec8958` - Added more terrain and quality-of-life improvements.
- `331233c` - Adjusted mob difficulty and terrain behavior.
- `d953033` - Added a follow-up chunk-generation fix checkpoint.
- `57d7d36` - Fixed chunk-generation breakage.

### 2026-04-13

- `37b5534` - Continued pushing world generation and encounter polish.
- `2aa013a` - Added raid difficulty logic.
- `ff30f58` - Overhauled world generation.
- `f86d790` - Fixed world generation behavior.
- `920ce4e` - Sanitized documentation.
- `c96c9e1` - Prepared the project for jar packaging.

### 2026-04-12

- `abc48e3` - Added raid logic quality-of-life improvements.
- `0d661a7` - Added missing systems and quality-of-life fixes.
- `2416273` - Audited mob and raid logic.
- `452ae74` - Overhauled mobs and raids after the first major encounter build.

### 2026-04-07

- `40825dc` - Added dungeon mob spawning around the Piglin Illager Shaman work.

### 2026-04-06

- `b0685e9` - Audited Minecraft API usage and type safety.
- `852a43b` - Added the Piglin Illager custom entity family.
- `18a0879` - Added portal dungeon structure chest loot tables.
- `dc8f623` - Merged the decompile batch work into the project.
- `68b01d6` - Added the first portal dungeon structure and ruined portal replacement.
- `b236969` - Added the full decompiled reference source set.
- `46c63c4` - Added the remaining Minecraft reference sources.
- `881c390` - Added utility reference sources.
- `694ac1e` - Added network reference sources.
- `1a8a27c` - Added server reference sources.
- `ee238be` - Added remaining world gameplay reference sources.
- `56fc53f` - Added world entity reference sources.
- `c1936b0` - Added remaining world level reference sources.
- `56f0d0d` - Added world level-generation reference sources.
- `fea3f2b` - Added world level block reference sources.
- `59e800f` - Added remaining client support reference sources.
- `354f630` - Added client UI and model reference sources.
- `52d24de` - Added client renderer reference sources.
- `97e5a74` - Added Mojang support reference sources.
- `bdc0198` - Added CFR decompiler artifacts.
- `31bbda0` - Added the remaining Minecraft reference sources in the decompile batch branch.
- `437c98c` - Added utility reference sources in the decompile batch branch.
- `1713842` - Added network reference sources in the decompile batch branch.
- `a32539d` - Added server reference sources in the decompile batch branch.
- `336bc52` - Added remaining world gameplay reference sources in the decompile batch branch.
- `38d08a1` - Added world entity reference sources in the decompile batch branch.
- `00ba40c` - Added remaining world level reference sources in the decompile batch branch.
- `ad61b0a` - Added world level-generation reference sources in the decompile batch branch.
- `1240e9f` - Added world level block reference sources in the decompile batch branch.
- `f181766` - Added remaining client support reference sources in the decompile batch branch.
- `7f3327a` - Added client UI and model reference sources in the decompile batch branch.
- `cfed262` - Added client renderer reference sources in the decompile batch branch.
- `7de8d1f` - Added Mojang support reference sources in the decompile batch branch.
- `c3806d4` - Added CFR decompiler artifacts in the decompile batch branch.
- `b58a0af` - Scaffolded the Fabric mod baseline.

### 2026-04-05

- `ae09309` - Created the initial repository.
