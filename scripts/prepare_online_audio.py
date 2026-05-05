from __future__ import annotations

import subprocess
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOUND_ROOT = ROOT / "src/main/resources/assets/ruined_portal_overhaul/sounds"
ATTRIBUTION = ROOT / "assets/audio_sources/ONLINE_AUDIO_ATTRIBUTION.md"


@dataclass(frozen=True)
class Source:
    key: str
    author: str
    license: str
    page: str
    local_path: str


@dataclass(frozen=True)
class Clip:
    target: str
    source: str
    start: float = 0.0
    duration: float | None = None
    gain: float = 1.0


SOURCES = {
    "oga_dungeon_ambient": Source(
        "oga_dungeon_ambient",
        "JaggedStone",
        "CC0",
        "https://opengameart.org/content/loopable-dungeon-ambience",
        "assets/audio_sources/opengameart_cc0/dungeon_ambient_1.ogg",
    ),
    "oga_dark_cavern": Source(
        "oga_dark_cavern",
        "Paul Wortmann",
        "CC0",
        "https://opengameart.org/content/dark-cavern-ambient",
        "assets/audio_sources/opengameart_cc0/dark_cavern_ambient_002.ogg",
    ),
    "oga_lost": Source(
        "oga_lost",
        "congusbongus",
        "CC0",
        "https://opengameart.org/content/lost-in-a-bad-place-horror-ambience-loop",
        "assets/audio_sources/opengameart_cc0/lost_1.ogg",
    ),
    "oga_roar": Source(
        "oga_roar",
        "trazzz123",
        "CC0",
        "https://opengameart.org/content/cc0-deep-monster-roar",
        "assets/audio_sources/opengameart_cc0/monster_roar.wav",
    ),
    "oga_low_roars": Source(
        "oga_low_roars",
        "Darsycho",
        "CC0",
        "https://opengameart.org/content/big-scary-troll-sounds",
        "assets/audio_sources/opengameart_cc0/troll-roars.ogg",
    ),
    "kenney_sci_fi": Source(
        "kenney_sci_fi",
        "Kenney",
        "CC0",
        "https://kenney.nl/assets/sci-fi-sounds",
        "assets/audio_sources/kenney_online/sci_fi",
    ),
    "kenney_impact": Source(
        "kenney_impact",
        "Kenney",
        "CC0",
        "https://kenney.nl/assets/impact-sounds",
        "assets/audio_sources/kenney_online/impact",
    ),
    "kenney_rpg": Source(
        "kenney_rpg",
        "Kenney",
        "CC0",
        "https://kenney.nl/assets/rpg-audio",
        "assets/audio_sources/kenney_online/rpg",
    ),
    "kenney_interface": Source(
        "kenney_interface",
        "Kenney",
        "CC0",
        "https://kenney.nl/assets/interface-sounds",
        "assets/audio_sources/kenney_online/interface",
    ),
    "kenney_jingles": Source(
        "kenney_jingles",
        "Kenney",
        "CC0",
        "https://kenney.nl/assets/music-jingles",
        "assets/audio_sources/kenney_online/jingles",
    ),
}


