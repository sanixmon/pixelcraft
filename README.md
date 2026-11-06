# PixelCraft

**PixelCraft** is an offline-first Android typography, vector graphics, and photo composition studio built with **Kotlin**, **Jetpack Compose**, and **Material 3**.

## Features

- **Home Workspace & Safe Autosave**
  - Project gallery with 256px WebP thumbnails, search, rename, duplicate, and Undo delete
  - Debounced `draft.json` autosave with crash recovery banner and ordered `BackHandler` protection
  - Portable `.pixelcraft` ZIP project archive export & import
  - 20 categorized starter templates (*Quote*, *YouTube*, *Promo*, *Greeting*, *Meme*, *Story*) and 20 curated gradients
- **Direct Canvas Manipulation**
  - Two-finger pinch-scale & twist-rotate on layers with `±4°` cardinal/diagonal snapping and haptic feedback
  - `0.5x–6x` canvas viewport zoom & pan, double-tap text editing, long-press context menu, and overlap tap-cycling
  - 48dp accessible corner & side handles (*Delete*, *Edit/Duplicate*, *Scale+Rotate*, *Width/Height Stretch*)
  - Screen-space magnetic grid & inter-layer Smart Guides + Social Safe Zone overlay
- **Rich Typography**
  - 15 bundled open-source fonts (SIL OFL 1.1 & Apache 2.0) + custom `.ttf`/`.otf` font import, search & favorites
  - Circular arc curved text (`-180°..180°`), letter & line spacing, and case transforms (`UPPER`, `lower`, `Title`)
  - Solid, linear, and radial text gradients, vector path contour strokes (*Outside / Center / Inside*, dash patterns, joins & caps), neon outer glow, drop shadows, 3D extrusion, and mirror reflection
  - Style Copy/Paste clipboard & persistent Saved Styles library
- **Vector Shapes & Photo Composition**
  - 24 procedural vector shapes across *Basic*, *Word Balloons*, *Arrows*, and *Badges*
  - Photo layers with non-destructive crop, brightness/contrast/saturation/warmth sliders, 8 photo filter presets, shape clipping masks, borders, and corner radius
  - Per-layer composite `BlendMode`, multi-select, layer grouping, and universal horizontal/vertical flip
- **Color & High-Resolution Export**
  - 2D HSV color wheel + `#AARRGGBB` hex input, Canvas Eyedropper pixel sampler, and recent color swatches
  - Memory-safe **PNG**, **JPEG**, and **WebP (Lossy & Lossless)** export up to 4K Ultra HD (`Pictures/PixelCraft`)
  - Selected-layer transparent sticker export, transparent border trimming, and direct `FileProvider` sharing without cluttering the device gallery
  - External `ACTION_SEND` / `ACTION_EDIT` / `ACTION_VIEW` (`image/*`) intent handling

## Tech Stack & Requirements

- **Language**: Kotlin 2.0 (`jvmTarget = 17`)
- **UI**: Jetpack Compose + Material 3 + Navigation Compose (`gradle/libs.versions.toml`)
- **SDK**: `minSdk = 28` (Android 9.0+), `targetSdk = 35`, `compileSdk = 35`
- **Persistence**: `kotlinx.serialization` JSON (`AtomicFile`) + Jetpack DataStore Preferences
- **Architecture**: [`docs/architecture.md`](docs/architecture.md)

## Testing & CI

Run JVM unit tests locally:

```bash
./gradlew testDebugUnitTest --no-daemon
```

GitHub Actions workflows:
- [`.github/workflows/ci.yml`](.github/workflows/ci.yml): Runs `testDebugUnitTest`, `lintDebug`, and `assembleDebug` on every push and pull request.
- [`.github/workflows/release.yml`](.github/workflows/release.yml): Runs `testReleaseUnitTest`, builds R8-minified signed `.apk` and Play Store `.aab` bundles, verifies signatures, and publishes GitHub Releases on `v*` tags.

## Documentation

- [Architecture Guide](docs/architecture.md)
- [Changelog](CHANGELOG.md)
- [Contributing Guide](CONTRIBUTING.md)
