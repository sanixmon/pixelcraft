# Changelog

All notable changes to **PixelCraft** are documented in this file.

## [0.6.0] - 2026-11-17

### Added
- **Exact position and size**: X, Y, W, H and rotation number boxes in a new Position tool for shapes and photos (text gets X, Y and rotation), with nudge and align.
- **Distribute**: space three or more layers evenly, across or down.
- **Copy and paste layers** (long-press menu, ⋮ menu, Ctrl+C / Ctrl+V); groups stay grouped and it works between projects.
- **Layer thumbnails** in the layers sheet.
- **Photo hue, blur and tint**.
- **Add several photos at once** (up to 10).
- **Ellipse and Line shapes**.
- **Stroke tool for photos and merged layers**; on merged layers it follows the shape.
- **Recent fonts** in the font picker.
- **12 one-tap text styles**: Sticker, Neon, Gold 3D, Retro, Comic, Highlight, Metal, Ice, Soft shadow, Elegant, Spaced, Outline.
- **Stretch text** horizontally or vertically, and **wrap long text** at a set width.
- **Inner shadow and emboss** for text.
- **Custom export size**, and a choice of background colour when a transparent design is saved as JPG.

## [0.5.0] - 2026-11-17

### Added
- **Merge group**: flatten a group into one picture that edits like any photo (crop, adjust, mask, outline, shadow, size, rotate).
- **Group stroke**: one outline width and colour for every layer in a group.
- **Shape-following outline and shadow** for merged pictures, like a sticker.
- **New crop editor**: full screen, drag corners or edges, move the box, rule-of-thirds guides and aspect presets (Free, Original, 1:1, 4:5, 3:4, 9:16, 4:3, 16:9). Photos are never stretched.
- **Save project** in the editor's ⋮ menu.

### Changed
- Messages like "Copied" and "Saved" appear at the top of the canvas instead of covering the tools.
- Loading fonts from a device folder is optional: tap the folder icon in the font picker to choose one.
- Larger touch targets for dropdowns and chips.

### Removed
- Built-in starter templates. The editor starts on a blank canvas.

## [0.4.0] - 2026-11-17

### Added
- **29 new starter templates** built at real canvas scale for common formats: quotes, YouTube thumbnails, flash sale, discount code, giveaway, menu, hiring, announcement, customer review, milestone, tips carousel, event poster, webinar, save the date, birthday, holidays, thank you, memes and stories. New **Business** and **Event** categories.
- **69 bundled open-source fonts** (54 new, including Inter, Montserrat, Playfair Display, Syne, DM Serif Display, Lora, Cinzel and JetBrains Mono). See `THIRD_PARTY_FONTS.md`.
- **Device fonts**: grant the `/sdcard/Fonts/` folder once and its `.ttf`/`.otf` fonts appear under a "Device" tab, re-scanned on every launch.
- **Group editing**: a selected group gets its own panel for size, rotate, opacity, blend, flip, align, front/back, duplicate and delete as one object. Pinch and twist gestures transform the whole group.
- **Drag to reorder layers** in the layers sheet.
- **In-app updater**: checks GitHub Releases, downloads the new APK with progress, verifies its checksum and signature, and installs it over the current app.

### Changed
- The canvas keeps its size and position when tools open; the tool panel has a fixed height.
- Mode choices (fill type, gradient type, blend mode, stroke style, photo filter and mask) are compact dropdowns instead of chip rows.
- The font picker no longer filters by style category.

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
