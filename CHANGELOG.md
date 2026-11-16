# Changelog

## 1.1.4

- Removed overlapping opaque face regions and hidden internal joins from both machine renderers while retaining the original UV mapping, transparent cutouts and independent animated parts.
- Kept the supplied Alchemical Laser atlas on claws and braces instead of silently switching to optional third-party texture files.
- Culled opaque backfaces and removed unnecessary translucent sorting from glow masks backed by solid surfaces. Added geometry regression checks for open claws, partial overlaps, glass and rotated parts.

## 1.1.3

- Imported the revised Alchemical Laser metal, copper and compatibility atlases, with cleaner metal shading and matching embedded Blockbench texture pages.
- Retained the extended model, native Create shafts, three-block processing clearance, animations and recipe compatibility.

## 1.1.2

- Replaced both machines’ stretched custom shaft models with Create’s native shaft, including its proper end-cap texture and horizontal-axis rotation.
- Unified Flywheel, fallback and inventory shaft rendering; removed the duplicate authored shaft from inventory views.

## 1.1.1

- Moved Alchemical Laser belt/Depot processing three blocks below the housing, matching the Creation Augmenter's clearance and restoring a full visible beam beneath the longer nozzle.
- Moved dropped-item processing, beam impacts and completion effects down one block; solid blocks in either gap cell now stop processing.
- Updated the Ponder setup and placement documentation. Existing laser setups need the emitter and side drive raised one block, or the processing line lowered one block.

## 1.1.0

- Integrated the revised 31px copper/fluid-tank laser and dark-steel Creation Augmenter with editable models, atlases, emissive masks and animated rainbow core.
- Added Creation Lenses with 10/100/100 uses, atomic wear/shattering, built-in Unbreakable equipment infusion and one-item processing on Depots or grounded stacks.
- Added `creationaugmenter:creation_augmenting` datapack/network codecs, time/RPM requirements, native KubeJS schema, optional JEI/EMI displays, standalone endgame crafts and four example recipes.
- Required 128 RPM and 64 SU/RPM for Creation; preserved existing alchemical IDs, automation and scripts. Both machines use horizontal instanced shaft visuals, delicate smoke and the vanilla lava-extinguish hiss.
- Passed the build, 65 dedicated-server GameTests and asset validators. See [Creation guide](docs/CREATION_AUGMENTER.md) for scope and remaining optional-client checks.

- Imported the updated bordered lens artwork and matching default beam/particle colors.
- Added respawn-anchor charging, a custom heavy beam loop, ignition and smoke-discharge hiss, plus separate lens insertion/removal sounds. Charge and beam audio are state-controlled with short fades and stop when the machine unloads.

- Lens replacement is now two-step: sneak-right-click with an empty hand to remove it and open the claws, then insert a new lens to close them. Direct right-click swapping is blocked; automated catalyst refills remain supported.

- Replaced the laser body and all five lens sprites/socket models with the supplied industrial artwork.
- Added authored power-up, spin, power-down, lens clamp, ignition recoil, vent and idle animations, with independent state for each machine.
- Added multi-texture glass/emissive rendering and the complete machine model in inventory and hand views.
- Added layered lens/recipe-colored beams, animated lens particles, impact rings and collision-aware embers.
- Added Flywheel shaft instancing and a conventional rendering fallback for both horizontal drive axes.
- Preserved the existing block/lens IDs, saved inventories, recipes, config switches, hopper feeding, bulk processing, KubeJS and sequenced assemblies.
- Kept existing belt/Depot spacing and updated the model outline and beam origin for the larger nozzle.
- Added geometry/animation/particle validation and retained supplied material provenance. No new runtime dependency is required.

See [integration and validation notes](docs/MODEL_1.1.0.md).

## 1.0.0-rc.1

- Fixed processing through a blocked beam gap on belts/Depots.
- Protected assembly outputs and installed catalysts from shared mutable item stacks.
- Preserved catalyst identity after NBT reload so same-item refills keep active progress.
- Rejected missing/air catalyst IDs with a recipe-loading error.
- Corrected lens colors in Ponder previews and translated assembly/catalyst text.
- Updated JEI integration to its current size and rich-tooltip APIs.
- Cleaned and formatted Java source; recipe selection avoids sorting the full match list.
- Removed GameTests and fixture structures from shipped JARs; added packaging checks and synchronized version metadata.
- Reworked player/developer documentation, configuration reference, wiki, contributor guidance and automated core/scripting verification.

