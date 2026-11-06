# Contributing

Discuss changes against the current branch and preserve existing lens IDs, side-shaft power, lantern geometry, recipe fields and optional integration behavior. Do not add progression locks or hard scripting/viewer dependencies.

Use JDK 21, run `./gradlew build compileGameTestJava runGameTestServer`, and run `python3 tools/verify_resources.py` after installing `tools/requirements-dev.txt`. See [testing](docs/TESTING.md) for optional KubeJS and configuration coverage. Format Java with google-java-format 1.28.0. Never report a skipped integration or a dedicated-server run as graphical client verification.

Keep changelog entries player-focused and document recipe/config compatibility. Include actual validation results in a PR. Changes to quantities, catalysts or assembly progression need tests demonstrating item conservation.

This repository is All Rights Reserved. Public availability does not grant redistribution rights; see [LICENSE](LICENSE). Supplied Create Deco assets have a separate permission scope in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
