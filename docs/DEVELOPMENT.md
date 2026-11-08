# Development


## Layout

| Path | Responsibility |
|---|---|
| `src/main/java/.../LaserBlockEntity.java` | Belt/Depot and dropped-item cycles, catalyst inventory, synchronization |
| `src/main/java/.../recipe` | Recipe codecs, bulk operations, Create assembly support, default-recipe condition |
| `src/main/java/.../compat` | Optional JEI/EMI categories and assembly rendering |
| `src/main/java/.../client` | Client renderer, particles and beam sound |
| `src/main/java/.../ponder` | Six tutorial scenes |
| `src/main/resources` | Shipped textures, models, sounds, recipes and KubeJS schema |
| `src/gameTest` | Test code and fixture structures; excluded from release JARs |
| `docs` / `wiki` | Developer reference and published player guides |

Pinned development dependencies are Minecraft 1.21.1, NeoForge 21.1.219, Create 6.0.10-280, Ponder 1.0.82, Flywheel 1.0.6 and Registrate MC1.21-1.3.0+67. JEI and EMI are compile-only integrations; KubeJS uses a data-driven schema without a Java dependency.


Java source uses google-java-format 1.28.0. CI checks formatting, compiles main/test code, verifies resources and packaging, and runs the core GameTests. Add regression coverage for inventory conservation, interruptions and recipe/assembly changes. Avoid requiring optional integrations from common startup code.

Assets are checked in and building does not run generators. Historical asset generators under `tools` are development references, not a complete regeneration pipeline; they can overwrite later resources. Do not run them over a release checkout. The original development plan is archived under `docs/archive` and is not the current feature specification.

The source license and permitted third-party asset use are documented in [LICENSE](../LICENSE) and [notices](../THIRD_PARTY_NOTICES.md).
