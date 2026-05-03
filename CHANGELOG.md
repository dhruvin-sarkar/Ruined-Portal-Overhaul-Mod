# Changelog

This changelog is reconstructed from the repository history back to the first commit on 2026-04-05. It focuses on shipped behavior and verified project state rather than raw commit prefixes.

## v1.0.0 - 2026-05-03

### Release Readiness

- Completed the release presentation layer with README, Modrinth description, contribution guide, issue templates, and this reconstructed changelog.
- Added English-valued localization skeletons for `de_de`, `es_es`, `fr_fr`, `ja_jp`, `pt_br`, `ru_ru`, and `zh_cn` so every shipped key has a translator-ready locale file.
- Added operator-only `/rpo` admin commands for portal location, status inspection, reset, direct wave spawning, instant completion, and Nether Dragon testing.
- Recorded dedicated-server dry start verification up to the expected unaccepted-EULA stop.
- Recorded client startup smoke verification through mod initialization, resource loading, recipe and advancement loading, biome modifications, and integrated-server startup.
- Reconciled `CLAUDE.md`, `SPEC.md`, `README.md`, and release-facing docs with the current live codebase.
- Verified the Java 21 Gradle build, static resource shape, language keys, sound subtitles, loot data, advancement triggers, GeckoLib assets, particles, Patchouli data, recipes, mixin references, and release artifact sanity.

### Portal Dungeons And World Generation

- Replaced overworld ruined portal encounters with large corrupted portal dungeon structures.
- Added a radius-136 corrupted surface scar, deterministic structure variants, protected ritual core, underground pits, worm-carved tunnels, special rooms, lava features, spawners, and loot caches.
- Added deterministic portal dungeon variants and persisted discovered variants in `PortalRaidState`.
- Added terrain-aware portal dungeon shaping, restored middle scar material sectors, added crying obsidian to outer scatter, and preserved vanilla Nether ruined portal placement.
- Hardened structure generation against chunk-boundary crashes and save-time scans.
- Ensured generated deep chests receive exactly one Nether Conduit while protecting normal generated loot.
- Added village spacing and data-side exclusion so portal dungeons do not generate on top of villages.
- Added Terralith skylands and cave biome exclusions and broadened cave coverage near release.
- Added structure rarity and outer scatter configuration support.

### Red Storm Atmosphere

- Added the red storm encounter atmosphere around portal zones with sky tinting, fog tinting, weather overlays, thunder, particles, ambience, and underground-compatible horizontal-distance checks.
- Limited storm weather overrides to the client world and stopped storm ambience on world unload.
- Added completed-portal low-level storm ambience so cleared portal scars remain visually distinct.
- Tuned storm pulse cadence to a slower organic cycle.
- Added intensity-scaled storm rumble and cleaned up payload states with explicit dimension guards.
- Reduced recurring atmosphere range cost and throttled refresh work in portal loops.

### Gold Tribute Raid

- Added the five-wave Gold Tribute Raid with proximity activation, no Bad Omen requirement, custom wave pacing, boss bar tracking, inter-wave delays, and completion rewards.
- Added wave escalation, later balancing passes, and roughly 25 percent lower wave 4 and 5 counts after playability review.
- Added raid restart recovery, restored saved wave delays after restart, and resumed pre-wave raids at the first real wave.
- Staggered raid completion into readable reward beats: portal lighting, spawner disable, boss chest, Exiled Piglin Trader, and final feedback.
- Expanded wave spawn retries outward when ring positions are blocked.
- Used squared distance checks in ambient spawn loops and guarded ambient spawning behind player proximity and configuration.
- Detached disconnecting players from active boss bars and rebuilt boss bar viewer tracking around the portal radius.
- Kept raid completion synced with the boss bar radius and post-raid suppression state.

### Custom Mobs And GeckoLib

- Added the Piglin Illager combat family, including Pillager, Vindicator, Brute Pillager, Evoker, Illusioner, Vex, and Ravager variants.
- Added GeckoLib 5.4.5 and moved custom mob rendering onto GeckoLib model and animation assets.
- Added synced texture variants for custom mobs.
- Added custom mob sound routing, voice pitch tuning, attack triggers, Vex flying idle, ravager roar animation, melee swing timing matched to GeckoLib clips, and keyframe cues for encounter animations.
- Added Piglin Pillager crossbow kiting, safer brute priority, limited-life Piglin Vex behavior, and portal-anchored Phase 2 guardians.
- Added generated entity texture variants for visual variety without external art dependencies.

### Rewards, Items, And Progression

