# Creation Augmenter runtime integration

The project registers `creationaugmenter:creation_augmenter` and three reusable
lens items. Its namespace is separate from Alchemical Lasers, so the two mods
can be installed together. Registration and server logic use Mojang mappings
for NeoForge 1.21.1 and Create 6.0.10.

## Machine setup

Connect a Create shaft to a horizontal side of the upper housing. At least
16 RPM is required; the machine applies 32 stress units per RPM. A neighboring
redstone signal disables it. Neither the input nor output is consumed while
charging, firing, or cancelling. Actual transformations happen on the server
only after the complete firing duration.

Place a Create Depot **three block positions beneath the upper machine block**,
with one piece of equipment on it. A machine at `(0, 4, 0)` processes a depot at
`(0, 1, 0)`. This leaves room below the fully extended emitter. The beam ends
at the depot's held-item surface. The depot is accessed through Create's real
DepotBehaviour, without copying depot geometry or bypassing its held item.

Alternatively, an item entity may rest on an anvil or pedestal in the centered
processing column. Its center must be within 0.18 blocks horizontally of the
machine center and its Y coordinate 1.4–2.5 blocks below the upper machine's
block floor. The target must be on the ground. Inputs moving, merging, being
removed, or changing count/components cancel the operation safely.

Right-click the machine with a Creation Lens to install it. Sneak and right-click
with an empty main hand to remove it. Remove the current lens before inserting
a replacement. Lens swaps cancel in-progress work and never consume a lens.

Each operation charges for 20 ticks, fires for 60 ticks, then cools for 20 ticks.
An unavailable recipe simply leaves the machine idle. An interrupted operation
never writes an output, so restarting cannot duplicate or lose the original.
On world load, the saved lens is restored and an unfinished operation restarts
from idle instead of committing stale item references.

## Lens behavior

- Unbreaking adds Minecraft's actual `minecraft:unbreakable` data component to
  one damageable equipment item. It preserves its item identity, existing
  components, name and damage. This is not the Unbreaking enchantment. Already
  unbreakable items are skipped, preventing repeated augmentation cycles.
- Absolute Conversion uses explicit `creationaugmenter:augmentation` recipes.
- Transmutation uses explicit recipes of the same type, with its own lens key.

No arbitrary free tier upgrades or random equipment-property changes are enabled.
The two example conversions in `recipe-examples/` are **inactive examples**;
they are kept outside `src/main/resources`. Adjust their ingredients, outputs,
and progression costs for the modpack, then install the folder as a datapack
or copy individual recipes into `data/<namespace>/recipe/`.

## Recipe schema

```json
{
  "type": "creationaugmenter:augmentation",
  "lens": "absolute_conversion",
  "ingredient": {"item": "minecraft:iron_sword"},
  "result": {"id": "minecraft:diamond_sword", "count": 1},
  "copy_components": true
}
```

`lens` accepts `unbreaking`, `absolute_conversion`, or `transmutation`. The
built-in Unbreaking effect takes precedence for ordinary damageable equipment.
`ingredient` is Minecraft's nonempty Ingredient codec and accepts item/tag forms.
`result` uses the 1.21.1 strict ItemStack codec, including data components.
`copy_components` defaults to false. When true, only the input's explicit
component patch is copied; the output item's default durability and attack
properties remain those of its new item type. Explicit result components then
override copied input components. Conversion recipes matching identical item
and component identity are skipped rather than processing forever.

One recipe consumes one item. A depot accepts singleton inputs because it has
one centered output position. Loose item stacks keep their remainder and spawn
the result separately. If multiple recipes match, the lexicographically first
recipe ID is selected, yielding deterministic behavior after datapack reloads.
The active recipe is revalidated before commit; removing it cancels safely.

## Renderer/particle contract

`AugmenterBlockEntity` exposes `getPhase()`, `getCycleTicks()`, `isBeamActive()`,
`hasPower()`, `lens()`, `lensKind()`, `getTargetPosition()` and
`beamTargetPosition()`. Both target-position methods return a world-space Vec3.
The target position and phase are synchronized in block-entity updates.
The shared server class never imports client classes.

Client tick controllers own animation transitions, looping beam audio and
particles. Common code broadcasts one respawn-anchor charge at the charge edge,
one heavy ignition at the firing edge, and one `SoundEvents.LAVA_EXTINGUISH`
when firing finishes or is cancelled, at Blocks volume 0.5 and pitch 1.15.
Charging-only cancellation does not play the firing shutoff hiss. Vanilla
trapdoor latch sounds provide restrained insertion/removal feedback.

The client owns the tiny smoke wisps and their particle-settings/distance budget.
No soul-sand or soul-flame particle is emitted.

## Tests

The `gameTest` source set contains five required server tests covering a complete
powered depot cycle, safe redstone interruption, saved lens restoration,
Unbreakable identity/durability preservation, and lens-aware recipes with safe
component copying. The tests are excluded from the release JAR.
