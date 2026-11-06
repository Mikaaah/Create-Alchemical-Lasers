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

`duration` is processing time in ticks at 32 RPM, plus the configured charging period. Speed scales processing time up to the effective speed cap. Catalysts are reusable by default; `consume: true` consumes one per operation. A bulk cycle performs `floor(stack_count / input_count)` recipe operations in one processing duration, retaining any incomplete quantity. Outputs split into legal stack sizes. Secondary chance rolls independently for every operation. The same rules apply to belts, Depots and each dropped stack directly under the nozzle. Removing power, disabling the emitter or changing the lens restarts unfinished work without consuming input.

`secondary`, `input_count`, `duration`, `minimum_rpm`, `secondary_chance`, and `copy_components` are optional. Their defaults are respectively absent, 1, 100, 16, 1.0, and false.

Input and output items can belong to any installed mod. Output stacks accept Minecraft 1.21.1 data components. With `copy_components: true`, input components are copied and the explicit output component patch is applied afterward. For state-changing recipes, use a component-sensitive NeoForge ingredient to ensure the already-processed item no longer matches. Do not create a self-matching recipe on a Depot unless repeated processing is intended.

Create sequenced assembly steps take priority. Ordinary recipes are selected in recipe-ID order if several match. `/reload` updates recipes; an interrupted cycle never consumes input before completion.

## KubeJS and sequenced assembly

See [KUBEJS.md](KUBEJS.md) for native builders, consumables, top hoppers, config switches and a full sequenced assembly example.

The JSON recipe additionally accepts `"color": "#FF8033"` and `"consume": true`. `lens` must be a registered non-air item ID; missing items are rejected when recipes load. Color defaults to the installed lens's data-map color; consume defaults to false for backward compatibility. Bulk operations are capped by catalyst availability for consuming recipes. The slot now holds ordinary item stacks as well as reusable lenses.

Laser recipes implement Create's processing/assembly interfaces and may be embedded directly in a `create:sequenced_assembly` sequence. Assembly steps require `input_count: 1` and no secondary output.

## Field reference

| JSON field | Default | Valid values |
|---|---|---|
| `ingredient` | Required | Nonempty Minecraft/NeoForge ingredient |
| `lens` | Required | Registered item ID, excluding air |
| `result` | Required | Nonempty item stack with optional count/components |
| `input_count` | `1` | Integer 1–64 |
| `duration` | `100` | Integer 1–72000, ticks at 32 RPM |
| `minimum_rpm` | `16` | Integer 1–4096; global minimum also applies |
| `color` | Lens color | Six hexadecimal digits, optional `#` |
| `consume` | `false` | One catalyst per operation when true |
| `secondary` | Absent | Nonempty output stack; standalone recipes only |
| `secondary_chance` | `1.0` | Number 0–1, rolled independently per operation |
| `copy_components` | `false` | Copy input components before explicit output patch |

The five native lens IDs are `alchemicallaser:growth_lens`, `convergence_lens`, `splitting_lens`, `power_lens` and `eclipse_lens` (all in the `alchemicallaser` namespace). Ordinary items need no registration as a lens. Without a color override or data-map entry, an ordinary catalyst uses gray.

A datapack can override `data/alchemicallaser/data_maps/item/laser_lenses.json` to set default RGB integer `color` (0–16777215) and optional `pitch` (0.5–2.0) per item. The file is a NeoForge item data map with a `values` object keyed by item ID. Recipe colors override data-map colors.
