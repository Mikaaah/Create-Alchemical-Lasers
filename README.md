<p align="center"><img src="docs/branding/banner.png" alt="Create: Alchemical Lasers" width="100%"></p>

<h1 align="center">Create: Alchemical Lasers</h1>

<p align="center"><strong>Put a little alchemy on the assembly line: kinetic lasers, five lenses and bulk processing.</strong></p>

<p align="center"><a href="https://www.curseforge.com/members/mikaaah/projects">Downloads</a> · <a href="https://github.com/Mikaaah/Create-Alchemical-Lasers/wiki">Player guide</a> · <a href="https://github.com/Mikaaah/Create-Alchemical-Lasers/issues">Report an issue</a> · <a href="https://github.com/Mikaaah/Create-Alchemical-Lasers/blob/main/CHANGELOG.md">Changelog</a> · <a href="https://discord.gg/CRYk3uDPVt">Discord</a></p>

<p align="center"><img src="https://img.shields.io/badge/Minecraft-1.21.1-5b8731?style=flat-square" alt="Minecraft 1.21.1"> <img src="https://img.shields.io/badge/NeoForge-d77828?style=flat-square" alt="NeoForge"> <a href="https://mikaaah.github.io/TownsAndKingdoms/projects/"><img src="https://img.shields.io/badge/Towns_%26_Kingdoms-Modding_Hub-7f384a?style=flat-square" alt="Towns & Kingdoms Modding Hub"></a></p>

> **Looking for the TNK development edition?** The [1.0.0-rc.1 branch](https://github.com/Mikaaah/Create-Alchemical-Lasers/tree/release/1.0.0) documents the newer pack-specific work. This main-branch guide describes the standalone/source version below.


## How it works

1. Place an emitter above a horizontal belt or Depot, leaving one air block between them, like a Mechanical Press.
2. Connect rotational power to either exposed end of the horizontal shaft and supply at least 16 RPM. Use a wrench to align the emitter with your shaft line.
3. Right-click with any optical lens. Sneak-right-click with an empty hand to remove it.
4. Supply a matching item. The laser holds it during charging and processing, commits the transformation, then releases the output.

Speed scales processing time, up to the configured cap. A powered redstone signal disables the emitter. Interrupted cycles leave input unchanged; belt items are released, and Depots can restart when power returns.

## Features

- Bulk processing on belts and Depots, plus multiple dropped item stacks directly beneath the beam. Complete recipe quantities process together; incomplete remainders stay behind.
- Five reusable lenses with fitted 3D cartridges, animated Minecraft beacon beams, charging particles and completion effects.
- Seven original sound events, including a spatial beam loop that stops when processing stops or the machine unloads.
- Engineer's Goggles information, Create wrench interaction and a one-slot lens item capability for automation.
- Datapack recipes and generic KubeJS custom recipes; optional component copying and probabilistic secondary outputs.
- Optional JEI and EMI recipe categories showing the lens, input, outputs, duration and minimum RPM.
- Six Ponder scenes: setup, kinetic power, belt processing, lens changes, sequential automation and redstone.

## Starter recipes

| Lens | Input | Output |
|---|---|---|
| Power | Sand | Glass |
| Power | Clay ball | Brick |
| Convergence | Magma block | Obsidian |
| Splitting | Amethyst shard | Quartz |
| Eclipse | Quartz | Prismarine crystals |
| Growth | Wheat seeds | Wheat |
| Convergence | 4 amethyst shards | Amethyst block |
| Splitting | Amethyst block | 4 amethyst shards |
| Power | Obsidian | Crying obsidian |
| Eclipse | Sculk | Echo shard |

These are standalone examples. Modpacks can replace them with their own progression. Towns & Kingdoms is not required.

[Custom recipes and KubeJS](docs/RECIPES.md) · [Recipe reference](docs/RECIPES.md)

## Requirements

Minecraft 1.21.1, Java 21, NeoForge 21.1.219 or newer, and Create 6.0.10 (6.0.x). Use Create's required matching dependencies. JEI, EMI and KubeJS are optional.

Current source version: **0.4.0**. See the [release notes](CHANGELOG.md) for changes.

The specialist lenses are recipe selectors. Their starter recipes demonstrate growth, consolidation, splitting, energizing and sculk processing; datapacks and KubeJS can replace them. The obsidian Power recipe requires 64 RPM and the Eclipse example requires 128 RPM.

**Updating from 0.2.0 or 0.2.1:** connect power to either horizontal shaft end. The top no longer accepts a shaft or large cog drive. Belt/Depot height and the lantern position stay the same; the five named lenses retain their item IDs.

**Updating to 0.4.0:** Red, Blue, Purple and White lenses have been removed. Replace them with Power, Convergence, Splitting and Eclipse respectively; the earlier starter transformations now use those lenses. Removed lens items cannot be recovered after loading an old world.

## Consumables, KubeJS and assemblies

Version 0.4.0 supports per-recipe catalyst items, hex colors, optional consumption, top hopper feeding, native KubeJS recipe builders and Create sequenced assembly. See [complete scripting examples](docs/KUBEJS.md). Bundled processing and crafting recipes can be disabled independently in `config/alchemicallaser-common.toml`.

## Pull up a chair

Questions, builds, feedback, server news and the occasional wildly overambitious factory: join the **Towns & Kingdoms Modding Hub**.

[![Join the Towns & Kingdoms Discord](https://i.imgur.com/Ojgcccm.png)](https://discord.gg/CRYk3uDPVt)

**[Join Discord](https://discord.gg/CRYk3uDPVt)** · [Explore all projects](https://mikaaah.github.io/TownsAndKingdoms/projects/) · [CurseForge projects](https://www.curseforge.com/members/mikaaah/projects)
