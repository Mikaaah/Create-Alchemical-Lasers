# Creation Augmenter runtime integration

The project registers `creationaugmenter:creation_augmenter` and three finite-durability
lens items (10 Unbreaking uses; 100 Conversion/Transmutation uses). Its namespace is separate from Alchemical Lasers, so the two mods
can be installed together. Registration and server logic use Mojang mappings
for NeoForge 1.21.1 and Create 6.0.10.

## Machine setup

Connect a Create shaft to a horizontal side of the upper housing. At least
128 RPM is required; the machine applies 64 stress units per RPM. A neighboring
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
a replacement. Removing a lens cancels in-progress work without spending durability. Each completed
operation spends one use; the final use shatters and removes the lens.

Each operation charges for 20 ticks, fires for its recipe processing_time (default
60 ticks), then cools for 20 ticks. Recipes may set min_rpm above the 128 RPM baseline.
An unavailable recipe simply leaves the machine idle. An interrupted operation
never writes an output, so restarting cannot duplicate or lose the original.
On world load, the saved lens is restored and an unfinished operation restarts
from idle instead of committing stale item references.

## Lens behavior

- Unbreaking adds Minecraft's actual `minecraft:unbreakable` data component to
  one damageable equipment item. It preserves its item identity, existing
  components, name and damage. This is not the Unbreaking enchantment. Already
  unbreakable items are skipped, preventing repeated augmentation cycles.
- Absolute Conversion uses explicit `creationaugmenter:creation_augmenting` recipes.
- Transmutation uses explicit recipes of the same type, with its own lens key.

No arbitrary free tier upgrades or random equipment-property changes are enabled.
The two example conversions in `recipe-examples/` are **inactive examples**;
they are kept outside `src/main/resources`. Adjust their ingredients, outputs,
and progression costs for the modpack, then install the folder as a datapack
or copy individual recipes into `data/<namespace>/recipe/`.

## Recipe schema

```json
{
  "type": "creationaugmenter:creation_augmenting",
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

One recipe consumes one item. Depot stacks keep their remainder and buffer the
converted output for extraction. Loose item stacks keep their remainder and spawn
the result separately. If multiple recipes match, the lexicographically first
recipe ID is selected, yielding deterministic behavior after datapack reloads.
The active recipe is revalidated before commit; removing it cancels safely.

## Renderer/particle contract

`CreationAugmenterBlockEntity` exposes `getPhase()`, `getCycleTicks()`, `isBeamActive()`,
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

The combined suite passed 65 required server tests, including finite lens uses,
shattering, codec round trips, stacked Depot inputs and safe interruptions. Tests
are excluded from the release JAR. See [the current integration guide](../CREATION_AUGMENTER.md)
for full recipe fields and validation details.
