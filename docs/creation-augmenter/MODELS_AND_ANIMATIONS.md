# Creation Augmenter models and animation

The runtime uses `geometry/machine.json`, `geometry/lenses/*.json`, and
`animations/machine.authoring.json`. The supplied NeoForge BER evaluates that
hierarchy directly, including multi-texture materials and the live Creation Core.
GeckoLib or AzureLib is not required by this implementation.

## Editable Blockbench projects

Open `art/Creation-Augmenter.bbmodel` for the main machine with an Unbreaking
lens. `art/lens-variants/` contains a complete project for each of the three
lenses. Each project embeds all five texture images, the 15-bone hierarchy,
and ten native animation clips. Models use Blockbench's Free Model format so
multi-texture UVs, arbitrary bone animations, and fractional cuboid dimensions
remain editable.

The animated Creation Core texture is physically 16×400 pixels but uses a
16×16 logical UV frame. Its Blockbench texture metadata specifies two ticks
per frame, loop order and interpolation. The 24 supplied frames are preserved;
frame 25 duplicates the first frame to satisfy the requested 25-frame layout.
The normalized PNG and matching `.mcmeta` are under `textures/block/`.

The 1,050 static UV rectangles are unique and non-overlapping across the
machine and all three lens overlays. The crystal's 18 animated faces
intentionally reuse the supplied flipbook's 16×16 frame; they do not consume
or overlap any static atlas rectangle. The separate core texture is sampled
at exactly the same animation phase on each face.

## Animation clips

All durations are seconds. Geometry positions and translations use pixels.

| Clip suffix | Duration | Behavior |
| --- | --- | --- |
| `idle` | 2.00, loop | Core levitation ±0.35px with 32 sinusoidal samples and a restrained scale pulse |
| `kinetic_drive` | 1.00, loop | Reference preview: horizontal shaft and internal flywheel counter-rotation; runtime angles come from Create RPM |
| `spin_up` | 0.60 | Accelerates both tilted gyroscopes and the core |
| `spin` | 4.00, loop | Outer gyro +360°/s, inner gyro −360°/s, core +90°/s before RPM scaling |
| `spin_down` | 0.80 | Small additional rotation that slows to a stop, relative to the last powered orientation |
| `lens_remove` | 0.28, hold | Three claws retract 2px and flare approximately 35°; nozzle retracts 0.5px |
| `lens_insert` | 0.32, hold | Claws snap shut with a short seating recoil and return to zero |
| `firing` | 0.32 | Nozzle extends downward 2.5px, stabilizing fins open 22.5°, core pulses |
| `firing_loop` | 0.80, loop | Holds the extended nozzle and open fins; continues the core pulse |
| `firing_release` | 0.30 | Retracts the nozzle and closes fins smoothly after firing ends |

The full names begin with `animation.creation_augmenter.`. The runtime blends
idle levitation, powered rotation, lens changes and firing on independent
clocks. Core flipbook color uses the shared world clock independently of
machine RPM, preventing separate machines from drifting in hue.

Both gyros have fixed opposite 22.5° X tilts. Counter-rotation around their
local Y axes therefore produces two distinct world rotation axes. Their
pivots are `[0,-3.5,0]`. The lower nozzle pivot is `[0,-7,0]`, and the socketed
lens pivot is `[0,-14,0]`. Stabilizer fin order is north, east, south, west.

## GeckoLib 4 / AzureLib companions

`geo/creation_augmenter.geo.json` is the body geometry using
`textures/block/gecko_atlas.png`; the three `geo/lens_*.geo.json` files are
socket overlays using that same atlas. They retain a complete matching
skeleton so their transforms can be applied from the same animation state.
`animations/creation_augmenter.animation.json` is the converted companion
animation file. Coordinates and animation rotations are converted together.

The animated core is deliberately separate:

| Geometry | Texture binding | Logical size |
| --- | --- | --- |
| `geo/creation_augmenter.geo.json` | `textures/block/gecko_atlas.png` | 1024×1024 |
| `geo/lens_*.geo.json` | `textures/block/gecko_atlas.png` | 1024×1024 |
| `geo/creation_core.geo.json` | `CoreSpectrum.texture()` after `CoreSpectrum.prepare(...)` | 16×16 |

A Gecko/Azure renderer replacing the supplied BER must render the core as an
additional layer at the same pose and animation state. Before that layer,
call the provided `CoreSpectrum.prepare(level.getGameTime(), partialTick)`;
use its live 16×16 texture with full-bright lighting. This is the same
DynamicTexture used by the BER. Merely binding the raw 16×400 sheet to a custom
entity RenderType does not make its `.mcmeta` animate. Likewise, the core is
not baked into the static combined atlas, where it would silently freeze.

The companion exports are geometry/animation assets for that alternative
renderer workflow. The complete, compiled implementation in this package is
the supplied multi-texture BER, which already performs the separate animated
core pass. No unsupported Gecko/Azure adapter is installed automatically.

## Rebuilding exports

```bash
python3 tools/build_animations.py
python3 tools/export_models.py
```

The exporter requires Pillow. It preserves source PNG pixels, embeds current
textures, reconstructs stable UUIDs, and validates bone references, animation
targets, UV bounds, and static UV non-overlap. Results are written to
`docs/EXPORT_AUDIT.json`; `art/gecko-atlas-layout.json` records the exact
combined-atlas offsets.
