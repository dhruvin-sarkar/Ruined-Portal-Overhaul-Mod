from __future__ import annotations

import json
import math
import shutil
import subprocess
import tempfile
from collections import OrderedDict
from pathlib import Path

import numpy as np
from scipy.io import wavfile


ROOT = Path(__file__).resolve().parents[1]
MOD_ID = "ruined_portal_overhaul"
SAMPLE_RATE = 44_100
SOUNDS_JSON = ROOT / "src/main/resources/assets/ruined_portal_overhaul/sounds.json"
SOUNDS_DIR = ROOT / "src/main/resources/assets/ruined_portal_overhaul/sounds"
AUDIO_SOURCE_DIR = ROOT / "assets/audio_sources/kenney_rpg_audio"

FOLEY_SOURCE_FILES = {
    "chop": "chop.ogg",
    "creak": "creak2.ogg",
    "draw_knife_1": "drawKnife1.ogg",
    "coins": "handleCoins.ogg",
    "knife_slice_2": "knifeSlice2.ogg",
    "metal_click": "metalClick.ogg",
    "metal_latch": "metalLatch.ogg",
}


EVENT_TO_FILE = OrderedDict(
    {
        "weather.red_storm.music": "weather/red_storm_music",
        "weather.red_storm.rumble": "weather/red_storm_rumble",
        "weather.red_thunder": "weather/red_thunder_crack",
        "weather.red_thunder_low": "weather/red_thunder_low",
        "weather.red_thunder_portal": "weather/red_thunder_portal",
        "ambient.portal_lava": "ambient/portal_lava",
        "ambient.portal_ghast": "ambient/portal_ghast",
        "block.nether_conduit.ambient": "block/nether_conduit_ambient",
        "block.nether_conduit.activate": "block/nether_conduit_activate",
        "block.nether_conduit.deactivate": "block/nether_conduit_deactivate",
        "entity.piglin_pillager.ambient": "entity/piglin_ambient",
        "entity.piglin_pillager.hurt": "entity/piglin_hurt",
        "entity.piglin_pillager.death": "entity/piglin_death",
        "entity.piglin_pillager.attack": "entity/piglin_crossbow",
        "entity.piglin_vindicator.ambient": "entity/piglin_ambient",
        "entity.piglin_vindicator.hurt": "entity/piglin_hurt",
        "entity.piglin_vindicator.death": "entity/piglin_death",
        "entity.piglin_vindicator.attack": "entity/piglin_axe_hit",
        "entity.piglin_brute_pillager.ambient": "entity/piglin_brute_ambient",
        "entity.piglin_brute_pillager.hurt": "entity/piglin_hurt",
        "entity.piglin_brute_pillager.death": "entity/piglin_death",
        "entity.piglin_brute_pillager.attack": "entity/piglin_crossbow",
        "entity.piglin_illusioner.ambient": "entity/piglin_ambient",
        "entity.piglin_illusioner.hurt": "entity/piglin_hurt",
        "entity.piglin_illusioner.death": "entity/piglin_death",
        "entity.piglin_illusioner.attack": "entity/piglin_spell_shot",
        "entity.piglin_evoker.ambient": "entity/piglin_ambient",
        "entity.piglin_evoker.hurt": "entity/piglin_hurt",
        "entity.piglin_evoker.death": "entity/piglin_death",
        "entity.piglin_evoker.cast_spell": "entity/piglin_evoker_cast",
        "entity.piglin_ravager.ambient": "entity/piglin_ravager_ambient",
        "entity.piglin_ravager.hurt": "entity/piglin_ravager_hurt",
        "entity.piglin_ravager.death": "entity/piglin_ravager_death",
        "entity.piglin_ravager.roar": "entity/piglin_ravager_roar",
        "entity.piglin_vex.ambient": "entity/piglin_vex_ambient",
        "entity.piglin_vex.hurt": "entity/piglin_vex_hurt",
        "entity.piglin_vex.death": "entity/piglin_vex_death",
        "entity.exiled_piglin.ambient": "entity/exiled_piglin_ambient",
        "entity.exiled_piglin.hurt": "entity/piglin_hurt",
        "entity.exiled_piglin.death": "entity/piglin_death",
        "entity.nether_dragon.ambient": "entity/nether_dragon_ambient",
        "entity.nether_dragon.growl": "entity/nether_dragon_growl",
        "entity.nether_dragon.phase2": "entity/nether_dragon_phase2",
        "item.ghast_tear_necklace.fireball": "item/ghast_tear_necklace_fireball",
        "item.portal_shard.locate": "item/portal_shard_locate",
        "music.disc.nether_tide": "music/disc_nether_tide",
        "raid.approach": "raid/approach",
        "raid.start": "raid/start_sting",
        "raid.wave_complete": "raid/wave_complete",
        "raid.complete": "raid/complete",
        "ritual.victory": "ritual/victory",
        "ritual.crystal_place": "ritual/crystal_place",
        "ritual.dragon_summon": "ritual/dragon_summon",
        "ritual.pedestal_shatter": "ritual/pedestal_shatter",
    }
)

