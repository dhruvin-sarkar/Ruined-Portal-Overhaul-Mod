# Contributing

Thanks for helping polish Ruined Portal Overhaul.

## Build

Use Java 21, then run:

```powershell
$env:JAVA_HOME='C:\Users\dhruv\.codex\jdks\temurin-21'
./gradlew.bat build
```

If the local Windows wrapper hits a native launcher issue, run the wrapper jar directly with Java 21.

## Code Style

- Use Mojang mappings, not Yarn names.
- Keep common code free of `net.minecraft.client.*` imports.
- Keep server gameplay logic guarded to server-side worlds.
- Store persistent portal data in `PortalRaidState` through `server.overworld()`.
- Use translation keys for player-facing text.
- Keep `CLAUDE.md`, `SPEC.md`, and `README.md` aligned after behavior changes.

## Pull Requests

Open a pull request with:

- A short description of the gameplay or technical change.
- The commands you ran, especially `./gradlew.bat build`.
- Screenshots or logs for visual, structure, animation, command, or dedicated-server changes.
- Notes about save compatibility if persistent state changes.