CLIPS = [
    Clip("weather/red_storm_music.ogg", "oga_dark_cavern", 0, 30, 0.95),
    Clip("weather/red_storm_rumble.ogg", "oga_dungeon_ambient", 8, 12, 0.95),
    Clip("weather/red_thunder_crack.ogg", "kenney_sci_fi:explosionCrunch_004.ogg", 0, None, 1.35),
    Clip("weather/red_thunder_low.ogg", "kenney_sci_fi:lowFrequency_explosion_000.ogg", 0, None, 1.35),
    Clip("weather/red_thunder_portal.ogg", "kenney_sci_fi:explosionCrunch_003.ogg", 0, None, 1.3),
    Clip("ambient/portal_lava.ogg", "kenney_sci_fi:thrusterFire_004.ogg", 0, 5, 0.8),
    Clip("ambient/portal_ghast.ogg", "oga_low_roars", 2, 5, 0.85),
    Clip("block/nether_conduit_ambient.ogg", "kenney_sci_fi:spaceEngineLow_003.ogg", 0, 5, 0.85),
    Clip("block/nether_conduit_activate.ogg", "kenney_sci_fi:forceField_004.ogg", 0, None, 1.15),
    Clip("block/nether_conduit_deactivate.ogg", "kenney_sci_fi:forceField_001.ogg", 0, None, 0.9),
    Clip("entity/piglin_ambient.ogg", "oga_low_roars", 0, 1.4, 0.8),
    Clip("entity/piglin_hurt.ogg", "oga_low_roars", 4.5, 0.8, 1.05),
    Clip("entity/piglin_death.ogg", "oga_low_roars", 2.0, 1.6, 1.05),
    Clip("entity/piglin_crossbow.ogg", "kenney_rpg:drawKnife3.ogg", 0, None, 1.05),
    Clip("entity/piglin_axe_hit.ogg", "kenney_impact:impactMining_003.ogg", 0, None, 1.15),
    Clip("entity/piglin_brute_ambient.ogg", "oga_low_roars", 4.4, 1.5, 0.95),
    Clip("entity/piglin_spell_shot.ogg", "kenney_sci_fi:laserLarge_003.ogg", 0, None, 0.85),
    Clip("entity/piglin_evoker_cast.ogg", "kenney_sci_fi:forceField_002.ogg", 0, None, 1.0),
    Clip("entity/piglin_ravager_ambient.ogg", "oga_low_roars", 0, 2.2, 1.05),
    Clip("entity/piglin_ravager_hurt.ogg", "oga_roar", 0.4, 1.0, 1.05),
    Clip("entity/piglin_ravager_death.ogg", "oga_roar", 1.0, 2.4, 1.05),
    Clip("entity/piglin_ravager_roar.ogg", "oga_roar", 0, 2.2, 1.18),
    Clip("entity/piglin_vex_ambient.ogg", "kenney_sci_fi:laserSmall_002.ogg", 0, None, 0.55),
    Clip("entity/piglin_vex_hurt.ogg", "kenney_sci_fi:laserRetro_001.ogg", 0, None, 0.55),
    Clip("entity/piglin_vex_death.ogg", "kenney_sci_fi:laserRetro_004.ogg", 0, None, 0.58),
    Clip("entity/exiled_piglin_ambient.ogg", "kenney_rpg:creak2.ogg", 0, None, 0.8),
    Clip("entity/nether_dragon_ambient.ogg", "oga_roar", 0, 3.2, 0.95),
    Clip("entity/nether_dragon_growl.ogg", "oga_low_roars", 0, 2.6, 1.18),
    Clip("entity/nether_dragon_phase2.ogg", "kenney_sci_fi:lowFrequency_explosion_001.ogg", 0, None, 1.4),
    Clip("item/ghast_tear_necklace_fireball.ogg", "kenney_sci_fi:laserLarge_004.ogg", 0, None, 0.85),
    Clip("item/portal_shard_locate.ogg", "kenney_interface:select_006.ogg", 0, 1.2, 0.7),
    Clip("music/disc_nether_tide.ogg", "oga_lost", 0, None, 0.9),
    Clip("raid/approach.ogg", "oga_dungeon_ambient", 0, 4, 0.75),
    Clip("raid/start_sting.ogg", "kenney_jingles:Hit jingles/jingles_HIT15.ogg", 0, None, 1.1),
    Clip("raid/wave_complete.ogg", "kenney_jingles:Steel jingles/jingles_STEEL07.ogg", 0, None, 0.85),
    Clip("raid/complete.ogg", "kenney_jingles:Steel jingles/jingles_STEEL14.ogg", 0, None, 0.95),
    Clip("ritual/victory.ogg", "kenney_jingles:Pizzicato jingles/jingles_PIZZI07.ogg", 0, None, 0.85),
    Clip("ritual/crystal_place.ogg", "kenney_sci_fi:forceField_003.ogg", 0, None, 0.92),
    Clip("ritual/dragon_summon.ogg", "kenney_sci_fi:spaceEngineLarge_004.ogg", 0, 5, 1.05),
    Clip("ritual/pedestal_shatter.ogg", "kenney_impact:impactMetal_heavy_004.ogg", 0, None, 1.15),
]


def source_path(clip_source: str) -> Path:
    if ":" not in clip_source:
        return ROOT / SOURCES[clip_source].local_path
    source_key, relative = clip_source.split(":", 1)
    return ROOT / SOURCES[source_key].local_path / Path(relative)


def source_key(clip_source: str) -> str:
    return clip_source.split(":", 1)[0]


def convert_clip(clip: Clip) -> None:
    src = source_path(clip.source)
    if not src.exists():
        raise FileNotFoundError(f"Missing source audio: {src}")
    target = SOUND_ROOT / clip.target
    target.parent.mkdir(parents=True, exist_ok=True)

    filters = [
        f"volume={clip.gain}",
        "afade=t=in:st=0:d=0.015",
        "loudnorm=I=-18:TP=-1.5:LRA=11",
    ]
    if clip.duration is not None and clip.duration > 0.08:
        filters.append(f"afade=t=out:st={max(0.0, clip.duration - 0.05):.3f}:d=0.05")

    command = ["ffmpeg", "-hide_banner", "-loglevel", "error", "-y"]
    if clip.start:
        command.extend(["-ss", f"{clip.start:.3f}"])
    command.extend(["-i", str(src)])
    if clip.duration is not None:
        command.extend(["-t", f"{clip.duration:.3f}"])
    command.extend(["-af", ",".join(filters), "-c:a", "libvorbis", "-q:a", "5", str(target)])
    subprocess.run(command, check=True)


def write_attribution() -> None:
    used = {source_key(clip.source) for clip in CLIPS}
    lines = [
        "# Online Audio Attribution",
        "",
        "All shipped Ruined Portal Overhaul sound files in `src/main/resources/assets/ruined_portal_overhaul/sounds/` are edited from online free sound packs listed below. Edits are limited to trimming, gain staging, fades, normalization, and OGG conversion; no procedural or synthesized sound generation is used for the final shipped audio.",
        "",
        "## Sources",
        "",
    ]
    for key in sorted(used):
        source = SOURCES[key]
        lines.append(f"- {source.author} - {source.page} - {source.license}")
    lines.extend(["", "## Output Map", ""])
    for clip in CLIPS:
        source = SOURCES[source_key(clip.source)]
        lines.append(f"- `{clip.target}` <- {source.author}, {source.license}, {source.page}")
    lines.append("")
    ATTRIBUTION.write_text("\n".join(lines), encoding="utf-8")


def main() -> None:
    for clip in CLIPS:
        convert_clip(clip)
    write_attribution()
    print(f"Prepared {len(CLIPS)} online-sourced OGG files")


if __name__ == "__main__":
    main()