STREAMED_EVENTS = {"weather.red_storm.music", "music.disc.nether_tide"}


def timeline(duration: float) -> np.ndarray:
    return np.linspace(0.0, duration, int(SAMPLE_RATE * duration), endpoint=False)


def sine(freq: float | np.ndarray, duration: float, phase: float = 0.0) -> np.ndarray:
    t = timeline(duration)
    if isinstance(freq, np.ndarray):
        phase_curve = 2.0 * np.pi * np.cumsum(freq) / SAMPLE_RATE
        return np.sin(phase_curve + phase)
    return np.sin(2.0 * np.pi * freq * t + phase)


def env(audio: np.ndarray, attack: float = 0.01, release: float = 0.08) -> np.ndarray:
    length = audio.size
    attack_samples = min(length, int(SAMPLE_RATE * attack))
    release_samples = min(length, int(SAMPLE_RATE * release))
    envelope = np.ones(length)
    if attack_samples > 0:
        envelope[:attack_samples] = np.linspace(0.0, 1.0, attack_samples)
    if release_samples > 0:
        envelope[-release_samples:] *= np.linspace(1.0, 0.0, release_samples)
    return audio * envelope


def decay(duration: float, amount: float = 6.0) -> np.ndarray:
    return np.exp(-timeline(duration) * amount)


def noise(duration: float, seed: int) -> np.ndarray:
    return np.random.default_rng(seed).uniform(-1.0, 1.0, int(SAMPLE_RATE * duration))


def smooth(audio: np.ndarray, width: int = 9) -> np.ndarray:
    kernel = np.ones(width) / width
    return np.convolve(audio, kernel, mode="same")


def normalize(audio: np.ndarray, target: float = 0.82) -> np.ndarray:
    peak = float(np.max(np.abs(audio))) if audio.size else 0.0
    if peak < 1.0e-6:
        return audio
    return np.clip(audio / peak * target, -1.0, 1.0)


def soft_clip(audio: np.ndarray, drive: float = 1.3) -> np.ndarray:
    return np.tanh(audio * drive) / np.tanh(drive)


def overlay(*layers: np.ndarray) -> np.ndarray:
    length = max(layer.size for layer in layers)
    mixed = np.zeros(length)
    for layer in layers:
        mixed[: layer.size] += layer
    return mixed


def lowpass(audio: np.ndarray, width: int = 19) -> np.ndarray:
    return smooth(audio, width) if width > 1 else audio


def highpass(audio: np.ndarray, width: int = 41) -> np.ndarray:
    return audio - lowpass(audio, width)


def fit(audio: np.ndarray, duration: float, gain: float = 1.0, offset: float = 0.0) -> np.ndarray:
    length = int(SAMPLE_RATE * duration)
    result = np.zeros(length)
    start = min(length, max(0, int(SAMPLE_RATE * offset)))
    available = length - start
    if available <= 0:
        return result
    clip = audio[:available]
    result[start : start + clip.size] = clip * gain
    return result


def chord(duration: float, freqs: tuple[float, ...], gain: float = 1.0) -> np.ndarray:
    tones = [sine(freq, duration, i * 0.73) for i, freq in enumerate(freqs)]
    return gain * sum(tones) / len(tones)


