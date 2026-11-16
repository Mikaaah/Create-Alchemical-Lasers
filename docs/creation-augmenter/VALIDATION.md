# Creation Augmenter validation — 16 November 2026

The unchanged Creation Java source compiled in the standalone baseline with
Java 21, Mojang-mapped Minecraft 1.21.1,
NeoForge 21.1.219, Create 6.0.10-280 and Flywheel 1.0.6. The final build took
20 seconds. Release checks excluded test classes and fixtures and verified
the packaged mod version. The combined delivery is an asset/source ZIP and
does not include that standalone JAR. Current lens and embedded-model checks
are recorded in the root `docs/COMBINED_ASSET_AUDIT.json`.

All five required dedicated-server GameTests passed for that code baseline:

- True Unbreakable component application preserves the input and its damage.
- A powered Create Depot completes augmentation without losing equipment.
- Redstone interruption preserves input and lens and returns the machine idle.
- Lens identity survives serialization without restoring a stale operation.
- Recipes enforce lens selection and copy explicit components without leaking
  the old item tier's default durability onto the new tier.

The 147-cuboid machine has 15 bones and is 31px tall at rest. Firing extends
the lower assembly 2.5px. Four editable Blockbench projects have valid UUIDs,
parents, five embedded texture sources and ten native animation clips.
The 1,050 static atlas islands are in bounds and non-overlapping. The 18 core
faces deliberately reuse supplied sprite crops; this mapping is recorded in
the material and export documentation rather than counted as unique UVs.

The original input is 16x384 / 24 frames. Its RGBA pixels are unchanged in
the first 24 runtime frames, with a copy of frame 1 appended as frame 25.
The emissive sheet matches, metadata parses, and the live texture and sampled
beam/particle palette share the same 50-tick interpolated world-time clock.
All three inventory sprites remain native 16x16 with valid masks and metadata.

All runtime resource JSON and animation metadata parse successfully. The
packaged runtime resources match the source tree byte for byte, including
material provenance. Rendering uses modern vertex methods. No Soul or large
smoke emissions are present. Create's actual removal chain was inspected to
verify installed lenses drop when the machine is broken.

Current model stills and all three shader beam profiles were refreshed using
the refined lens assets and visually inspected. The included beam preview is
five seconds at 12fps. The previous model-animation movie/GIF is omitted;
all ten editable animation clips remain in the Blockbench and JSON exports.
These are offline previews; this revision has not been visually or audibly
tested in a running Minecraft client.

See `VALIDATION.json`, `EXPORT_AUDIT.json`, `geometry-validation.json`,
`LENS_ASSET_AUDIT.json`, `CORE_ANIMATION.json` and the included build/test logs.
