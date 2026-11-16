# Copper emitter and shutoff integration

This revision retains the approved open amethyst cage, core, gimbal and their
animation tracks. The 214-cuboid body is now 31px tall (`Y: -15..16`). Two
telescoping sleeve segments extend the emitter by 3px while the attachment stays
at `Y: -6`. The four longer jaw arms and socketed lens follow the new lower
position. Upper plating uses Vanilla copper/cut copper, Create copper casing
and Create's verified `fluid_tank` / `fluid_tank_top` textures with brass trim.
Train-casing material assignments are removed.

`geometry/machine.json`, `geometry/lenses/*.json` and
`animations/machine.authoring.json` are the active runtime assets. The matching
Blockbench projects and Gecko companions are regenerated with
`tools/refresh_model_exports.py`. Ignition recoil is 2.25px; lens removal adds
0.6px of nozzle retraction and insertion has a 0.8px seating recoil.

## Runtime alignment and effects

- `MachineAnimation` defines the resting muzzle at `-0.9375` blocks and the
  impact at `-2.04`. The resting beam length is `1.1025`; recoil changes that
  length while the impact stays fixed. Belts and Depots sit three block positions
  below the upper housing. The chamber arcs retain their original position rather
  than following the extended nozzle.
- `OpticalEffects` keeps the chamber energy arcs and emits sparse lens-colored
  impact sparks every four ticks. The beam's three animated geometry layers
  remain. Soul, flame and continuous beam-smoke emissions are removed.
- `LaserBlockEntity` plays `SoundEvents.LAVA_EXTINGUISH` once when the PROCESSING
  phase stops, including completion and interrupted firing. The server call
  uses `SoundSource.BLOCKS`, volume `0.5F × LaserConfig.SOUND_VOLUME`, and pitch
  `1.15F`. Charging cancellation alone does not play that firing shutoff hiss.
- `LaserEffects` detects the active-to-inactive firing edge in client ticks.
  It emits two tiny Vanilla SMOKE particles at the muzzle and two at the impact
  within 48 blocks. Each has scale 0.28, lifetime 10–15 ticks and upward velocity
  0.02–0.04. Decreased settings allow one per endpoint; Minimal allows none.

## Retained audio and lens behavior

The manual remove-first lens interaction is preserved. Actual inventory
exchange chooses `lens_remove` for extraction and `lens_install` for insertion;
render frames do not trigger interaction sounds.

`LaserEffects` owns one stoppable respawn-anchor charge instance per machine,
releasing within two ticks after charging ends. It owns one heavy beam loop,
with a four-tick fade at either edge. Both stop on unload, removal, world
changes or leaving range. Client tracking uses weak keys and weak references
for charge instances. Beam base volume is 0.30 before the mod-volume setting and
fade envelope; moderate lens/RPM pitch variation is retained.

The beam event ID remains `beam`. All eight registered sound IDs remain for
compatibility. The legacy `shutdown` entry now references Vanilla's lava-extinguish
event; the active runtime shutoff calls the Vanilla sound directly. The old
custom shutdown OGG is removed. Charge and cooling audio remain event references
in the distributable resources.

See `VALIDATION.md` for build/test evidence, `EXPORT_AUDIT.json` for derived-model
checks, and `copper-material-audit.json` for material replacement and preserved
atlas regions. Final appearance and mix can be reviewed with the modpack's
resource packs, shaders and audio settings.