- Added Portal Shard discovery, Nether Conduit progression, Ghast Tear Necklace fireballs, Corrupted Netherite armor rewards, Shard of the Nether, Corrupted Portal Key, Nether Tide music disc, and Nether Dragon Scale trophy.
- Switched Portal Shard targeting to saved portal state instead of sampled world positions.
- Added player-facing tooltips and localized named loot artifacts.
- Added loot table improvements across surface, deep, boss, and entity rewards.
- Added nether star drops to key raid mobs and chests so the Nether Crystal progression loop is attainable.
- Limited boss chest netherite ingots and aligned enchantment loot functions with the current vanilla format.
- Made named loot rewards replace existing names cleanly.
- Clarified Dragon Scale as an intentional trophy/material item while Accessories compatibility remains blocked for Minecraft 1.21.11.

### Nether Conduit

- Added the Nether Conduit block, block entity, activation frame detection, support effects, lava movement help, Nether sleep support, nether mob attack system, upgrade levels, chest placement, and mob drop path.
- Moved the Nether Conduit into a GeckoLib animated block renderer with active/inactive render state, inner glow, and synced blockstate activation.
- Tightened the activation ring to the documented 12-nether-brick flat horizontal frame.
- Preserved upgraded conduits when broken.
- Cleared conduit lava movement boost state when the server stops.
- Capped and documented conduit attack/effect behavior and status messaging.

### Ghast Tear Necklace

- Added the Ghast Tear Necklace as a native carried charm after removing the unverified Accessories dependency path.
- Added Speed and Jump Boost passive effects, persistent fireball cooldown state, server-validated C2S fireball packet handling, and client keybind support.
- Validated necklace fireball direction against the server look direction while preserving the keypress aim vector through the payload.
- Rejected fireball packets from dead or spectating players.
- Released Exiled Piglin trade locks on player disconnect and cleared stale trade locks when entities load after restart.

### Nether Crystal Ritual And Nether Dragon

- Added Nether Crystal item placement, GeckoLib Nether Crystal entity animation, generated pedestal placement, ritual detection, summoning sequence, and ritual recovery.
- Let pre-staged crystals start the dragon ritual and stopped disabled pedestals from consuming crystals.
- Ignored invalid ritual pedestal state writes and kept ritual cue titles aligned to the portal footprint.
- Added the Nether Dragon as a portal-anchored boss based on vanilla dragon combat semantics while suppressing End fight output.
- Added dragon summoning, portal-centered fight radius, phase-two enragement, enraged flight boosts, Nether Slam, breath aura, boss bar tracking, death finale, loot drops, XP behavior, and advancement rewards.
- Suppressed nearby vanilla End Crystals so Overworld crystal healing cannot affect the Nether Dragon.
- Kept the Nether Dragon anchored to the portal arena instead of drifting toward overworld origin.
- Prevented double damage from Nether Slam and kept the slam non-griefing.
- Dropped dragon death loot at the portal ring instead of the corpse location.
- Kept active dragon locks while chunks are unloaded and gave dragon fight credit across the full arena.

### Advancements, Text, Audio, And Discovery

- Added advancement milestones for conduit use, necklace carry/fireball use, crystal ritual progress, raid completion, dragon defeat, and late-game rewards.
- Localized advancement display text, raid and conduit status messages, named artifacts, item names, sound subtitles, and release-facing player text.
- Added mod-owned sound events for raid, conduit, ritual, dragon, storm, music, and reward moments.
- Made custom sound fallbacks play correctly and routed encounter sounds through the mod sound event registry.
- Added optional REI progression pages and corrected REI recipe hints.
- Added optional Patchouli guide data and fixed Patchouli guide loading.
- Removed internal draft wording from release text before closeout.

### Compatibility, Configuration, And Safety

- Added Cloth Config and ModMenu support with grouped options, live gameplay tuning, validation, and clear pack-tuning tooltips.
- Removed dead configuration options and aligned structure rarity, combat scaling, scatter, nether star drops, and portal biome rules with real runtime behavior.
- Guarded optional REI displays and kept the project free of unverified Accessories API runtime dependency.
- Added post-raid natural spawn suppression for completed portal areas, with a config toggle.
- Added safe persistent-state codec defaults for older saves and restart safety.
- Cleared Exiled Piglin trade locks across disconnect/reload paths.
- Repaired dragon ritual recovery, save-and-exit chunk scanning, and portal scans during save.
- Kept common code dedicated-server safe by moving client-only rendering into client entrypoints and mixins.

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
