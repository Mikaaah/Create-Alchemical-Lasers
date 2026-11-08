# Server verification

Java 21, NeoForge 21.1.219 and Create 6.0.10-280 are pinned by the build.


To exercise native scripting, install these optional test mods in `run/mods`:

- KubeJS `2101.7.2-build.379`
- Rhino `2101.2.7-build.81`
- KubeJS Create `2101.3.1-build.18`



`python3 -m pip install -r tools/requirements-dev.txt` installs the resource-check dependencies. Run `python3 tools/verify_resources.py` to validate assets, recipes, structures and translation keys.


The regression suite covers side-shaft orientation, belt directions, speed limits, Depot/dropped-item bulk conservation, catalyst exhaustion/refills, redstone/power/obstruction interruptions, codecs, assembly loops, invalid catalyst IDs, defensive item-stack ownership and catalyst NBT reloads. A passing KubeJS-specific test without KubeJS installed is a skip, not scripting verification.

Graphical-client review of JEI/EMI layouts, beam coloring and Ponder playback remains manual. Dedicated-server tests exercise recipe/network codecs and processing behavior, not rendering.
