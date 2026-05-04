from __future__ import annotations

import argparse
import colorsys
from pathlib import Path

from PIL import Image, ImageEnhance, ImageFilter, PngImagePlugin


ROOT = Path(__file__).resolve().parents[1]
ENTITY_TEXTURE_DIR = ROOT / "src/main/resources/assets/ruined_portal_overhaul/textures/entity"
MARKER_KEY = "ruined_portal_overhaul_postprocess"
MARKER_VALUE = "entity_texture_readability_v1"


def boost_gold_and_orange(image: Image.Image) -> Image.Image:
    pixels = image.load()
    width, height = image.size

    for y in range(height):
        for x in range(width):
            red, green, blue, alpha = pixels[x, y]
            if alpha == 0:
                continue

            hue, lightness, saturation = colorsys.rgb_to_hls(red / 255.0, green / 255.0, blue / 255.0)
            hue_degrees = hue * 360.0
            if 18.0 <= hue_degrees <= 58.0 and saturation > 0.18 and lightness > 0.18:
                saturation = min(1.0, saturation * 1.2)
                lightness = min(1.0, lightness * 1.04)
                red_f, green_f, blue_f = colorsys.hls_to_rgb(hue, lightness, saturation)
                pixels[x, y] = (round(red_f * 255), round(green_f * 255), round(blue_f * 255), alpha)

    return image


def apply_tile_shading(image: Image.Image) -> Image.Image:
    source = image.load()
    width, height = image.size
    tile = 8 if max(width, height) <= 64 else 16

    for y in range(height):
        for x in range(width):
            red, green, blue, alpha = source[x, y]
            if alpha == 0:
                continue

            local_x = x % tile
            local_y = y % tile
            edge_distance = min(local_x, local_y, tile - 1 - local_x, tile - 1 - local_y)
            edge_factor = 0.92 if edge_distance == 0 else 0.97 if edge_distance == 1 else 1.0
            vertical_factor = 1.06 if local_y < tile * 0.25 else 0.94 if local_y >= tile * 0.75 else 1.0
            factor = edge_factor * vertical_factor
            source[x, y] = (
                max(0, min(255, round(red * factor))),
                max(0, min(255, round(green * factor))),
                max(0, min(255, round(blue * factor))),
                alpha,
            )

    return image


def process_texture(path: Path, force: bool) -> bool:
    with Image.open(path) as opened:
        if opened.info.get(MARKER_KEY) == MARKER_VALUE and not force:
            return False

        image = opened.convert("RGBA")
        processed = ImageEnhance.Contrast(image).enhance(1.3)
        processed = apply_tile_shading(processed)
        processed = boost_gold_and_orange(processed)
        processed = processed.filter(ImageFilter.UnsharpMask(radius=0.6, percent=65, threshold=3))

        png_info = PngImagePlugin.PngInfo()
        for key, value in opened.info.items():
            if isinstance(value, str) and key != MARKER_KEY:
                png_info.add_text(key, value)
        png_info.add_text(MARKER_KEY, MARKER_VALUE)
        processed.save(path, pnginfo=png_info)

    return True


def main() -> None:
    parser = argparse.ArgumentParser(description="Post-process generated entity textures for readability.")
    parser.add_argument("--force", action="store_true", help="Reprocess textures even if the metadata marker is present.")
    args = parser.parse_args()

    textures = sorted(ENTITY_TEXTURE_DIR.glob("*.png"))
    processed = 0
    skipped = 0

    for texture in textures:
        if process_texture(texture, args.force):
            processed += 1
        else:
            skipped += 1

    print(f"ENTITY_TEXTURES_PROCESSED {processed}")
    print(f"ENTITY_TEXTURES_SKIPPED {skipped}")
    print(f"ENTITY_TEXTURES_TOTAL {len(textures)}")


if __name__ == "__main__":
    main()
