# Copper housing refinement

The original 214-cuboid model and all existing UV rectangles are retained. Twenty upper-housing cuboids now use copper material texels: pressure decks, front armour, side/rear bulkheads, window jambs, corner cuffs and the top input manifold. Existing brass trim, bearings and extruded bolt heads remain in place.

Create 6.0.10 calls its copper Fluid Tank textures `create:block/fluid_tank` and `create:block/fluid_tank_top`. These verified asset paths implement the requested copper-tank appearance. `create:block/copper_casing`, `minecraft:block/copper_block` and `minecraft:block/cut_copper` provide the other upper-housing surfaces.

The atlas islands sample complete source plates with nearest texels. First and last seam pixels are retained on small faces; no interpolation, gradient fills or replacement flat colours are used. The plate's dark copper perimeter, warm edge highlights and mottled inner shading come from the actual installed Minecraft 1.21.1 and Create 6.0.10 textures. One-pixel atlas gutters are refreshed without repacking.

The amethyst block, crossed shards, gimbal and open cage retain their exact original texels. All 1,164 unmodified machine faces were compared byte for byte after the copper patch. No railway-casing material remains assigned to any machine face. The machine's lower-emitter geometry changes are tracked separately from this material patch.

`copper-material-audit.json` records all 120 changed faces, the verified source paths and source PNG hashes. Existing Minecraft/Create asset attribution and distribution terms still apply.
