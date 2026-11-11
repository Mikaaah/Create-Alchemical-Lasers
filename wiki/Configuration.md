# Configuration

## Bundled recipes

`config/alchemicallaser-common.toml` applies to the installation. Restart after changing these values.

```toml
[recipes]
    enableDefaultProcessingRecipes = true
    enableDefaultCraftingRecipes = true
```

The first switch controls bundled laser examples. The second controls the emitter and five lens crafting recipes. Custom datapack and KubeJS recipes remain enabled, including recipes in the mod's namespace. Install matching recipe settings on client and server.

## World processing settings

NeoForge stores `alchemicallaser-server.toml` in the world's `serverconfig` directory. For new-world defaults, place a copy in `defaultconfigs`.

| Setting | Default | Range | Effect |
|---|---|---|---|
| `minimumRpm` | 16 | 1–256 | Global minimum; the recipe's own minimum also applies |
| `maximumEffectiveRpm` | 256 | 16–4096 | Caps processing speed scaling, not accepted network RPM |
| `chargeTicks` | 12 | 1–200 | Fixed charging delay per batch |
| `stressImpact` | 8.0 | 0–1024 | Stress per RPM while connected, including idle |
| `soundVolume` | 0.65 | 0–2 | Multiplier for machine sounds |

At a constant speed, cycle ticks are `chargeTicks + ceil(duration * 32 / min(abs(RPM), maximumEffectiveRpm))`. Processing starts only when the absolute network speed meets both minimums and the emitter has no disabling redstone signal. Reloading a world restarts unfinished cycles without consuming input.

[Home](Home)
