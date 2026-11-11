# Processing and lenses

| Lens | Item ID | Default beam color | Example |
|---|---|---|---|
| Growth | `alchemicallaser:growth_lens` | `#86C65B` | Wheat seeds → wheat |
| Convergence | `alchemicallaser:convergence_lens` | `#F0B65D` | 4 amethyst shards → amethyst block |
| Splitting | `alchemicallaser:splitting_lens` | `#65D6D1` | Amethyst block → 4 shards |
| Power | `alchemicallaser:power_lens` | `#E77E47` | Sand → glass |
| Eclipse | `alchemicallaser:eclipse_lens` | `#A286DC` | Sculk → echo shard |

The five lenses are reusable recipe selectors. Packs can instead specify any registered non-air item, override the beam color and consume one catalyst per operation. These are recipe rules, not fixed global bonuses for each lens.

A bulk cycle processes every complete recipe quantity in a stack at once. With a recipe using four inputs, a stack of ten runs twice and leaves two. With consumption enabled, available catalysts cap the number of operations. Secondary chances roll separately for each operation; oversized outputs split into legal stacks.

Belts, Depots and dropped stacks follow the same quantity rules. Several dropped stacks can process together, sharing the installed catalyst supply. Input/catalysts are spent only when a cycle commits. Power loss, redstone, a blocked gap or a changed catalyst interrupts unfinished work. Existing unprocessed input remains available.

Create sequenced assembly steps take priority over standalone recipes. Intermediates awaiting another step are left untouched. Standalone recipe ties use alphabetical recipe ID order.

[Home](Home)
