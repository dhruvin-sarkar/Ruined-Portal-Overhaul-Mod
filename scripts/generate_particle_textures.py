from __future__ import annotations

from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
PARTICLE_DIR = ROOT / "src/main/resources/assets/ruined_portal_overhaul/textures/particle"


def radial_sprite(size: int, core: tuple[int, int, int], rim: tuple[int, int, int], alpha: int) -> Image.Image:
    image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = image.load()
    center = (size - 1) / 2.0
    max_distance = center * 1.12
    for y in range(size):
        for x in range(size):
            dx = x - center
            dy = y - center
            distance = (dx * dx + dy * dy) ** 0.5
            if distance > max_distance:
                continue
            t = min(1.0, distance / max_distance)
            falloff = (1.0 - t) ** 1.7
            red = round(core[0] * (1.0 - t) + rim[0] * t)
            green = round(core[1] * (1.0 - t) + rim[1] * t)
            blue = round(core[2] * (1.0 - t) + rim[2] * t)
            pixels[x, y] = (red, green, blue, round(alpha * falloff))
    return image


def ember_sprite() -> Image.Image:
    image = radial_sprite(8, (255, 225, 118), (196, 35, 24), 230)
    pixels = image.load()
    pixels[3, 1] = (255, 246, 176, 255)
    pixels[4, 2] = (255, 203, 68, 235)
    return image


def rune_sprite() -> Image.Image:
    image = radial_sprite(8, (255, 80, 118), (92, 0, 40), 210)
    pixels = image.load()
    for x, y in ((2, 2), (3, 3), (4, 4), (5, 5), (5, 2), (4, 3), (3, 4), (2, 5)):
        pixels[x, y] = (255, 135, 176, 245)
    return image


def dragon_blood_sprite() -> Image.Image:
    image = radial_sprite(8, (255, 44, 62), (72, 0, 10), 235)
    pixels = image.load()
    for x, y, color in (
        (3, 1, (255, 102, 94, 255)),
        (4, 2, (215, 8, 30, 245)),
        (4, 3, (180, 0, 22, 230)),
        (3, 4, (120, 0, 18, 210)),
        (2, 5, (70, 0, 14, 170)),
    ):
        pixels[x, y] = color
    return image


def main() -> None:
    PARTICLE_DIR.mkdir(parents=True, exist_ok=True)
    sprites = {
        "nether_ember.png": ember_sprite(),
        "corruption_rune.png": rune_sprite(),
        "dragon_blood.png": dragon_blood_sprite(),
    }
    for name, image in sprites.items():
        image.save(PARTICLE_DIR / name)
    print(f"PARTICLE_TEXTURES_GENERATED {len(sprites)}")


if __name__ == "__main__":
    main()
