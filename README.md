# PixelCraft (Jetpack Compose & Material 3)

**PixelCraft** is an open-source Android editor for putting styled text, shapes and stickers on pictures, built from scratch with **Android Jetpack Compose**, **Material 3**, and declarative state-driven rendering.

---

## ✨ Features

### 1. 🔤 Advanced 3D Typography & Styling
- **3D Text Extrusion**: Configurable 3D extrusion depth, darkening ratio, and light angle.
- **Mirror Reflection**: Realistic bottom floor reflection with adjustable distance and opacity fade.
- **Floor & Drop Shadows**: Directional 3D floor shadow and 2D blur shadow with X/Y offset controls.
- **Stroke & Outline**: Crisp outline rendering with custom stroke widths and color palette.
- **Typography Controls**: Font families (Serif, Sans, Monospace, Cursive), Bold, Italic, Underline, Letter Spacing, and Line Height.
- **Text Background**: Customizable background pill/box with inner padding and corner radius.
- **Color & Gradients**: Solid color palette and multi-stop linear/radial gradients.

### 2. 🖼️ Photos
- **Photo Layers**: Add pictures from the system photo picker (no storage permission needed) and move, scale and rotate them like any layer.
- **Photo Backgrounds**: Use a picture as the canvas background, centre-cropped to fill any aspect ratio.
- Picked photos are copied into app storage, so saved projects keep their pictures.

### 2b. 🔷 Shapes & Decorative Stickers
- **Geometric Shapes**: Rectangle, Rounded Rectangle, Circle, 5-Point Star, Triangle, Heart, Hexagon, Speech Bubble, and Arrow.
- **Corner Radius & Stroke**: Adjustable corner roundness, outline stroke width, fill color, and transparency.
- **Sticker Library**: Curated vector/emoji stickers organized into Emojis, Badges & Stars, Decorative elements, and Sale labels.

### 3. 🎨 Interactive Canvas & Transform Controls
- **Interactive Handles**: Cyan selection bounding box with 4 corner scale pins, top rotation pin handle, quick delete button, and duplicate button.
- **Zoom & Pan Mode**: Toggleable canvas viewport zoom (up to 5x) and pan gestures.
- **Magnetic Snapping**: Real-time guide lines snapping layer centers to canvas horizontal and vertical center axes.
- **Grid Alignment**: Toggleable alignment grid with configurable divisions.
- **Nudge & Relative Position**: Directional arrow pad (with step slider) and one-tap alignments (Center H/V, Left, Right, Top, Bottom).

### 4. 📐 Canvas Aspect Ratio & Backgrounds
- **Aspect Ratio Presets**:
  - `Square (1:1)` - 1080 x 1080 px
  - `YouTube Thumbnail (16:9)` - 1280 x 720 px
  - `YouTube Channel Banner` - 2560 x 1440 px
  - `Story / Reels (9:16)` - 1080 x 1920 px
  - `Facebook Cover` - 1200 x 628 px
  - `Twitter Header` - 1500 x 500 px
  - `Custom Dimensions`
- **Background Modes**: Solid Color, Multi-stop Gradients, Transparent (checkered pattern for PNG cutout).
- **Magic Canvas Filters**: Vignette shading, TV scanlines / stripes, Brightness, and Contrast.

### 5. 📑 Multi-Layer Management Stack
- **Floating Layers Drawer**: Reverse z-order stack display.
- **Layer Actions**:
  - Visibility toggle (eye icon)
  - Lock / Freeze toggle (padlock icon to prevent accidental moves)
  - Bring to Front / Send to Back
  - Reorder layers up/down
  - Duplicate & Delete layers

### 6. 💾 High-Resolution Export & Project Persistence
- **Save to Gallery**: Exports land in `Pictures/PixelCraft` (MediaStore), with a one-tap **Share** action.
- **Export Formats**: PNG (preserves transparency) and JPEG.
- **Resolution Scaling**:
  - `Default` (1.0x)
  - `Low` (0.5x)
  - `Medium` (1.0x)
  - `High` (1.5x)
  - `Very High` (2.0x HD)
  - `Ultra` (3.0x - 4K UHD!)
- **Project Storage**: Save and load projects locally as JSON snapshots with complete undo/redo history.
- **Quotes Library**: 1-tap quote inserts across Motivation, Success, Friendship, Love, Happiness, and Wisdom categories.

---

## 🏗️ Architecture & Technology Stack

```
pixelcraft/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/app/pixelcraft/
│   │   │   │   ├── PixelCraftApp.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/         # Layer, CanvasConfig, Preset, History, PointF
│   │   │   │   │   └── presets/       # DefaultPresets, QuotesLibrary, StickerLibrary, FontLibrary
│   │   │   │   ├── domain/            # TransformHelper, SnappingEngine, ImageExporter, ProjectStorage
│   │   │   │   ├── presentation/      # PixelCraftViewModel, PixelCraftUiState, PixelCraftScreen
│   │   │   │   │   └── components/    # PixelCraftCanvas, TransformHandles, TopBar, BottomNav, Sheets, Dialogs
│   │   │   │   └── ui/theme/          # Material 3 Theme, DarkColorScheme, Typography
│   │   │   └── res/
│   │   └── test/java/com/app/pixelcraft/ # Transform, Snapping, Serialization & Export Unit Tests
│   └── build.gradle.kts
├── .github/workflows/ci.yml           # Automated GitHub Actions CI workflow
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

- **UI Framework**: Android Jetpack Compose with Material 3 (`androidx.compose.material3`).
- **State Management**: Kotlin Coroutines Flow (`StateFlow` & `MutableStateFlow`) with Uni-directional Data Flow (UDF).
- **Kotlin Serialization**: `kotlinx.serialization` for zero-reflection JSON project serialization.
- **Compositing Engine**: Dual-mode rendering:
  - Compose Declarative Canvas (`androidx.compose.foundation.Canvas`) for smooth 60fps interactive editing.
  - Android Software Canvas (`android.graphics.Canvas` / `Bitmap`) for crisp, offline 4K high-resolution rendering and export.

---

## 🚀 Building & Testing Locally

### Prerequisites
- JDK 17 or JDK 21
- Android SDK (compileSdk 34+, platforms 34, 35, or 36)

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Compile Debug APK
```bash
./gradlew assembleDebug
```
The output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔄 GitHub CI Workflow

The repository includes automated CI in `.github/workflows/ci.yml`:
1. Checks out repository code on `push` and `pull_request` to `main` / `master`.
2. Sets up JDK 17 with Temurin and Gradle build cache.
3. Executes `./gradlew testDebugUnitTest` and uploads test result artifacts.
4. Compiles `./gradlew assembleDebug` and archives `app-debug.apk` as a downloadable artifact.

---

## 📄 License

Licensed under the [Apache License, Version 2.0](LICENSE).
