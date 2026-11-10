# Changelog

All notable changes to **PixelCraft** are documented in this file.

## [0.2.0] - Unreleased

### Added
- **Full English + Indonesian UI**: All ~430 user-facing strings (screens, dialogs, tool panels, shape/blend/filter/ratio names, undo history, snackbar messages) now come from `strings.xml` with complete `values-in` translations. ViewModel messages use a locale-aware `UiText`.
- **Rendering & gesture test suite**: Robolectric tests render real pixels through the export pipeline (size, quality scaling, layer positioning, z-order, JPEG/PNG transparency, sticker trim, encode round-trip) and drive real touch input through the canvas (drag, select-on-drag, pinch in/out, locked layers).
- **Home Screen & Navigation**: Project browser with 256px WebP thumbnails, search, rename, duplicate, delete with Undo, blank canvas ratio selector, "From Photo" quick start, and 20 categorized starter templates (`Quote`, `YouTube`, `Promo`, `Greeting`, `Meme`, `Story`).
- **Crash-Safe Autosave & Atomic Storage**: Debounced `draft.json` autosave with startup recovery prompt, `AtomicFile` writes for all project/index/draft files, portable relative image filenames (`schemaVersion = 2`), and 24-hour orphan image garbage collection.
- **Direct Multi-Touch Manipulation**: Two-finger layer pinch-scale & twist-rotate with `±4°` cardinal/diagonal snapping + haptic feedback, 48dp corner/side selection handles, double-tap text editing, long-press quick context menu, tap-to-cycle overlapping layers, and `0.5x–6x` canvas viewport zoom & pan.
- **Typography Phase 1–5**: Letter spacing, linear & radial text gradient fills, circular arc curved text (`-180°..180°`), neon outer glow, case transforms (`UPPERCASE`, `lowercase`, `Title Case`), custom `.ttf`/`.otf` font import with favorites & search, Copy/Paste Style clipboard, Saved Styles library, and flexible date/time stamp formats.
- **Photo & Layer Composition**: Non-destructive photo crop, brightness/contrast/saturation/warmth adjustments, 8 photo filter presets, shape clipping masks, photo border & corner radius, per-layer `BlendMode` (`Normal`, `Multiply`, `Screen`, `Overlay`, `Darken`, `Lighten`, `Add`), multi-select, and layer grouping.
- **Canvas, Color & Export**: Linear/radial/sweep canvas gradients, 20 curated gradient presets, background photo blur & dim, film grain filter, Canvas Eyedropper color sampling, Social Safe Zone overlay, WebP (Lossy/Lossless) export, selected-layer sticker export with transparent trim, direct `FileProvider` sharing without gallery clutter, `.pixelcraft` ZIP project archive export/import, and external `ACTION_SEND`/`ACTION_EDIT` image intent support.
- **Platform, Branding & Build**: Adaptive launcher icon (foreground/background/monochrome), Android 12+ `SplashScreen`, Gradle Version Catalog (`gradle/libs.versions.toml`), `compileSdk = 35` / `targetSdk = 35` / `minSdk = 28`, EN + ID localization (`locales_config.xml`), `backup_rules.xml` & `data_extraction_rules.xml`, Android Lint CI gate, and Play Store App Bundle (`bundleRelease`) workflow.

### Fixed
- Ordered `BackHandler` in editor preventing accidental exit and loss of unsaved work.
- Exact `TextGeometryCache` glyph path and selection bounds (replacing heuristic character-width tables on canvas).
- Screen-space (`12.dp / scale`) snapping threshold plus edge & inter-layer Smart Guides.
- Universal horizontal/vertical flip and drop shadow parity across Text, Shape, and Photo layers.
- Memory-aware export dimension capping with automatic OOM step-down fallback.
- Undo history consistency when locking and unlocking layers, plus `<500ms` nudge coalescing and interactive Edit History jump sheet.

## [0.1.0] - 2026-11-05

### Added
- Initial Jetpack Compose typography and vector shape studio with 15 bundled open-source fonts, vector path contour strokes, 24 shapes & word balloons, 3D extrusion, reflection, HSV color wheel picker, local project storage, and PNG/JPEG export.
