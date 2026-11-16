# Alchemical Laser resource and model import

This export uses the current mod namespace, `alchemicallaser`. It includes the
latest approved inventory sprites and the actual machine geometry used by
Alchemical Lasers 1.1.0 for NeoForge 1.21.1.

## Import into the existing mod

Merge the supplied `src/main/resources/assets/alchemicallaser` directory into
the same location in your mod. Preserve its relative paths. Merge the supplied
`src/main/resources/data/alchemicallaser` directory as well for the updated
default beam colors. The main package also includes the matching Java source
and sound resources; include those changes together for lens removal audio,
beam-loop lifecycle, the longer nozzle's beam origin, and shutoff behavior.

The active renderer reads `geometry/machine.json` and `geometry/lenses/*.json`
with four explicit texture pages. It uses the custom multi-texture BER. The
`geo/*.geo.json` models and combined `gecko_atlas.png` are companion exports for
a GeckoLib-compatible workflow and do not automatically replace that renderer.

## Open and edit the models

- `art/Alchemical-Laser.bbmodel` is the main editable model, with a Convergence
  lens seated for preview.
- `art/lens-variants/` contains a complete machine project for each of the five
  socketed lens variants.
- Each project embeds its four texture pages and all nine native animation
  clips, so it can be opened without resolving external image files.
- The model body remains 214 cuboids and is now 31 pixels high (`Y: -15..16`).
  The nozzle is extended by 3px; the longer jaws, all lens overlays and pivots
  follow the new socket position. The approved amethyst core, gimbal and cage
  geometry remain unchanged.
- Upper plating uses Vanilla copper/cut copper and verified Create
  `fluid_tank`, `fluid_tank_top` and `copper_casing` texels with brass trim.
  There are no train-casing material assignments in the exported model.
- Ignition recoil reaches 2.25px. Lens removal retracts the nozzle 0.6px;
  insertion has a 0.8px seating recoil. Amethyst animation tracks are retained.

Native item sprites are under `textures/item/`, with matching `_e.png`
emissive masks. Copies for editing are provided in `art/lens-items/`.
The item sprites and in-world lens atlas faces are separate assets: editing
an inventory PNG does not repaint the socketed model's packed texture region.

## Names and color mapping

| Installed lens | Socket geometry key | Default beam tint |
| --- | --- | --- |
| Convergence | `quartz` | `#91CCD7` |
| Power | `ignis` | `#DF7D3D` |
| Splitting | `glacies` | `#86AEE7` |
| Eclipse | `aether` | `#8D6ACC` |
| Growth | `void` | `#2FA877` |

The tints are in
`data/alchemicallaser/data_maps/item/laser_lenses.json`. The five `color`
values retain the approved palette; lens pitch values and packaged recipes
remain unchanged. Recipe-defined beam color overrides continue to take precedence.

The resting beam begins at the new muzzle (`Y: -0.9375` blocks) and travels
`0.1025` blocks to the unchanged processing impact (`Y: -1.04`). Recoil extends
the beam back to that fixed endpoint. Chamber arcs retain their original
position and do not move with the extended muzzle.

## Verification and notices

`RESOURCE_IMPORT_AUDIT.json` records the baseline import and approved palette.
`copper-material-audit.json` records replaced face materials and unchanged
non-target atlas regions; `EXPORT_AUDIT.json` records the regenerated companions.
All five editable lens variants have valid embedded textures, element UUIDs,
bone references and nine animation tracks. The supplied resource JSON files
parse successfully. Run `python3 tools/refresh_model_exports.py` after editing
canonical geometry, atlas pages or `animations/machine.authoring.json` to refresh
all six Blockbench projects and the Gecko companions.

See `MATERIAL_PROVENANCE.md`, `verified-external-materials.json` and `licenses/`
for the upstream texture references and applicable notices. No third-party
model geometry is imported. Aeronautics textures are optional runtime resource
references with original fallback artwork.
