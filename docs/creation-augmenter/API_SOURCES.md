# API references

The project was compiled against the actual pinned Mojang-mapped Minecraft
1.21.1 / NeoForge 21.1.219 and Create 6.0.10-280 dependency classes. Dynamic
texture, particle, kinetic and Flywheel API signatures were inspected from
those dependencies as needed.

Primary references:

- NeoForge 1.21 rendering migration: https://docs.neoforged.net/primer/docs/1.21/
- Flywheel project and dependency setup: https://github.com/Engine-Room/Flywheel
- Create source: https://github.com/Creators-of-Create/Create

Vertex emission uses `addVertex`, `setColor`, `setUv`, `setOverlay`, `setLight`
and `setNormal`; it does not use `endVertex`.
