# Creation Augmenter material provenance

All model geometry is original voxel construction. Minecraft1.21.1 and Create6.0.10-280 source PNGs supply the explicitly mapped metal, creative casing, shaft, brass and glass texels. Source paths and SHA-256 hashes appear in `material-map.json`. Create assets retain their upstream authorship and asset license; Minecraft assets retain Mojang ownership.

The three static512px atlas pages contain unique face islands with1px gutters. UVs retain native pixel density through cropping and tiling; small fittings retain authentic highlight and shadow colors. Rune, gilding and vent detail is original pixel art over the verified shadow-steel plate, using Create creative-motor/brass palette colors. Tinted-glass alpha is multiplied by0.23 so the core remains visible.

The supplied Creation Core is animated independently. Its three stepped crystal cuboids intentionally share small crystal-facet crops of the same16x16 logical frame, so no icon frame is rescaled into a sticker. This shared sprite mapping is the explicit exception to the otherwise non-overlapping static UVs. The original24frames are preserved and a copy of frame0 closes the requested25-frame strip. Runtime and emissive masks use the common core clock.

The core has an exact6x8x6 stepped envelope. Its narrow endcaps project0.5px beyond the7px open chamber into the input/output collars. Two voxel-octagonal gyros have opposing22.5degree inclinations; their Y rotations preserve chamber clearance.
