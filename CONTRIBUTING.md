# Contributing

Thanks for taking the time to work on Ruined Portal Overhaul. This project has a lot of moving pieces for one mod: world generation, persistent raid state, custom entities, GeckoLib rendering, networking, loot, sounds, particles, optional integrations, and release docs. Small, well-tested changes are much easier to review than giant cleanups.

## Before You Start

Work from the live source tree, the changelog, and the behavior you can actually verify in-game. Planning notes can drift behind the code, so treat the current implementation as the source of truth before changing systems that are already wired up.

## Local Setup

The project targets:

- Minecraft `1.21.11`
- Fabric Loader `0.18.6`
- Fabric API `0.141.3+1.21.11`
- GeckoLib `5.4.5`
- Java `21`
- Mojang official mappings

On Windows, point Gradle at Java 21 before building:

```powershell
$env:JAVA_HOME='C:\Users\dhruv\.codex\jdks\temurin-21'
./gradlew.bat build
```

If the Windows wrapper itself fails before Gradle starts with a native launcher error, run the wrapper jar directly with Java 21:

```powershell
$env:JAVA_HOME='C:\Users\dhruv\.codex\jdks\temurin-21'
& "$env:JAVA_HOME\bin\java.exe" @(
  '-Dorg.gradle.workers.max=1',
  '-Dorg.gradle.jvmargs=-Xmx768m',
  '-jar',
  '.\gradle\wrapper\gradle-wrapper.jar',
  'build',
  '--no-daemon',
  '--max-workers=1'
)
```

## What To Test

Always run the build for code or resource changes:

```powershell
./gradlew.bat build
```

For gameplay changes, also test the smallest in-game path that proves the change:

- Worldgen changes: inspect at least a flat biome, a slope/cliff, and a water edge.
- Raid changes: run `/rpo wave <1-5>` for the touched wave, then a normal raid start if timing changed.
- Persistent state changes: save, quit, reload, and verify the state still makes sense.
- Client/rendering changes: check singleplayer and think about dedicated-server class loading.
- Loot changes: validate JSON and open the relevant chest or kill the relevant mob in-game.
- Creative inventory changes: check the custom tab and the expected vanilla tab.

The `/rpo` commands are there for testing and server administration. Use them.

## Code Guidelines

- Use Mojang mapping names. Do not paste Yarn names into this codebase.
- Keep common code free of `net.minecraft.client.*` imports.
- Guard server gameplay logic with the right server-side world/entity checks.
- Use `PortalRaidState` through `server.overworld()` for shared portal progress.
- Call `setDirty()` immediately after persistent state mutations.
- Keep structure generation bounded to the current piece and `chunkBox`.
- Do not force-load chunks from runtime portal scans.
- Prefer squared distance checks in tick-rate paths.
- Use translation keys for anything players can see.
- Keep optional integrations guarded. Patchouli, REI, Cloth Config, and ModMenu should not become hard requirements unless the dependency list deliberately changes.
- Do not reintroduce Accessories until a real Minecraft `1.21.11` compatible build is verified.

## Worldgen Notes

Portal terrain is intentionally custom-sculpted. The structure JSON uses `terrain_adaptation: none` because vanilla adaptation is too coarse for the radius-136 scar. If you touch `PortalStructureHelper` or `PortalDungeonPiece`, pay special attention to:

- Terrain blending at the outer scar edge.
- Water, lava, shorelines, and ocean floors.
- Slopes and exposed cliff faces.
- Chunk-boundary safety.
- Chest placement and block-entity access.
- Pit readability from the surface into the cave system.

Worldgen bugs are often silent until a specific biome or chunk boundary exposes them, so test awkward terrain, not only plains.

## Resources And Text

For JSON/resource edits:

- Use singular resource folders such as `loot_table/` and `advancement/`.
- Keep loot conditions and functions namespaced.
- Keep item models in `assets/ruined_portal_overhaul/models/item/`.
- Add language keys to `en_us.json` and all locale stub files.
- Add subtitle keys for new sound events.
- Document bundled online audio in `assets/audio_sources/ONLINE_AUDIO_ATTRIBUTION.md`.

Avoid hardcoded English in Java unless it is strictly debug-only and not shown to players.

## Documentation

When behavior changes, update the tracked public docs in the same pull request. At minimum, keep `CHANGELOG.md` current. If the change touches bundled sound assets, also update `assets/audio_sources/ONLINE_AUDIO_ATTRIBUTION.md`.

Keep the wording plain. Describe what changed, why it matters, and what was tested.

## Pull Request Checklist

Before opening a pull request, include:

- What changed.
- Why it changed.
- What commands were run.
- What was tested in-game, if applicable.
- Screenshots or logs for visual, worldgen, rendering, command, or dedicated-server changes.
- Notes about save compatibility if `PortalRaidState`, NBT, codecs, or entity persistence changed.

A good pull request is easy to replay. Someone else should be able to read it, run the same checks, and understand the risk.

## Bug Reports

Useful bug reports include:

- Minecraft version.
- Fabric Loader and Fabric API versions.
- Mod version or commit hash.
- Other worldgen/gameplay mods installed.
- Seed, coordinates, and biome for worldgen issues.
- Steps to reproduce.
- Expected behavior and actual behavior.
- Latest log or crash report when relevant.

For terrain issues, screenshots from a few angles help a lot. Include the portal location and whether the issue was on flat ground, water, a slope, or a cliff.
