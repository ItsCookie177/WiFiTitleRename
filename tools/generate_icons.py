#!/usr/bin/env python3
"""Generate launcher icon aliases and colour-template resources.

Run from the repository root:

    python3 tools/generate_icons.py

The script reads the untouched source assets from ``design/icons/`` (the
contents of ``caret-wave-assets.zip``) and writes:

* ``app/src/main/res/drawable/ic_caret_wave_*.xml`` (copied verbatim)
* ``app/src/main/res/drawable/ic_tpl_<id>_bg.xml`` / ``_fg.xml``
* ``app/src/main/res/mipmap-anydpi-v26/ic_launcher_<id>.xml``
* ``app/src/main/res/drawable-nodpi/ic_extra_<n>.webp`` (512 px, WebP)
* ``app/src/main/res/mipmap-anydpi-v26/ic_launcher_anime_<n>.xml``
* the generated ``activity-alias`` block in ``AndroidManifest.xml``
* the default ``ic_launcher.xml`` / ``ic_launcher_round.xml`` (Simple Default)

Adding a colour template
------------------------
1. Add an item to ``TEMPLATES`` below. ``id`` must be unique and stable; the
   alias is derived from it (``simple_lime`` -> ``.IconAliasSimpleLime``) and
   the resources become ``ic_tpl_simple_lime_*`` / ``ic_launcher_simple_lime``.
2. Run this script.
3. Add the same id to ``IconCatalog.kt`` with the generated resource names and
   translated title/summary resources. The manifest and drawables are handled
   by this script; the UI list is deliberately explicit so strings stay
   translatable.

The three extra photographs are not part of the AGPL-3.0 source licence; see
``NOTICE``. They are centre-cropped and converted to WebP. The full-bleed
adaptive background keeps the face inside the circular/squircle safe zone;
hair/background at the very edge can still be masked by the launcher.
"""

from __future__ import annotations

import re
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
DESIGN = ROOT / "design" / "icons"
RES = ROOT / "app" / "src" / "main" / "res"
MANIFEST = ROOT / "app" / "src" / "main" / "AndroidManifest.xml"

# id, background colour(s), glyph colour, swatch colours.
# A single colour is a solid layer; two colours make a diagonal linear gradient.
TEMPLATES = [
    {
        "id": "simple_default",
        "bg": ("#1C1670", "#5A2D91"),
        "fg": ("#E37DE6", "#F0B2AA"),
        "swatch": ("#1C1670", "#5A2D91"),
        "copy_default_assets": True,
    },
    {"id": "simple_white", "bg": ("#F2F2F7",), "fg": ("#1C1C1E",), "swatch": ("#F2F2F7",)},
    {"id": "simple_black", "bg": ("#0B0B0F",), "fg": ("#FFFFFF",), "swatch": ("#0B0B0F",)},
    {"id": "simple_blue", "bg": ("#1D4ED8",), "fg": ("#FFFFFF",), "swatch": ("#1D4ED8",)},
    {"id": "simple_green", "bg": ("#15803D",), "fg": ("#FFFFFF",), "swatch": ("#15803D",)},
    {"id": "simple_orange", "bg": ("#EA580C",), "fg": ("#1A1206",), "swatch": ("#EA580C",)},
    {"id": "simple_pink", "bg": ("#C2185B",), "fg": ("#FFFFFF",), "swatch": ("#C2185B",)},
]

EXTRAS = [1, 2, 3]

START_MARKER = "<!-- BEGIN GENERATED ICON ALIASES -->"
END_MARKER = "<!-- END GENERATED ICON ALIASES -->"


def camel(icon_id: str) -> str:
    return "".join(part[:1].upper() + part[1:] for part in icon_id.split("_"))


def alias(icon_id: str) -> str:
    return ".IconAlias" + camel(icon_id)


def read_glyph_path() -> str:
    text = (DESIGN / "ic_caret_wave_glyph.xml").read_text()
    match = re.search(r'android:pathData="([^"]+)"', text)
    if not match:
        raise SystemExit("glyph pathData not found")
    return match.group(1)


def bg_vector(colors: tuple[str, ...]) -> str:
    if len(colors) == 1:
        return f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="{colors[0]}"
        android:pathData="M0,0h108v108h-108z" />
</vector>
'''
    return f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:type="linear"
                android:startX="0"
                android:startY="0"
                android:endX="108"
                android:endY="108"
                android:startColor="{colors[0]}"
                android:endColor="{colors[1]}" />
        </aapt:attr>
    </path>
</vector>
'''


def fg_vector(colors: tuple[str, ...], glyph_path: str) -> str:
    if len(colors) == 1:
        return f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <group
        android:scaleX="0.27"
        android:scaleY="0.27"
        android:translateX="19.44"
        android:translateY="18.36">
        <path
            android:fillColor="{colors[0]}"
            android:pathData="{glyph_path}" />
    </group>
</vector>
'''
    return f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <group
        android:scaleX="0.27"
        android:scaleY="0.27"
        android:translateX="19.44"
        android:translateY="18.36">
        <path android:pathData="{glyph_path}">
            <aapt:attr name="android:fillColor">
                <gradient
                    android:type="linear"
                    android:startX="30"
                    android:startY="40"
                    android:endX="226"
                    android:endY="226"
                    android:startColor="{colors[0]}"
                    android:endColor="{colors[1]}" />
            </aapt:attr>
        </path>
    </group>
</vector>
'''