def chirp(duration: float, start: float, end: float) -> np.ndarray:
    freq = np.linspace(start, end, int(SAMPLE_RATE * duration))
    return sine(freq, duration)


def rumble(duration: float, seed: int = 1) -> np.ndarray:
    t = timeline(duration)
    base = 0.34 * sine(82.0, duration) + 0.28 * sine(101.0, duration, 0.4) + 0.18 * sine(128.0, duration, 1.1)
    tremolo = 0.72 + 0.28 * np.sin(2.0 * np.pi * 0.38 * t)
    grit = smooth(noise(duration, seed), 81) * 0.2
    return normalize(env((base + grit) * tremolo, 0.08, 0.14), 0.72)


def thunder(duration: float, seed: int, low: bool = False) -> np.ndarray:
    t = timeline(duration)
    impact = noise(duration, seed) * np.exp(-t * (6.0 if low else 8.5))
    body = sine(56.0 if low else 74.0, duration) * np.exp(-t * 2.4)
    crack = smooth(noise(duration, seed + 20), 3) * np.exp(-t * 13.0)
    roll = np.zeros_like(t)
    for delay, gain, freq in ((0.16, 0.34, 48.0), (0.32, 0.22, 39.0), (0.54, 0.13, 32.0)):
        delayed_duration = max(0.01, duration - delay)
        delayed = sine(freq, delayed_duration) * decay(delayed_duration, 2.1)
        roll += fit(delayed, duration, gain, delay)
    mixed = 0.62 * impact + 0.45 * body + roll + (0.3 if not low else 0.08) * crack
    return normalize(soft_clip(env(mixed, 0.002, 0.16), 1.6), 0.9)


def portal_thunder() -> np.ndarray:
    duration = 1.1
    return normalize(env(0.6 * chirp(duration, 96, 42) + 0.35 * rumble(duration, 3), 0.01, 0.18), 0.76)


def conduit_hum() -> np.ndarray:
    duration = 3.0
    t = timeline(duration)
    pulse = 0.65 + 0.35 * np.sin(2.0 * np.pi * 0.55 * t)
    tone = chord(duration, (212.0, 247.0, 286.0), 0.62) + 0.18 * sine(424.0, duration)
    return normalize(env(tone * pulse, 0.05, 0.08), 0.64)


def sweep(duration: float, start: float, end: float, seed: int) -> np.ndarray:
    body = chirp(duration, start, end)
    sparkle = smooth(noise(duration, seed), 5) * decay(duration, 7.0)
    return normalize(env(0.78 * body + 0.22 * sparkle, 0.006, 0.1), 0.82)


def raid_sting() -> np.ndarray:
    duration = 1.5
    t = timeline(duration)
    build = np.clip(t / duration, 0.0, 1.0)
    low = chord(duration, (54.0, 64.0, 81.0), 0.72) * (0.45 + 0.55 * build)
    blade = chirp(duration, 160.0, 92.0) * np.exp(-np.maximum(0.0, t - 1.0) * 9.0)
    hit = noise(duration, 5) * np.exp(-np.maximum(0.0, t - 1.18) * 16.0) * (t > 1.18)
    return normalize(env(low + 0.25 * blade + 0.22 * hit, 0.04, 0.18), 0.84)


def wave_complete() -> np.ndarray:
    duration = 0.8
    return normalize(env(0.55 * chirp(duration, 330.0, 180.0) + 0.32 * chord(duration, (130.0, 155.0), 1.0), 0.01, 0.12), 0.78)


def completion_tone(duration: float = 1.2) -> np.ndarray:
    return normalize(env(chord(duration, (196.0, 247.0, 294.0), 0.68) + 0.2 * chirp(duration, 520.0, 780.0), 0.03, 0.18), 0.78)


def crystal_place() -> np.ndarray:
    duration = 0.42
    ring = chord(duration, (392.0, 466.0, 622.0), 0.52) * decay(duration, 2.2)
    shadow = chirp(duration, 210.0, 150.0) * 0.42
    return normalize(env(ring + shadow, 0.006, 0.08), 0.78)


