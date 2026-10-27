# Laser processing recipes

Minecraft 1.21.1 uses the singular `recipe` directory: `data/<namespace>/recipe/<name>.json`.

```json
{
  "type": "alchemicallaser:laser_processing",
  "ingredient": { "tag": "minecraft:sand" },
  "input_count": 1,
  "lens": "alchemicallaser:power_lens",
  "result": { "id": "minecraft:glass", "count": 1 },
  "duration": 100,
  "minimum_rpm": 16,
  "secondary": { "id": "minecraft:quartz", "count": 1 },
  "secondary_chance": 0.1,
  "copy_components": false
}
```

`duration` is processing time in ticks at 32 RPM, plus the configured charging period. Speed scales processing time up to the effective speed cap. Lenses are reusable catalysts. A bulk cycle performs `floor(stack_count / input_count)` recipe operations in one processing duration, retaining any incomplete quantity. Outputs split into legal stack sizes. Secondary chance rolls independently for every operation. The same rules apply to belts, Depots and each dropped stack directly under the nozzle. Removing power, disabling the emitter or changing the lens restarts unfinished work without consuming input.

`secondary`, `input_count`, `duration`, `minimum_rpm`, `secondary_chance`, and `copy_components` are optional. Their defaults are respectively absent, 1, 100, 16, 1.0, and false.

Input and output items can belong to any installed mod. Output stacks accept Minecraft 1.21.1 data components. With `copy_components: true`, input components are copied and the explicit output component patch is applied afterward. For state-changing recipes, use a component-sensitive NeoForge ingredient to ensure the already-processed item no longer matches. Do not create a self-matching recipe on a Depot unless repeated processing is intended.

Recipes are selected in recipe-ID order if several match. `/reload` updates recipes; an interrupted cycle never consumes input before completion.

## KubeJS and sequenced assembly

See [KUBEJS.md](KUBEJS.md) for native builders, consumables, top hoppers, config switches and a full sequenced assembly example.

The JSON recipe additionally accepts `"color": "#FF8033"` and `"consume": true`. `lens` can be any registered item ID. Color defaults to the installed lens's data-map color; consume defaults to false for backward compatibility. Bulk operations are capped by catalyst availability for consuming recipes. The slot now holds ordinary item stacks as well as reusable lenses.

Laser recipes implement Create's processing/assembly interfaces and may be embedded directly in a `create:sequenced_assembly` sequence. Assembly steps require `input_count: 1` and no secondary output.