This is a release candidate for Minecraft 1.21.1 / NeoForge / Create 6.0.10. See [release audit](docs/RELEASE_AUDIT.md) for exact checks and outstanding graphical-client review. Current five-lens item IDs, recipe fields, catalyst inventory and side-shaft layout are preserved.

## 0.4.0

- Added native KubeJS builders with LENS, COLOR, CONSUME, INPUT and OUTPUT fields, plus lowercase aliases and a positional constructor.
- Added per-recipe colors and optional catalyst consumption for any item. Bulk processing is capped by available consumables; unfinished input stays behind.
- Added Create sequenced assembly steps with catalyst checks, intermediate progression, loops and final outputs.
- Catalyst inventory accepts stacked items and hopper insertion from above. Container remainders are retained or returned.
- Added independent common-config switches for bundled processing and crafting recipes. Custom recipes remain enabled.
- JEI/EMI distinguish consumables from reusable catalysts; JEI assemblies display the laser and catalyst.
- Added server regression coverage for consumption, interruptions, top hoppers, assembly loops and recipe synchronization.

Validation: 46 server GameTests passed with KubeJS 2101.7.2-build.379, Rhino 2101.2.7-build.81 and KubeJS Create 2101.3.1-build.18. The same suite passed without KubeJS (the scripting-only case skips in that installation). A separate config test passed with both bundled recipe switches disabled and confirmed custom recipes survive. Native builders loaded with zero failed recipes. Resource/schema checks and the Gradle build passed. Graphical JEI/EMI review remains manual.

## 0.3.0

- Process every complete recipe quantity in a held stack in one cycle, on Mechanical Belts and Depots. Incomplete quantities remain, outputs split into legal stack sizes, and secondary chances roll independently per recipe operation.
- Added bulk processing for multiple dropped item stacks directly under the nozzle. Inputs stay intact until completion; moving the stack, changing the lens, power loss, redstone and obstructions cancel unfinished work.
- Replaced the custom laser tube with Minecraft’s native animated beacon core and halo, colored by the installed lens’s synced RGB data. Retained the brass housing, industrial lantern and horizontal side shaft.
- Kept only Growth, Convergence, Splitting, Power and Eclipse lenses. Removed Red, Blue, Purple and White items, recipes, models, textures and translations. Their starter transformations now use Power, Convergence, Splitting and Eclipse respectively. Replace old colored lenses before upgrading a world.
- Updated Ponder tutorials, asset generators, model preview and recipe documentation for the five-lens bulk system.

### Verification

The Gradle build and all 38 dedicated-server GameTests passed on Minecraft 1.21.1, NeoForge 21.1.219 and Create 6.0.10-280. Coverage includes 64-item belt/Depot cycles, counted-input remainders, 256-item splitting outputs, independent secondary chances and copied components, multiple dropped stacks with normal gravity, interruptions, moved items, obstructions and restart, all five lenses and the existing kinetic regressions. Resource validation and a packaged-JAR audit passed: exactly five lens models and no removed color-lens or custom beam assets. Native beacon rendering compiles against the vanilla API; graphical-client review remains manual.

## 0.2.2

- Replaced the controller top with a compact solid brass housing, matching the retained industrial lantern. Added steel collars around the existing horizontal axle.
- Restored ordinary Create shaft power from either end of the horizontal axle, including wrench rotation and both horizontal axes. Removed the overhead cog connection, custom kinetic mixin and controller model wrapper.
- Kept the lantern position, nine lenses, processing recipes, sounds and beam behavior. Updated the Ponder shaft layout, setup instructions and preview.

### Verification

The build and all 26 dedicated-server GameTests passed, including both side inputs, a rotated emitter, rejection of vertical shafts and the removed overhead cog drive, all five specialist lenses, belt/Depot processing, speed limits, redstone and item conservation. Resource validation passed. The preview uses actual model geometry/textures; graphical-client review remains manual.

## 0.2.1

