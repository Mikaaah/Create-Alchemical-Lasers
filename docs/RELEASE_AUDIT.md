# 1.0 release audit

Audit date: 6 November 2026. Baseline: `7327f60` on the repository's main branch, source version 0.4.0. Candidate: **1.0.0-rc.1**, prepared on `release/1.0.0` without promoting main or publishing a stable release.

## Scope and changes

The review covered kinetic connections, Belt/Depot and dropped-item processing, item conservation, catalyst inventory and automation, recipe/network codecs, Create assembly interfaces, optional integrations, client effects/Ponder, resource packaging, configuration and public documentation.

| Finding | Resolution |
|---|---|
| Belt/Depot callbacks did not apply the obstruction check used for dropped items | Shared gap check now interrupts both processing paths; added stop/resume test |
| Bulk assembly code could mutate an item stack returned by Create's result supplier | Copy the supplied result before merging/splitting; regression verifies ownership and output counts |
| Public catalyst exchange retained the caller's mutable stack | Copy replacements into inventory; regression verifies caller mutations cannot change the installed count |
| Inventory identity was not initialized after NBT deserialization | Restore identity when reading inventory; regression checks persistence and first same-item refill |
| Unknown/air catalyst IDs decoded into unusable recipes | Validate registered non-air item IDs at datapack recipe decoding |
| Ponder installs lenses directly without running recipe color selection | Set the default lens color during manual/Ponder installation; active recipes still override it |
| Assembly text and catalyst help were literal English | Translation keys now cover assembly descriptions and catalyst tooltips |
| Optional JEI integration used APIs marked for removal | Use explicit category dimensions and rich-tooltip callbacks; removed deprecated subscriber bus annotation |
| Test classes and empty fixture structures shipped in production | Separate `src/gameTest` source/resources; automatic release-JAR exclusion check |
| Build and mod metadata carried independent version literals | Expand metadata from the Gradle project version; verify packaged version |
| Matching recipes were sorted on each uncached ordinary lookup | Select the minimum matching ID directly, preserving priority without a full sort |
| Documentation mixed historical and current behavior | Rewrite guides/wiki, add configuration/field references, correct 0.3.0 lens removal history and archive the initial plan |
| Historical asset generators could overwrite later work when run casually | Require an explicit overwrite flag; document their snapshot status |
| No repository build workflow or report template | Add formatting/resource/build/packaging/core/scripting/config CI and a focused bug template |

Java production and test sources use google-java-format 1.28.0. The main Java compilation completed without the previous removal warnings. Gradle/third-party test APIs may still emit their own development warnings.

## Validation

| Check | Result |
|---|---|
| JDK 21 Gradle build, sources JAR and test compilation | Passed |
| Core dedicated-server suite without optional scripting mods | 52 test cases pass; the KubeJS-specific case exits early in this installation |
| Dedicated-server suite with KubeJS/Rhino/KubeJS Create | All 52 cases pass, including the actual scripting assertions |
| Bundled processing/crafting disabled | Separate config test passes; custom recipes survive, including the addon namespace |
| Script load | One smoke script loads with zero errors/warnings; uppercase/lowercase/positional builders, omitted color and nested assemblies verified |
| JSON, textures, model geometry, sounds, NBT structures, recipes and translations | Resource validator passes |
| Release packaging and metadata | No test classes or empty fixtures; candidate version matches the Gradle version |
| Java formatting and `git diff --check` | Pass |

Test coverage includes both belt directions; side-shaft orientations; 16–256 RPM and below-minimum behavior; full stacks; counted-input remainders; oversized outputs; secondary rolls; component copying; dropped stacks sharing catalysts; hopper feeds; refill/persistence behavior; wrong lenses; redstone/power/obstruction interruptions; assembly loops and recipe/network codecs. The test suite runs against production classes; packaging is checked separately. It is not a substitute for playing the release JAR in a graphical client.

Tested dependencies: Minecraft 1.21.1, NeoForge 21.1.219, Create 6.0.10-280, Ponder 1.0.82+mc1.21.1, Flywheel 1.0.6 and JDK 21.0.13. Scripting run: KubeJS 2101.7.2-build.379, Rhino 2101.2.7-build.81 and KubeJS Create 2101.3.1-build.18. The broader dependency ranges declared in metadata were not exhaustively tested.

## Compatibility

The candidate retains `alchemicallaser`, its recipe type and existing fields, the five named lens IDs, inventory `Lens` NBT, COMMON/SERVER config keys, brass housing, lantern, horizontal side-shaft power and processing height. Optional scripting/viewer mods remain optional. No Create/Create Deco runtime dependency was added beyond the existing Create requirement, and the asset notices/license are preserved.

Invalid catalysts now fail with a recipe-loading error instead of silently producing unusable recipes. A block placed in the beam gap now also stops belt/Depot work. Unfinished work restarts without consuming input; active cycle progress is intentionally not persisted across world reloads.

## Remaining stable-release checks

Before promoting this candidate to stable **1.0.0**, perform a graphical client pass on the packaged JAR: both emitter orientations and lens rendering, beacon colors, JEI/EMI layout/tooltips and assembly displays, all six Ponder scenes, audio start/stop and chunk unload/reload. These checks were not performed in this audit. The candidate is intentionally marked RC rather than claiming graphical release verification.

GitHub Actions is configured to reproduce both test installations and the config run. Local validation above does not imply a hosted workflow has already passed. See [testing instructions](TESTING.md) for commands and [changelog](../CHANGELOG.md) for player-facing changes.