def adaptive_icon(bg: str, fg: str, mono: str | None = None) -> str:
    mono_line = f'\n    <monochrome android:drawable="{mono}" />' if mono else ""
    return f'''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="{bg}" />
    <foreground android:drawable="{fg}" />{mono_line}
</adaptive-icon>
'''


def write(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text)


def process_extras() -> None:
    out_dir = RES / "drawable-nodpi"
    out_dir.mkdir(parents=True, exist_ok=True)
    for n in EXTRAS:
        src = DESIGN / f"icon{n}.jpg"
        im = Image.open(src).convert("RGB")
        w, h = im.size
        side = min(w, h)
        im = im.crop(
            ((w - side) // 2, (h - side) // 2, (w + side) // 2, (h + side) // 2)
        ).resize((512, 512), Image.LANCZOS)
        im.save(out_dir / f"ic_extra_{n}.webp", "WEBP", quality=82, method=6)
        write(
            RES / "mipmap-anydpi-v26" / f"ic_launcher_anime_{n}.xml",
            adaptive_icon(
                f"@drawable/ic_extra_{n}",
                "@drawable/ic_extra_fg_transparent",
            ),
        )


def main() -> None:
    glyph_path = read_glyph_path()

    write(RES / "drawable" / "ic_caret_wave_glyph.xml", (DESIGN / "ic_caret_wave_glyph.xml").read_text())
    write(RES / "drawable" / "ic_caret_wave_bg_default.xml", (DESIGN / "ic_caret_wave_bg_default.xml").read_text())
    write(RES / "drawable" / "ic_caret_wave_fg_default.xml", (DESIGN / "ic_caret_wave_fg_default.xml").read_text())

    for tpl in TEMPLATES:
        icon_id = tpl["id"]
        if tpl.get("copy_default_assets"):
            bg_ref = "@drawable/ic_caret_wave_bg_default"
            fg_ref = "@drawable/ic_caret_wave_fg_default"
        else:
            write(RES / "drawable" / f"ic_tpl_{icon_id}_bg.xml", bg_vector(tpl["bg"]))
            write(RES / "drawable" / f"ic_tpl_{icon_id}_fg.xml", fg_vector(tpl["fg"], glyph_path))
            bg_ref = f"@drawable/ic_tpl_{icon_id}_bg"
            fg_ref = f"@drawable/ic_tpl_{icon_id}_fg"
        write(
            RES / "mipmap-anydpi-v26" / f"ic_launcher_{icon_id}.xml",
            adaptive_icon(bg_ref, fg_ref, "@drawable/ic_caret_wave_glyph"),
        )

    # Application icon (also used by LSPosed/Vector) follows the Simple default.
    default_icon = adaptive_icon(
        "@drawable/ic_caret_wave_bg_default",
        "@drawable/ic_caret_wave_fg_default",
        "@drawable/ic_caret_wave_glyph",
    )
    write(RES / "mipmap-anydpi-v26" / "ic_launcher.xml", default_icon)
    write(RES / "mipmap-anydpi-v26" / "ic_launcher_round.xml", default_icon)

    write(
        RES / "drawable" / "ic_extra_fg_transparent.xml",
        '''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#00000000"
        android:pathData="M0,0h108v108h-108z" />
</vector>
''',
    )
    process_extras()

    entries = []
    for tpl in TEMPLATES:
        icon_id = tpl["id"]
        entries.append(
            f'''        <activity-alias
            android:name="{alias(icon_id)}"
            android:enabled="{'true' if icon_id == 'simple_default' else 'false'}"
            android:exported="true"
            android:icon="@mipmap/ic_launcher_{icon_id}"
            android:roundIcon="@mipmap/ic_launcher_{icon_id}"
            android:label="@string/app_name"
            android:targetActivity=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity-alias>'''
        )
    for n in EXTRAS:
        icon_id = f"anime_{n}"
        entries.append(
            f'''        <activity-alias
            android:name="{alias(icon_id)}"
            android:enabled="false"
            android:exported="true"
            android:icon="@mipmap/ic_launcher_{icon_id}"
            android:roundIcon="@mipmap/ic_launcher_{icon_id}"
            android:label="@string/app_name"
            android:targetActivity=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity-alias>'''
        )
    block = "\n".join(entries)

    manifest = MANIFEST.read_text()
    if START_MARKER not in manifest or END_MARKER not in manifest:
        raise SystemExit("manifest markers are missing")
    before, rest = manifest.split(START_MARKER, 1)
    _, after = rest.split(END_MARKER, 1)
    MANIFEST.write_text(
        before
        + START_MARKER
        + "\n        <!-- Generated by tools/generate_icons.py. Do not edit this block by hand. -->\n"
        + block
        + "\n        "
        + END_MARKER
        + after
    )

    print("Generated", len(TEMPLATES), "colour aliases and", len(EXTRAS), "extra aliases")


if __name__ == "__main__":
    main()
