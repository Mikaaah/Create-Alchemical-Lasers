# Actual simplified Create models — 1.1.6

Version 1.1.5 imported the older texture-only package. This revision replaces both machine bodies with the correct simplified geometry from the current `Alchemical-Lasers-Full-Import-1.1.0.zip`.

| Machine | Previous cuboids | Simplified cuboids | Rest bounds |
| --- | --- | --- | --- |
| Alchemical Laser | 214 | 31 | −8, −15, −8 to 8, 16, 8 |
| Creation Augmenter | 147 | 34 | −8, −15, −8 to 8, 16, 8 |

The import applies all 23 files in the supplied resource-update overlay, plus ten complete Blockbench projects and two Gecko atlas-layout exports. It includes broad native casing shells, straight supports, square gimbals, compact emitter tiers and single-piece jaws. Matching animations reduce core/gimbal travel for the smaller chamber and hold Creation Core scale at one.

Both machines keep their original bone names, 31px resting height, lens socket positions, three-block processing clearance, native Create shaft rendering and 1.1.4 face/cutout/glow fixes. All eight lens sprites, socket geometry and the animated 25-frame Creation Core strip remain unchanged. Reference Java, recipes, translations and Ponder files were not imported.

The asset validator now explicitly requires 31 and 34 machine cuboids, in addition to UV, dimensions, bone, animation, sound and texture checks.

Source ZIP SHA-256: `fecde2b99d06398bf831439954f3e43db8d2bc9803c37750ff68d4ae9e7afa0c`.

Native and Gecko lens-removal/insertion tracks and all ten Blockbench projects also retarget the short jaws: the old two-pixel outward slide is removed and opening rotation is halved to 17.5 degrees. Closed-pose geometry and lens placement remain exact; the jaws stay attached to the nozzle while open.
