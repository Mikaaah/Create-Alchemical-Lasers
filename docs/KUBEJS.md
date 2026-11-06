# KubeJS and sequenced assembly

Put recipes in `kubejs/server_scripts/alchemical_lasers.js`. Requires KubeJS 7 for Minecraft 1.21.1. The previously tested combination is KubeJS 2101.7.2-build.379, Rhino 2101.2.7-build.81 and KubeJS Create 2101.3.1-build.18. Older KubeJS Create builds may be incompatible with Create 6.0.10. Install KubeJS Create as well when using its `event.recipes.create.sequenced_assembly` helper. Laser steps themselves work with Create alone through datapack JSON.

The addon ships a native KubeJS recipe schema. No `event.custom` wrapper, startup registration or hard dependency on KubeJS is needed.

## Requested field order

`LENS{item:item} COLOR{lasercolorhex} CONSUME{true/false} INPUT{item:item} OUTPUT{item:item}` maps to these JavaScript builder methods:

```js
ServerEvents.recipes(event => {
  event.recipes.alchemicallaser.laser_processing()
    .LENS('minecraft:coal')
    .COLOR('#FF8033')
    .CONSUME(true)
    .INPUT('minecraft:sand')
    .OUTPUT('minecraft:glass')
    .duration(100)
    .minimumRpm(16)
    .id('tnk:coal_laser_glass')
})
```

`LENS` names the item installed in the emitter, including an ordinary item used as a consumable. It does not need to be one of the five built-in lenses or be registered in a lens data map. `COLOR` accepts exactly six hex digits with an optional `#`. `CONSUME: true` spends one installed item per completed operation; false keeps it indefinitely. The input below the emitter is always consumed according to the recipe's input count.

The same five fields are available in a positional constructor, in your requested order:

```js
ServerEvents.recipes(event => {
  event.recipes.alchemicallaser.laser_processing(
    'minecraft:coal', '#FF8033', true,
    'minecraft:sand', 'minecraft:glass'
  ).id('tnk:coal_laser_glass_short')
})
```

For a reusable lens:

```js
ServerEvents.recipes(event => {
  event.recipes.alchemicallaser.laser_processing()
    .LENS('alchemicallaser:growth_lens')
    .COLOR('#86C65B')
    .CONSUME(false)
    .INPUT('minecraft:wheat_seeds')
    .OUTPUT('minecraft:wheat')
    .id('tnk:growth_wheat')
})
```

Lowercase aliases `.lens`, `.color`, `.consume`, `.input` and `.output` also work. Optional `.inputCount(4)` makes each operation consume four input items. Output counts use `.OUTPUT('5x minecraft:glass')`. `.secondary(...)`, `.secondaryChance(0.1)` and `.copyComponents(true)` remain available for standalone recipes.

## Sequenced assembly

```js
ServerEvents.recipes(event => {
  const incomplete = 'create:incomplete_precision_mechanism'

  event.recipes.create.sequenced_assembly(
    ['minecraft:diamond'],
    'minecraft:iron_ingot',
    [
      event.recipes.alchemicallaser.laser_processing()
        .LENS('minecraft:coal')
        .COLOR('#FF8033')
        .CONSUME(true)
        .INPUT(incomplete)
        .OUTPUT(incomplete)
        .duration(60),
      event.recipes.create.pressing(incomplete, incomplete),
      event.recipes.alchemicallaser.laser_processing()
        .LENS('alchemicallaser:eclipse_lens')
        .COLOR('#A286DC')
        .CONSUME(false)
        .INPUT(incomplete)
        .OUTPUT(incomplete)
        .duration(80)
    ]
  ).transitionalItem(incomplete).loops(2).id('tnk:laser_assembly')
})
```

Each laser step requires its specified catalyst. Use separate emitters for different catalysts. Supply coal with a hopper above the first emitter. Create handles the intermediate component, step advancement, loops and final weighted outputs. Assembly steps use one input per operation and no secondary output; put chances/byproducts in the parent assembly's result pool. Bulk stacks advance each item exactly one step per completed cycle, with one catalyst per item when consuming.

An intermediate awaiting another machine or another lens is left untouched. Laser recipe output is replaced by Create's next intermediate or final result. Avoid reusing the same incomplete item for unrelated recipes unless you deliberately want that behavior.

## Inventory and interruptions

A hopper pointing downward into the emitter feeds the catalyst slot. Funnels and item pipes can access the same capability from any side. The slot holds up to the item's normal stack limit (64 for coal, one for built-in lenses). Right-click installs one item and returns the previous stack; sneak with an empty hand to retrieve it.

For eight input items and three consumables, one bulk cycle processes three inputs and leaves five untouched. Secondary chance rolls once per operation. Power loss, redstone shutdown or a catalyst swap consumes nothing until a cycle completes. Container remainders are retained in the slot when possible, otherwise dropped at the emitter.

## Disable bundled recipes

After first launch, edit `config/alchemicallaser-common.toml`:

```toml
[recipes]
    enableDefaultProcessingRecipes = false
    enableDefaultCraftingRecipes = true
```

Restart after changing these switches. The first controls only bundled laser-processing examples. The second independently controls bundled emitter/lens crafting recipes. Custom datapack and KubeJS recipes remain enabled, including ones in the `alchemicallaser` namespace. Neither setting changes existing inventory.
