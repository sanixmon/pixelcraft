# Contributing to PixelCraft

Thank you for contributing to **PixelCraft**! This guide covers local setup, testing conventions, and pull request checks.

## Prerequisites

- **JDK 17** (Temurin 17 recommended)
- **Android SDK 35** (`compileSdk = 35`, `targetSdk = 35`, `minSdk = 28`)
- Gradle Wrapper (`./gradlew` included in repository)

## Architecture Overview

See [`docs/architecture.md`](docs/architecture.md) for package boundaries, state management, rendering pipeline, and storage formats.

Key conventions:
- **Brand-neutral copy & comments**: Keep all UI strings, preset names, and source comments original to PixelCraft.
- **Pure domain logic**: Place math, snapping, history, layer operations, and serialization in `domain/` without Compose dependencies so JVM unit tests run fast without an emulator.
- **Atomic persistence**: Always write files via `DraftStore.writeAtomically` (`AtomicFile`) and store image references as relative filenames (`<uuid>.jpg` / `<uuid>.png`).
- **Shared renderer**: Preview (`PixelCraftCanvas`) and export (`ImageExporter`) share `CanvasRenderer` and `ImageRenderer` so exported images match the canvas 1:1.

## Running Verification Locally

Run the JVM unit test suite before opening a PR:

```bash
./gradlew testDebugUnitTest --no-daemon
```

GitHub Actions CI automatically runs:
1. `./gradlew testDebugUnitTest`
2. `./gradlew lintDebug`
3. `./gradlew assembleDebug`

## Pull Request Checklist

- [ ] Unit tests added or updated in `app/src/test/java/com/app/pixelcraft/`
- [ ] `./gradlew testDebugUnitTest --no-daemon` passes cleanly
- [ ] User-facing strings localized in `res/values/strings.xml` and `res/values-in/strings.xml`
- [ ] Backward compatibility preserved for saved `ProjectData` JSON snapshots (`ignoreUnknownKeys = true`, default field values)
