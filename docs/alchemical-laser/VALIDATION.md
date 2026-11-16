# Copper refinement validation — 16 November 2026

- Gradle `build` passed with Java 21, NeoForge 21.1.219, Create 6.0.10-280
  and Flywheel 1.0.6. The final build completed in 11 seconds. Release-JAR
  checks confirmed version 1.1.0 and excluded GameTest classes and fixtures.
- All 53 existing required NeoForge GameTests passed. These cover belt/depot
  processing, power interruption, catalyst handling, serialization and manual
  remove-first lens changes. The complete server run took 46 seconds.
- Resting model height is 31px. The emitter attachment remains at Y=-6;
  the barrel, jaws, five socketed lenses and their pivots align at the new
  lower muzzle. Ignition recoil is 2.25px.
- The amethyst, gimbal, cage struts and blast-shield cube records and sampled
  texels were compared against the current baseline JAR. Their approved
  geometry and art are unchanged. Amethyst animation tracks are unchanged.
- Twenty upper-housing cuboids / 120 faces use verified copper and Fluid Tank
  texels. No train-casing reference remains in active model assets. All 1,164
  other machine face crops are unchanged by the material patch.
- All 1,500 UV islands across the machine and five lens overlays are inside
  their texture pages and do not overlap. All six Blockbench projects have
  valid UUIDs, hierarchy, embedded textures and nine native animation clips.
  Companion Gecko exports use exactly the same source texture pages.
- All ten lens inventory PNGs, including emissive masks, are byte-identical
  to the current baseline. The four custom beam/ignition/lens audio files
  regenerate to identical decoded PCM. The updated audition uses the actual
  Vanilla lava-extinguish sample at pitch 1.15.
- The muzzle and fixed impact helpers align the beam and particles with the
  longer emitter. Soul emissions, continuous beam smoke and flames are absent.
  Shutoff creates a small, bounded smoke budget at both endpoints, with exact
  initial upward velocity 0.02–0.04, and one server-side cooling hiss.
- Packaged runtime resources match the source tree byte for byte. Model
  previews were rendered from the supplied geometry and actual UV atlases,
  then visually inspected.

`REFINEMENT_AUDIT.json`, `EMITTER_EXTENSION_AUDIT.json`,
`copper-material-audit.json` and `EXPORT_AUDIT.json` record the comparisons.
The build and GameTest logs are included alongside them.

The GameTests run on a dedicated server. They do not validate client shader
appearance or listen to audio. The included images are model renders and the
sound preview is an audition mix; this revision has not been visually tested
in a running Minecraft client.
