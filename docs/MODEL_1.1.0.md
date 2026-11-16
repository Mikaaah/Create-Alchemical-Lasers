# Industrial model integration — 1.1.0

Minecraft 1.21.1 · NeoForge 21.1.219+ · Java 21 · Create 6.0.10.

The supplied industrial model replaces the lantern and brass casing. The complete
machine uses a reloadable multi-texture block-entity renderer, translucent glass,
emissive masks, a rotating amethyst core, gimbal, gears, stabilizers, lens jaws,
cooling vents and nozzle. The shaft uses Flywheel instancing with a conventional
Create rendering fallback. The inventory/hand model uses the same geometry.

## Existing worlds and recipes

The mod ID remains `alchemicallaser`; the block and entity remain
`alchemicallaser:laser_emitter`. Existing blocks, saved catalyst inventories,
configs, datapacks, KubeJS builders, sequenced assemblies and bundled recipes keep
their identifiers and behavior. A powered redstone signal still disables the laser.

The beam retains the existing processing endpoint. Leave one air block between
the emitter and a belt/Depot. The larger nozzle occupies more of that visual gap;
no extra block of vertical spacing is needed. Shafts still connect horizontally on
either end, and wrench rotation supports both X and Z axes. Hoppers still feed the
catalyst slot from above. Ordinary item catalysts remain visible in the socket.

The kit's standalone mod (`alchemical_laser`) is a visual reference rather than
the processing implementation. Do not install that reference JAR alongside this
update in place of the existing addon.

## Sound and color update

The updated import supplies final lens item textures and masks, matching beam and
particle palettes, and eight sound events. Charge references Minecraft’s respawn-
anchor charge event. Custom ignition, heavy beam, vent hiss and separate lens
insertion/removal audio are shipped as mono OGG assets. Client lifecycle handling
limits overlapping charge tails and fades the beam loop. See [sound design](SOUND_DESIGN.md).

## Changing a lens

Sneak-right-click with an empty main hand to remove the installed lens or catalyst.
The claws open and remain open while the socket is empty. Right-click with the new
lens to insert it and close the claws. Clicking an occupied socket with another
item leaves both items unchanged and shows the removal instruction. Hopper refills
of the same catalyst still work.

## Lens artwork mapping

Only the five established functional lenses are registered. Reference-art names
identify mesh styles and do not introduce extra lens items.

| Existing item | Supplied visual style |
| --- | --- |
| Growth | Void: green crystal and sculk-like details |
| Convergence | Quartz: focusing optic |
| Splitting | Glacies: cyan faceted optic |
| Power | Ignis: orange optic |
| Eclipse | Aether: violet optic |

Beam color remains recipe-driven, falling back to the lens data map. Custom
consumables retain their `COLOR` setting.

## Animation playback

`assets/alchemicallaser/animations/machine.authoring.json` contains the original
Y-up keyframes used at runtime. Independent clocks per block control power-up,
continuous spinning, power-down, lens removal/insertion, ignition recoil and
continuous firing. Idle scale breathing is layered separately. The shaft and bevel
gears follow actual Create rotation. `kinetic_preview_60rpm` remains an authoring
preview; it never replaces live kinetic speed. The companion Gecko-format model
and animation exports are retained for future tooling.

`MachineGeometry` loads per-face texture assignments and the parent bone hierarchy.
`MachineAnimation` evaluates position, rotation and scale tracks. Resource reloads
clear both caches. Particle emission runs once per client tick with distance and
particle-setting budgets, rather than per rendered frame.

## Dependencies and material sources

No additional runtime dependency is needed. The supplied custom renderer does not
use GeckoLib's Java API. Create's existing matching Flywheel/Ponder dependencies
remain necessary as before; KubeJS, KubeJS Create, JEI and EMI remain optional.
Nuclear-derived texels are shipped with the supplied license and provenance.
Aeronautics materials are selected only if the resource stack supplies them;
original fallback texels are included. See [material provenance](MATERIAL_PROVENANCE.md).

## Verification

- Java 21 build and release packaging checks pass.
- All 53 server GameTests pass without scripting mods; the scripting-only
  case takes its documented skip path in that installation.
- Geometry validation covers six models (250 cuboids), bone parents, UV bounds,
  texture sizes, nine authored animation clips and all particle sprite references.
- Existing recipe, resource, translation and five-lens checks pass.

A graphical Minecraft client was not available for this integration run. In-game
model playback, transparency, shader/resource-pack combinations, inventory views,
Ponder visuals, Flywheel fallback and audio still need visual review. Server tests
are not a substitute for those checks.