def shatter() -> np.ndarray:
    duration = 0.75
    t = timeline(duration)
    shards = smooth(noise(duration, 18), 2) * np.exp(-t * 8.0)
    low = sine(72.0, duration) * np.exp(-t * 3.2)
    return normalize(env(0.65 * shards + 0.45 * low, 0.002, 0.14), 0.88)


def piglin_voice(duration: float, base: float, seed: int, bite: float = 0.2) -> np.ndarray:
    t = timeline(duration)
    wobble = base + 18.0 * np.sin(2.0 * np.pi * 7.0 * t)
    throat = sine(wobble, duration) + 0.45 * sine(wobble * 0.5, duration, 0.9)
    rasp = smooth(noise(duration, seed), 4) * bite
    return normalize(env((throat + rasp) * decay(duration, 1.7), 0.01, 0.08), 0.78)


def hit(duration: float, seed: int) -> np.ndarray:
    t = timeline(duration)
    transient = smooth(noise(duration, seed), 3) * np.exp(-t * 18.0)
    thud = sine(95.0, duration) * np.exp(-t * 7.5)
    return normalize(env(0.62 * transient + 0.45 * thud, 0.001, 0.07), 0.86)


def crossbow() -> np.ndarray:
    duration = 0.28
    t = timeline(duration)
    snap = noise(duration, 31) * np.exp(-t * 24.0)
    string = chirp(duration, 720.0, 230.0) * np.exp(-t * 9.0)
    return normalize(env(0.4 * snap + 0.6 * string, 0.001, 0.04), 0.82)


def spell(duration: float = 0.62) -> np.ndarray:
    return normalize(env(0.48 * chirp(duration, 260.0, 520.0) + 0.45 * chord(duration, (311.0, 415.0, 554.0), 1.0), 0.015, 0.12), 0.76)


def ravager_roar() -> np.ndarray:
    duration = 1.15
    t = timeline(duration)
    throat = sine(70.0 + 11.0 * np.sin(2.0 * np.pi * 5.5 * t), duration)
    grit = smooth(noise(duration, 44), 5)
    return normalize(env((0.7 * throat + 0.38 * grit) * np.exp(-t * 0.65), 0.02, 0.16), 0.88)


def dragon_growl(duration: float, seed: int = 52) -> np.ndarray:
    t = timeline(duration)
    throat = sine(43.0 + 8.0 * np.sin(2.0 * np.pi * 3.0 * t), duration)
    upper = sine(86.0 + 14.0 * np.sin(2.0 * np.pi * 4.0 * t), duration, 0.5)
    grit = smooth(noise(duration, seed), 6)
    return normalize(env((0.62 * throat + 0.34 * upper + 0.24 * grit) * np.exp(-t * 0.55), 0.03, 0.2), 0.9)


def disc() -> np.ndarray:
    duration = 16.0
    t = timeline(duration)
    pulse = 0.7 + 0.3 * np.sin(2.0 * np.pi * 0.5 * t)
    drone = chord(duration, (73.42, 87.31, 110.0, 146.83), 0.58) * pulse
    bell = chord(duration, (293.66, 369.99), 0.16) * (0.5 + 0.5 * np.sin(2.0 * np.pi * 0.125 * t))
    return normalize(env(drone + bell + 0.04 * smooth(noise(duration, 77), 31), 0.08, 0.4), 0.68)


def load_source_ogg(source: Path, temp_dir: Path) -> np.ndarray:
    if not source.exists():
        raise SystemExit(f"missing CC0 foley source: {source}")
    wav_path = temp_dir / f"{source.stem}.wav"
    subprocess.run(
        [
            "ffmpeg",
            "-y",
            "-loglevel",
            "error",
            "-i",
            str(source),
            "-ac",
            "1",
            "-ar",
            str(SAMPLE_RATE),
            str(wav_path),
        ],
        check=True,
    )
    _, data = wavfile.read(wav_path)
    audio = np.asarray(data, dtype=np.float32)
    if audio.ndim > 1:
        audio = audio.mean(axis=1)
    if audio.size == 0:
        return audio
    audio /= max(float(np.max(np.abs(audio))), 1.0)
    return normalize(audio, 0.8)


