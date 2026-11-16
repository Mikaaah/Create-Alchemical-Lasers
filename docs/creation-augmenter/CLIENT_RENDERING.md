# Creation Augmenter client rendering

The live renderer reads `geometry/machine.json` and the selected
`geometry/lenses/*.json` with explicit texture pages. Positions and pivots use
Blockbench pixels with Y pointing up; the BER translates to the block center,
rotates for a Z-axis shaft and scales by 1/16. The emitter always points down.

`CreationAnimation` evaluates `animations/machine.authoring.json`, the same
explicit keyframes exported to Blockbench and GeckoLib. Idle bobbing continues
while the kinetic drive is stopped. Power transitions retain gyro orientation;
ring rotation is driven by RPM after spin-up. Lens tracks keep empty claws open
and close them on insertion. Firing extends the nozzle 2.5px, then the release
track retracts it over 0.30 seconds. The socketed lens inherits nozzle movement.

`CoreSpectrum` supplies a shared live 16x16 texture. Faces marked `animated_core`
use that image with frame-local UVs. The source sheet is not incorrectly drawn
as a 400px-tall static entity texture. The core, beam and particle hues share
world ticks and render partial ticks. Texture reloads release the old live
texture and invalidate geometry and animation caches.

Flywheel 1.0+ instances the 4x4 horizontal shaft with the supplied partial model.
The BER omits the shaft cuboids and a cached Create buffer draws the same partial
when instancing is unavailable. Internal flywheel geometry remains in the BER
and uses the same kinetic angle, so it follows both RPM and axis orientation.

Client ticks, not render frames, emit glyphs, prismatic motes and sparse rising
sparks. Machines are discovered by the BER and held with weak keys. World
changes, unloading and leaving the 64-block range release state and stop loops.
Particle budgets decrease with distance and Minecraft particle settings.

A firing-to-stopped edge emits up to two vanilla smoke wisps at the animated
muzzle and two at the target. Each is scaled to 0.28, lasts 10–15 ticks and starts
with upward velocity 0.02–0.04. Reduced settings permit one per endpoint; Minimal
permits none. The server owns the single cooling hiss; the client does not play
a duplicate. There are no soul, flame or continuous beam-smoke emissions.

The heavy current loop fades over four ticks, follows the moving muzzle, and
stops if its block entity is removed, unloaded, out of range or in another world.
Minecraft's Blocks sound slider controls its volume. Charging, lens interaction,
ignition and the cooling hiss are server-authoritative sounds.

All custom geometry uses the Minecraft 1.21.1 `addVertex`, `setColor`, `setUv`,
`setOverlay`, `setLight` and `setNormal` vertex API. No legacy `endVertex()` calls
are used. The companion Gecko files are authoring exports; this renderer does
not require a GeckoLib runtime installation.
