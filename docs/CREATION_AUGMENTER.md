# Creation Augmenter — 1.1.0

## Compatibility and setup

Minecraft 1.21.1, NeoForge 21.1.219+, Java 21, Create 6.0.10 / Flywheel 1.0.6. One JAR contains `alchemicallaser` and `creationaugmenter`; install neither reference module separately. No new runtime library is required. JEI, EMI and KubeJS remain optional.

Both machines emit strictly downward and accept only horizontal X/Z shaft power. Place the processing surface three blocks below the upper housing, leaving two air blocks between their block positions for the extended nozzle and beam. The Alchemical Laser supports belts, Depots and bulk dropped-item processing; its saved IDs and scripts are unchanged. Existing Alchemical Laser setups need the emitter and side power raised one block, or the processing line lowered one block. The Creation Augmenter supports Depots and grounded dropped items in the narrow beam column; Creation belt processing is not implemented. Its minimum speed is 128 RPM and stress is 64 SU/RPM. Individual recipes can require more speed. Processing time is measured in firing ticks, with separate charge/cooldown phases.

Use an empty main hand while sneaking to remove a lens, then insert another. Direct swaps are blocked. A lens retains its damage when removed or saved. Each successful operation consumes exactly one use; interruptions consume none. Unbreaking has 10 uses, Absolute Conversion and Transmutation have 100. Final use commits the output before removing the shattered lens. Built-in Unbreaking costs one use per equipment item and preserves damage, name, enchantments and other components; Creation Lenses themselves cannot be made unbreakable this way.

## Datapack recipe

Place JSON in `data/<namespace>/recipe/<name>.json`:

```json
{
  "type": "creationaugmenter:creation_augmenting",
  "lens": "creationaugmenter:transmutation_lens",
  "ingredient": {"item": "minecraft:diamond"},
  "result": {"id": "minecraft:emerald", "count": 2},
  "processing_time": 100,
  "min_rpm": 128,
  "copy_components": false
}
```

`lens`, `ingredient` and `result` are required. Lens values also accept `transmutation`, `absolute_conversion` or `unbreaking`. `processing_time` defaults to 60 (range 1–72000); `min_rpm` defaults to 128 (range 128–65536). `copy_components` defaults to false; when enabled, explicit input component patches are copied, then result components take precedence. No-op recipes are skipped. Each operation consumes one ingredient item, including when a Depot or dropped stack contains several. Depot outputs use Create's normal output buffer when a remainder is held, so use funnels to extract them. A datapack reload that changes the active recipe cancels that operation safely.

Four bundled examples cover scrap-to-netherite, echo refining, diamond-to-emerald and iron-to-diamond pickaxe conversion. Crafting uses vanilla/Create materials, including Nether Stars, Netherite, Sturdy Sheets, Echo Shards, Amethyst and Tinted Glass; no modpack-exclusive Creation Core item is required. Creation recipes are independent of the legacy alchemical recipe config switches; remove/replace them through datapacks or KubeJS.

## KubeJS

```js
ServerEvents.recipes(event => {
  event.recipes.creationaugmenter.creation_augmenting(
    'creationaugmenter:transmutation_lens',
    'minecraft:diamond',
    '2x minecraft:emerald'
  ).TIME(100).RPM(128)
  // event.custom({...}) accepts the same JSON fields above.
})
```

Native schema provides LENS, INPUT, OUTPUT, TIME and RPM aliases. Recipe serialization uses MapCodec and StreamCodec; optional JEI/EMI categories display lens, duration and speed.

## Art and source

Editable machine and socketed-lens Blockbench files are under `art/alchemical-laser` and `art/creation-augmenter`. Exported Minecraft/geometry assets, detailed atlases, 16x16 lens sprites and emissive masks are under `src/main/resources/assets/<modid>`. Both bodies are 31 pixels tall. The laser uses copper/fluid-tank references, its retained open amethyst cage and extended moving nozzle; no railway casing is assigned. Creation uses faceted cuboids, dark steel, gilded rings, tinted containment and a 25-frame 16x400 core with frame time 2 and interpolation. The Transmutation crystal also changes hue while its metal bezel remains untinted.

The core texture, beam hue and particles share a clock. Both machines have layered plasma, helices and descending rings, small smoke wisps on shutoff, and vanilla lava-extinguish sound at volume 0.5/pitch 1.15. No Soul Flame/Soul Sand effects are used. Horizontal shaft instances extend Create's OrientedRotatingVisual; modern VertexConsumer methods and ResourceLocation factories are used.

Java entry points: `nl.mikaaah.creationaugmenter.CreationAugmenter`, `CreationAugmenterBlock`, `CreationAugmenterBlockEntity`, `CreationLensItem`, `recipe.CreationAugmentingRecipe`, and `client.CreationAugmenterRenderer`. Existing alchemical classes remain named `LaserBlockEntity` and `client.LaserBeamRenderer` to preserve project conventions.

The five alchemical display names map to existing IDs for saved worlds and scripts:

| Display name | Existing item ID |
|---|---|
| Quartz Focus Lens | alchemicallaser:convergence_lens |
| Ignis Thermal Lens | alchemicallaser:power_lens |
| Glacies Cryo Lens | alchemicallaser:splitting_lens |
| Aether Transmutation Lens | alchemicallaser:eclipse_lens |
| Void Disintegration Lens | alchemicallaser:growth_lens |

## Verification

The Gradle build and 65 dedicated-server GameTests passed on Java 21, NeoForge 21.1.219 and Create 6.0.10. Tests include 10 sequential Unbreaking operations, 100th-use shattering, stack conservation, timing, high-RPM gating, obstructions, interruptions, component copying, saving and codec round trips. Resource, UV, animation, core-frame and sound-reference validators passed. A graphical Minecraft client check confirmed both models, rotating side shafts, the cycling Creation core, installed lens, firing nozzle and downward layered beam. A completed client operation recorded one lens damage at 128 RPM and 8192 SU. Optional JEI/EMI and the new KubeJS schema compile/validate but have not received a full graphical/scripted integration run. Shader-pack compatibility remains unverified.
