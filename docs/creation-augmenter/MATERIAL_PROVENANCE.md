# Material provenance

Creation Augmenter geometry, custom rune details and lens pixel grids were
created for this project. No third-party block model geometry is imported.

The machine's steel, creative-tier casing, brass, shaft, glass and netherite
texels come from the Minecraft 1.21.1 and Create 6.0.10-280 assets identified
in `material-map.json`. Lens asset source paths and hashes are recorded in
`LENS_ASSET_AUDIT.json`. Original ownership and upstream asset terms remain
unchanged; this export does not relicense Minecraft or Create artwork.

Create source and asset-license reference:
https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/LICENSE.md

The Creation Core was supplied by the user as `creative_core(1).png`,
16x384 pixels. All 24 frames remain unchanged in RGBA content; frame 1 is
repeated once at the end to make `creation_core.png` 16x400 pixels.
`CORE_ANIMATION.json` records its source hash and normalization. A copy of
the original input is in `art/source/creative_core.png`.

The heavy beam and ignition sounds are original synthesized assets retained
from the approved Alchemical Laser audio design. Vanilla charge, lens latch
and lava-extinguish sounds are referenced through Minecraft sound events;
Mojang OGG files are not bundled as mod assets.
