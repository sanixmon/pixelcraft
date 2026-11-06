# PixelCraft (Jetpack Compose & Material 3)

**PixelCraft** is an open-source Android typography, vector shape, and photo editor built with **Android Jetpack Compose**, **Material 3**, and declarative state-driven rendering.

---

## ✨ Features

### 1. 🔤 Typography, 15 Bundled Open-Source Fonts & Ivy Draw Vector Strokes
- **15 Bundled Commercial-Safe Fonts**: Packaged directly in `res/font/` under SIL OFL 1.1 & Apache 2.0 (*Poppins*, *Lato*, *Bebas Neue*, *Anton*, *Archivo Black*, *Russo One*, *Bangers*, *Luckiest Guy*, *Abril Fatface*, *PT Serif*, *Permanent Marker*, *Pacifico*, *Great Vibes*, *Press Start 2P*, and *Space Mono*, plus *System Sans* — see `assets/FONT_LICENSES.txt`).
- **Ivy Draw Vector Path Strokes**: True glyph/contour `Path` strokes supporting **Position** (`Outside`, `Center`, `Inside` via `clipOutPath` / `clipPath`), **Dash Patterns** (`Solid`, `Dashed`, `Dotted`, `Dash-Dot` with `Dash`, `Gap`, and `Phase` controls), **Corner Joins** (`Round`, `Miter` + `Miter Limit`, `Bevel`), and **Line Caps** (`Round`, `Butt`, `Square`).
- **3D Text Extrusion**: Configurable 3D extrusion depth, darkening ratio, floor shadow, and light angle.
- **Mirror Reflection & Drop Shadows**: Studio floor mirror reflection and customizable blur drop shadows.
- **Typography Styles & Pill Background**: Bold, Italic, Underline, multi-line alignment, line spacing, and rounded text background pills.

### 2. 🔷 PicSay Pro Vector Shapes, Word Balloons & Pointers
- **24 Pure Vector Shapes in 4 Categories**:
  - **Basic**: Rectangle, Rounded Rect, Circle / Oval, Triangle, Diamond, Pentagon, Hexagon, Octagon
  - **Word Balloons**: Oval Balloon, Speech Box, Thought Cloud, Comic Shout Burst
  - **Arrows**: Block Arrow, Double Arrow, Chevron, Callout Pin
  - **Badges & Symbols**: 5-Point Star, 12-Point Starburst, Heart, Shield, Ribbon Banner, Crown, Lightning, Cross
- **Flip Horizontal / Vertical**: Instantly flip word balloon tails and arrow directions toward any subject.
- **Solid, 2-Color Gradient & Hollow Fills**: Combine solid or linear gradient fills with Ivy Draw vector strokes and shape drop shadows.

### 3. 🎨 Interactive Color Wheel & Hex Picker Everywhere
- **2D HSV Color Wheel + Brightness & Alpha Sliders + Hex (`#RRGGBB` / `#AARRGGBB`)**: Accessible from every color palette in the editor (Text Color, Text Background, Vector Stroke, Drop Shadows, Shape Fill & Gradients, Canvas Background, and Vignette).

### 4. 🖼️ Photos, Fixed Canvas & Grid-Activated Magnetic Anchors
- **Fixed Canvas Viewport**: Static canvas slot + compact icon-only bottom navigation bar so opening or switching any tool/inspector never shifts or rescales the canvas.
- **Freemove by Default, Anchors When Grid ON**: Smooth un-anchored freemove dragging by default; toggle **Grid** on to activate magnetic snapping and guide lines on canvas center axes and grid divisions.
- **Photo Layers & Backgrounds**: Import photos via the Android Photo Picker into movable layers or center-cropped canvas backgrounds.

### 5. 💾 High-Resolution Export & Project Persistence
- **Save to Gallery & Share**: Exports to `Pictures/PixelCraft` (MediaStore) in **PNG** (preserving transparency) or **JPEG** up to **4K Ultra HD (`3.0x`)**.
- **Project Storage & Undo/Redo**: Local JSON project snapshots (`kotlinx.serialization`) and 50-step undo/redo history.

---

## 🚀 Building & Testing

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Compile Debug APK
```bash
./gradlew assembleDebug
```

---

## 📄 License

Licensed under the [Apache License, Version 2.0](LICENSE). Bundled font licenses are documented in [`app/src/main/assets/FONT_LICENSES.txt`](app/src/main/assets/FONT_LICENSES.txt).
