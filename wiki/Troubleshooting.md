# Troubleshooting

| Symptom | Check |
|---|---|
| Nothing processes | Matching recipe and catalyst; clear air gap; enough RPM; no disabling redstone signal |
| Sand does not turn into glass | Power Lens installed and bundled processing recipes enabled, or a matching pack recipe |
| Network overstressed | Default stress impact is 8 SU per RPM, including while idle |
| Only part of a stack changes | Recipe input quantity and available consumable count |
| Top shaft/cog does not power it | Current emitter uses the horizontal side shaft |
| Assembly intermediate waits | Correct next step, installed catalyst, input count of one, no secondary output |
| KubeJS Create crashes | Use a build compatible with Create 6.0.10; see scripting guide for the tested combination |
| Recipe rejected on reload | Check item IDs, non-air catalyst, six-digit hex color, field types and ranges |

Interrupted work retains its unconsumed input and can restart on a Depot. A belt item may continue downstream after interruption.

The four old colored lenses were removed in **0.3.0**, not 0.4.0. Replace Red/Blue/Purple/White with Power/Convergence/Splitting/Eclipse before loading older saves. The 1.0 candidate retains the five current lens IDs, catalyst inventory NBT and recipe type.

Report exact Minecraft, NeoForge, Create and addon versions, a minimal recipe/setup and relevant logs. Dedicated-server test results do not verify graphical rendering or optional viewer layouts.

[Home](Home)
