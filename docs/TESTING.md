# Server verification

Java 21, NeoForge 21.1.219 and Create 6.0.10-280 are pinned by the build.

`./gradlew build compileGameTestJava runGameTestServer` runs the processing/kinetic regressions. The KubeJS-specific test exits early if KubeJS is absent.

To exercise native scripting, install these optional test mods in `run/mods`:

- KubeJS `2101.7.2-build.379`
- Rhino `2101.2.7-build.81`
- KubeJS Create `2101.3.1-build.18`

Copy `tools/test-kubejs/server_scripts/laser_smoke.js` into `run/kubejs/server_scripts/`, leave both common-config recipe switches true, and run `./gradlew runGameTestServer`. The suite checks the typed uppercase builder, positional constructor, lowercase aliases, nested assemblies, and execution of a consuming scripted recipe.

For the separate config test, set both default recipe switches false in `run/config/alchemicallaser-common.toml`, retain the same test scripts/mods, then run `./gradlew runConfigTestServer`. It checks bundled processing/crafting recipes are absent while scripted recipes survive, including one using the addon's namespace. Restore both switches afterward.

`python3 -m pip install -r tools/requirements-dev.txt` installs the resource-check dependencies. Run `python3 tools/verify_resources.py` to validate assets, recipes, structures and translation keys.

GameTests and their structures live in `src/gameTest`, separate from production sources. `./gradlew verifyReleaseJar` (also part of `check`) verifies packaging and version metadata. Java formatting is checked using google-java-format 1.28.0. CI runs the suite in both core-only and scripting installations, and tests recipe switches in the scripting job.

The regression suite covers side-shaft orientation, belt directions, speed limits, Depot/dropped-item bulk conservation, catalyst exhaustion/refills, redstone/power/obstruction interruptions, codecs, assembly loops, invalid catalyst IDs, defensive item-stack ownership and catalyst NBT reloads. A passing KubeJS-specific test without KubeJS installed is a skip, not scripting verification.

Graphical-client review of JEI/EMI layouts, beam coloring and Ponder playback remains manual. Dedicated-server tests exercise recipe/network codecs and processing behavior, not rendering.
