[← Guide home](https://github.com/Mikaaah/Create-Alchemical-Lasers/wiki) · [Project](https://github.com/Mikaaah/Create-Alchemical-Lasers) · [Get help](https://github.com/Mikaaah/Create-Alchemical-Lasers/issues)

# KubeJS and assemblies

Put recipes in `kubejs/server_scripts/`. KubeJS and Rhino are optional. KubeJS Create is additionally needed for its sequenced-assembly JavaScript helper; datapack assemblies work with Create alone.

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

`CONSUME(true)` spends one installed catalyst per completed operation. `false` keeps it reusable. `.inputCount(4)` consumes four inputs per operation; `.OUTPUT('5x minecraft:glass')` produces five output items. Lowercase aliases are also supported.

Laser steps can be nested inside Create sequenced assembly. Use one input per operation with no secondary output. Create controls intermediate state, loops and final weighted results. Use separate emitters for steps needing different catalysts. A hopper above the first emitter can feed consumables.

[Full builder and assembly examples](https://github.com/Mikaaah/Create-Alchemical-Lasers/blob/release/1.0.0/docs/KUBEJS.md) · [Datapack field reference](https://github.com/Mikaaah/Create-Alchemical-Lasers/blob/release/1.0.0/docs/RECIPES.md)

[Home](https://github.com/Mikaaah/Create-Alchemical-Lasers/wiki/Home)
