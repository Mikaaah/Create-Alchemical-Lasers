# Verified external materials

The model geometry is original. Vanilla/Create material texels, where copied into the packed edit/runtime sheets, retain their original authorship; original machine geometry and custom details are identified separately in the material map.

## Copper housing revision

The 31px model uses Minecraft 1.21.1 `copper_block` and `cut_copper` texels,
plus Create 6.0.10-280 `fluid_tank`, `fluid_tank_top` and `copper_casing`
texels for the upper housing. The verified Create Fluid Tank resource names
are `fluid_tank` and `fluid_tank_top`. The housing keeps the existing brass
trim and bolt geometry. No train-casing material remains in the active model.

`COPPER_MATERIALS.md` describes the mapping; `copper-material-audit.json`
records the source asset paths and SHA-256 hashes. The amethyst, gimbal and
open-cage materials remain unchanged. Existing Minecraft/Create attribution
and asset ownership apply to these texels.

## Create Nuclear

Official source: https://github.com/Create-Nuclear-Team/CreateNuclearNeoForge

Verified commit: `20a0aa7a90efd332b9a733b0483dee9bfc95780e`.

| Material ID | Dimensions |
| --- | --- |
| `createnuclear:block/steel_block` | 16×16 |
| `createnuclear:block/lead_block` | 16×16 |
| `createnuclear:block/reactor/casing/reactor_casing` | 16×16 |
| `createnuclear:block/reactor/core/reactor_core_casing` | 16×16 |

These PNGs were read directly from the pinned official repository. The repository license is GPL-3.0; the unmodified license is included in `licenses/Create-Nuclear-GPL-3.0.txt`. Packed textures derived from these inputs retain these notices and source provenance. `verified-external-materials.json` records exact source URLs and SHA-256 hashes.

## Create Aeronautics

Official source: https://github.com/Creators-of-Aeronautics/Simulated-Project

The official Modrinth project `oWaK0Q19` links to that repository. Verified commit: `5cc7aa333688a84603a4b0316e6d92a1efda653a`.

| Verified material ID | Dimensions |
| --- | --- |
| `aeronautics:block/metal_bearing_plate` | 32×32 |
| `aeronautics:block/propeller_bearing_side` | 16×16 |
| `aeronautics:block/gyroscopic_propeller_bearing_side` | 16×16 |
| `aeronautics:block/steam_vent/steam_vent_head_brass` | 16×16 |

The official license explicitly reserves all rights to the `assets` directories, while licensing the code under MIT. Consequently the shipped default aerospace detail sheet is original artwork. These verified IDs are appropriate for optional direct resource references when the original mod supplies the textures. Aeronautics source PNGs are excluded from the deliverable. See `licenses/Aeronautics-asset-license-reference.md` for the exact source license text.

IDs here are verified paths, not guessed names. Verify the same paths exist if targeting a different installed Nuclear or Aeronautics build. No external block models are copied.