- Kept the industrial lantern optics and replaced the andesite top cap with Create’s native Rotation Speed Controller top fittings, referenced directly from its baked model. No Create model or textures are bundled.
- Added a working friction drive from a horizontal large cogwheel placed directly above, with its axle perpendicular to the emitter’s roller. Input and emitter RPM are 1:1. Plain shafts, small cogs and incorrectly oriented cogs cannot power it.
- Automatically aligns placement beneath an existing horizontal large cog. Wrench rotation and either horizontal axle orientation are supported.
- Disconnects power when the drive cog is removed or rotated out of alignment.
- Moved the unchanged lantern down six pixels to preserve Create’s native roller/cog contact geometry. Belt/Depot placement stays at the same height; lens, beam and charge particles follow the lowered nozzle.
- Updated the Ponder schematic, setup explanation, model preview and upgrade instructions.

### Verification

The build and all 27 dedicated-server GameTests passed, including the five new cog-drive regressions and all existing lens, belt/Depot, speed, redstone and conservation tests. The client model-wrapper check passed for both axes, retaining the lantern and 44 native controller rail quads through inventory, fixed-display and chunk render paths. The textured preview is a software render; graphical-client lighting and Ponder playback remain manual review items.

## 0.2.0

- Rebuilt the emitter around Create Deco’s permitted industrial cage lantern, with an andesite drive cap, brass focusing bands, and a recessed optical cartridge. Credits are in `THIRD_PARTY_NOTICES.md`.
- Moved the shaft connection to the top with a vertical rotation axis; side and underside shaft connections are closed. Move existing power connections above the emitter when upgrading.
- Removed the exposed lower cog and free-spinning floating lens. The lens now seats horizontally in the nozzle as a shallow 3D cartridge.
- Refined the four existing lens icons and added Growth, Convergence, Splitting, Power, and Eclipse lenses, including survival crafting, beam colors, audio pitches, Ponder registration and starter processing recipes.
- Updated all six Ponder scenes for overhead power and nine lenses. Existing block and colored-lens IDs remain stable.

### Verification

The Gradle build and all 22 dedicated-server GameTests passed on Minecraft 1.21.1, NeoForge 21.1.219 and Create 6.0.10-280. This includes overhead-only shaft connectivity, all five specialist processing recipes, reusable lenses, insufficient convergence input, and the existing belt/Depot, speed, redstone and item-conservation regressions. JSON, model/texture bindings, lens crafting/data-map entries, Ponder structure and translations also passed resource checks.

The preview is a software render of the actual model and textures. In-game graphical lighting, lens inventory presentation, audio, Ponder playback and optional recipe-viewer layouts still require a graphical-client review.

## 0.1.0

- Apply supplied branding: GitHub banner and in-game mod information logo.

First implementation of Create: Alchemical Lasers for NeoForge 1.21.1.

- Added the kinetic Laser Emitter, four reusable optical lenses and survival crafting recipes.
- Added native Create belt/Depot processing, recipe quantities, safe stack remainders, optional secondary outputs and item component copying.
- Added redstone disabling, goggles information, wrench support and a lens inventory capability.
- Added original textures, models, continuous beam rendering, mechanical animation, particles and seven synthesized sound assets.
- Added datapack/generic KubeJS recipes, JEI/EMI categories and six Ponder scenes with a custom structure.
- Added server configuration and automated in-game regression coverage.

### Verification

The Gradle build passed and all 15 dedicated-server GameTests passed against NeoForge 21.1.219 and Create 6.0.10-280. Coverage includes both belt directions; Depot conversion and stack conservation; 16, 32, 64, 128 and 256 RPM; below-minimum speed; redstone disable/restart; power-loss input retention; lens exchange; counted ingredients; component copying; guaranteed secondary outputs; and recipe codec roundtrips. Resource validation also passed.

Graphical playtesting, Ponder playback review, sound review, multiple-client synchronization, chunk unload/reload and optional viewer/KubeJS installation checks have not been performed. These checks remain necessary before treating this as a polished production release.

### Behavior notes

Processing uses Create's standard press spacing (one air block above the belt/Depot). Stress is reserved continuously while the emitter is connected, matching Create's kinetic accounting. Interrupted cycles restart without consuming partial inputs; no partial progress is persisted across world reloads. Lenses can be inserted and extracted through item-handler automation; Mechanical Arm and Deployer interactions have not been claimed as tested.
