# Creation Core and synchronized beam

The supplied image contains 24 frames at 16x16 (16x384 total). The normalized
runtime sheet is 16x400: every supplied RGBA texel is retained, and frame 25
repeats frame 1. This holds the seam briefly before the cycle restarts.
Both `creation_core.png` and its fullbright `_e.png` companion have animation
metadata with `frametime: 2` and `interpolate: true`, producing a 50-tick cycle.
The original upload is retained in `art/source/creative_core.png`.

Minecraft's standalone entity textures do not automatically animate from
`.mcmeta`. `CoreSpectrum` therefore loads the strip, creates one shared 16x16
dynamic texture, and interpolates its RGBA channels at render time. All machines
share the world's clock; no per-machine random phase or wall-clock time is used.
The fullbright pass samples the same live image. Resource reload releases the
dynamic image and reads the current resource-pack version on the next use.

Animated core faces use logical 16x16 UV coordinates. The physical sheet is
16x400, but the geometry renderer replaces it with the 16x16 live image. Static
model and lens atlas islands remain independently allocated. Reusing the
animated core's supplied sprite across its facets is intentional; those dynamic
UV regions are not separate static atlas islands.

The representative color of each frame is sampled from its chromatic crystal
texels; dark neutral metal is excluded. The same 2-tick frame interpolation
controls the beam tint and particle colors. The exact 25-color palette, source
hash and normalization details are in `CORE_ANIMATION.json`.

`CreationBeamRenderer` renders downward from the actual animated lens position.
Its endpoint follows the synchronized target item's height while retaining the
machine's vertical centerline. The source moves with the nozzle's 2.5px
extension; it is not a hard-coded visual offset.

The beam uses three fullbright, depth-tested `NEW_ENTITY` passes with additive
alpha blending and color-only writes:

- A thin square white/iridescent plasma prism, tinted by the current core frame.
- Two counter-rotating, UV-scrolling ribbons mixing Creative violet/magenta with
  the installed lens's two colors.
- A faint containment aura, descending square infusion bands, and expanding
  square target seals. Rings use four straight sides rather than smooth circles.

Geometry budgets are bounded to 8 blocks and 128 segments per ribbon. All
vertices use NeoForge 1.21.1's `addVertex`/`setColor`/`setUv`/`setOverlay`/
`setLight`/`setNormal` API. Particle and sound emission is owned by client/server
ticks, not by render frames.