def load_foley_sources(temp_dir: Path) -> dict[str, np.ndarray]:
    return {
        name: load_source_ogg(AUDIO_SOURCE_DIR / filename, temp_dir)
        for name, filename in FOLEY_SOURCE_FILES.items()
    }


def make_assets(foley: dict[str, np.ndarray]) -> dict[str, np.ndarray]:
    axe_hit = normalize(
        overlay(
            hit(0.38, 16),
            fit(highpass(foley["chop"], 9), 0.38, 0.5),
            fit(highpass(foley["knife_slice_2"], 11), 0.38, 0.25, 0.05),
        ),
        0.86,
    )
    crossbow_snap = normalize(
        overlay(
            crossbow(),
            fit(highpass(foley["draw_knife_1"], 7), 0.28, 0.42),
            fit(highpass(foley["metal_click"], 5), 0.28, 0.32, 0.07),
        ),
        0.82,
    )
    conduit_activate = normalize(
        overlay(sweep(0.58, 150.0, 590.0, 10), fit(highpass(foley["metal_latch"], 9), 0.58, 0.26, 0.1)),
        0.82,
    )
    conduit_deactivate = normalize(
        overlay(sweep(0.56, 560.0, 118.0, 11), fit(lowpass(foley["creak"], 13), 0.56, 0.2)),
        0.78,
    )
    shatter_with_foley = normalize(
        overlay(shatter(), fit(highpass(foley["metal_latch"], 5), 0.75, 0.35), fit(highpass(foley["coins"], 17), 0.75, 0.18, 0.08)),
        0.88,
    )
    return {
        "weather/red_storm_music": normalize(0.6 * rumble(12.0, 61) + 0.32 * chord(12.0, (55.0, 82.5, 110.0), 1.0) + 0.08 * spell(12.0), 0.68),
        "weather/red_storm_rumble": normalize(0.82 * rumble(5.0, 1) + 0.18 * lowpass(noise(5.0, 101), 121), 0.72),
        "weather/red_thunder_crack": thunder(0.8, 2),
        "weather/red_thunder_low": thunder(1.2, 3, low=True),
        "weather/red_thunder_portal": portal_thunder(),
        "ambient/portal_lava": normalize(env(smooth(noise(2.6, 8), 17) * 0.4 + sine(44.0, 2.6) * 0.22, 0.06, 0.12), 0.6),
        "ambient/portal_ghast": normalize(env(chord(2.4, (184.0, 207.0, 248.0), 0.45) + 0.12 * smooth(noise(2.4, 9), 21), 0.08, 0.18), 0.62),
        "block/nether_conduit_ambient": conduit_hum(),
        "block/nether_conduit_activate": conduit_activate,
        "block/nether_conduit_deactivate": conduit_deactivate,
        "entity/piglin_ambient": piglin_voice(0.9, 150.0, 12),
        "entity/piglin_brute_ambient": piglin_voice(1.0, 112.0, 13, 0.28),
        "entity/piglin_hurt": hit(0.34, 14),
        "entity/piglin_death": piglin_voice(1.05, 104.0, 15, 0.34) * np.linspace(1.0, 0.18, int(SAMPLE_RATE * 1.05)),
        "entity/piglin_crossbow": crossbow_snap,
        "entity/piglin_axe_hit": axe_hit,
        "entity/piglin_spell_shot": spell(0.5),
        "entity/piglin_evoker_cast": spell(0.78),
        "entity/piglin_ravager_ambient": ravager_roar() * 0.72,
        "entity/piglin_ravager_hurt": hit(0.46, 42),
        "entity/piglin_ravager_death": normalize(ravager_roar() * np.linspace(1.0, 0.05, int(SAMPLE_RATE * 1.15)), 0.82),
        "entity/piglin_ravager_roar": ravager_roar(),
        "entity/piglin_vex_ambient": normalize(env(chord(0.82, (510.0, 642.0, 763.0), 0.42) + 0.18 * smooth(noise(0.82, 45), 4), 0.01, 0.1), 0.72),
        "entity/piglin_vex_hurt": hit(0.22, 46),
        "entity/piglin_vex_death": sweep(0.56, 620.0, 160.0, 47),
        "entity/exiled_piglin_ambient": piglin_voice(1.2, 126.0, 48, 0.12),
        "entity/nether_dragon_ambient": dragon_growl(2.2, 51) * 0.68,
        "entity/nether_dragon_growl": dragon_growl(1.4, 52),
        "entity/nether_dragon_phase2": normalize(0.65 * dragon_growl(1.8, 53) + 0.4 * thunder(1.8, 54, low=True), 0.9),
        "item/ghast_tear_necklace_fireball": sweep(0.55, 290.0, 760.0, 55),
        "item/portal_shard_locate": normalize(overlay(env(0.5 * chord(0.9, (392.0, 554.0, 740.0), 1.0) + 0.28 * chirp(0.9, 760.0, 430.0), 0.01, 0.16), fit(highpass(foley["coins"], 23), 0.9, 0.12, 0.18)), 0.7),
        "music/disc_nether_tide": normalize(overlay(disc(), fit(lowpass(foley["creak"], 33), 16.0, 0.07, 4.0)), 0.68),
        "raid/approach": rumble(2.0, 57) * 0.72,
        "raid/start_sting": raid_sting(),
        "raid/wave_complete": wave_complete(),
        "raid/complete": completion_tone(1.35),
        "ritual/victory": completion_tone(1.55),
        "ritual/crystal_place": crystal_place(),
        "ritual/dragon_summon": normalize(overlay(0.5 * dragon_growl(2.0, 59), 0.45 * raid_sting()), 0.88),
        "ritual/pedestal_shatter": shatter_with_foley,
    }


