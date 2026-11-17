# Simplified Alchemical Laser artwork — 1.1.5

The supplied `Alchemical-Lasers-Full-Import-1.1.0 (2).zip` updates metal and fitting artwork. The simplification is in the textures: cuboid counts, dimensions, bones, pivots, animations and UV rectangles are unchanged. Creation Augmenter artwork and all eight lens sets are unchanged.

The import uses only the five files listed in the package’s `docs/RESOURCE_UPDATE_FILES.json`, plus the main Alchemical Blockbench project and its five lens variants. The four atlas pages are metal, copper, detail and the Gecko companion. The machine JSON removes `preferred_texture`, `preferred_uv` and `preferred_size` metadata from 270 faces on 45 cubes. Java renderers already use the supplied atlas consistently.

The existing native Create shaft, extended nozzle, three-block processing clearance, face occlusion, transparent-cutout handling and glow render states remain in place. No reference Java, Ponder, translation or build files from the package were imported.

Source ZIP SHA-256: `9a693526124ac130f1429a209ed29ecedaac52a852a07fe2b15409ea5ab32711`.

Imported file hashes (SHA-256):

| File | SHA-256 |
| --- | --- |
| `src/main/resources/assets/alchemicallaser/geometry/machine.json` | `c3a8733f61288088f2cef0d016df4eb404f36b0387280bd2b649d048c4b396d0` |
| `src/main/resources/assets/alchemicallaser/textures/block/copper_atlas.png` | `2a7b9aa118badbf972d802fa69dba14609b88204a6aba1b83093b7e14ee88cac` |
| `src/main/resources/assets/alchemicallaser/textures/block/detail_atlas.png` | `2980872b782fc3f51dbfb8393f0c19e79489f464422d9e2e068c6ced30979105` |
| `src/main/resources/assets/alchemicallaser/textures/block/gecko_atlas.png` | `ee212b884652ca7c8fa360e3b9fbbd731fb7222695f2111daae363741c24335e` |
| `src/main/resources/assets/alchemicallaser/textures/block/metal_atlas.png` | `8c555f7658be29004b15aef17ebaf83cdf95bbcd311f9526a3b23f0aa5b529e4` |
| `art/alchemical-laser/Alchemical-Laser.bbmodel` | `8a3fbb57d2cc0f1d6e8c9edff89c6facf1c3d9a0e0e346535bc00a2fd876db5c` |
| `art/alchemical-laser/lens-variants/Alchemical-Laser-Convergence.bbmodel` | `e884a495d4f4732e21196d9c60ff25deeeec1ce32a989faed42d349934003625` |
| `art/alchemical-laser/lens-variants/Alchemical-Laser-Eclipse.bbmodel` | `8969ea06abf8b2c23e5f63ebd4da8c2203f9cf466561c26006447900f923a4e4` |
| `art/alchemical-laser/lens-variants/Alchemical-Laser-Growth.bbmodel` | `e5fe89ae7eda2d790e170f971fc45f52f261d57fe349061ea0f25d42119733a2` |
| `art/alchemical-laser/lens-variants/Alchemical-Laser-Power.bbmodel` | `edad7d7ed3f5a58aacddea1e4ab47da10e196a79fe8deee6a6789e5acbcf4225` |
| `art/alchemical-laser/lens-variants/Alchemical-Laser-Splitting.bbmodel` | `989cf78ca9cfbeb77b31639a820cd211884f906a43d075693e120c60c686463d` |