def write_ogg(name: str, audio: np.ndarray, temp_dir: Path) -> None:
    destination = SOUNDS_DIR / f"{name}.ogg"
    destination.parent.mkdir(parents=True, exist_ok=True)
    wav_path = temp_dir / f"{name.replace('/', '_')}.wav"
    pcm = np.asarray(np.clip(normalize(audio), -1.0, 1.0) * 32767, dtype=np.int16)
    wavfile.write(wav_path, SAMPLE_RATE, pcm)
    subprocess.run(
        [
            "ffmpeg",
            "-y",
            "-loglevel",
            "error",
            "-i",
            str(wav_path),
            "-c:a",
            "libvorbis",
            "-q:a",
            "5",
            str(destination),
        ],
        check=True,
    )


def update_sounds_json() -> None:
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8"), object_pairs_hook=OrderedDict)
    missing = [event for event in data if event not in EVENT_TO_FILE]
    extra = [event for event in EVENT_TO_FILE if event not in data]
    if missing or extra:
        raise SystemExit(f"sounds.json mismatch; missing map={missing}, unused map={extra}")

    rewritten = OrderedDict()
    for event, entry in data.items():
        original = entry["sounds"][0] if entry.get("sounds") else {}
        sound = OrderedDict([("name", f"{MOD_ID}:{EVENT_TO_FILE[event]}")])
        for key in ("volume", "pitch", "weight", "attenuation_distance"):
            if key in original:
                sound[key] = original[key]
        if event in STREAMED_EVENTS:
            sound["stream"] = True
        rewritten[event] = OrderedDict([("subtitle", entry["subtitle"]), ("sounds", [sound])])

    SOUNDS_JSON.write_text(json.dumps(rewritten, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    if shutil.which("ffmpeg") is None:
        raise SystemExit("ffmpeg is required to encode .ogg procedural sounds")

    SOUNDS_DIR.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        temp_dir = Path(tmp)
        foley = load_foley_sources(temp_dir)
        assets = make_assets(foley)
        referenced = set(EVENT_TO_FILE.values())
        missing_assets = sorted(referenced - set(assets))
        if missing_assets:
            raise SystemExit(f"missing generated assets: {missing_assets}")

        for name in sorted(referenced):
            write_ogg(name, assets[name], temp_dir)

    update_sounds_json()
    print(f"Generated {len(referenced)} procedural .ogg files and updated sounds.json")


if __name__ == "__main__":
    main()
