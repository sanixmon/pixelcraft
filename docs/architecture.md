# PixelCraft Architecture

PixelCraft is an offline-first Android typography, vector shape, and photo composition studio built with **Kotlin**, **Jetpack Compose (Material 3)**, and native **`android.graphics.Canvas`** rendering.

## Layered Package Structure

```text
com.app.pixelcraft
├── PixelCraftApp.kt                # Application entry, AppContainer manual DI, StrictMode, onTrimMemory, startup GC
├── MainActivity.kt                 # SplashScreen, ACTION_SEND/EDIT/VIEW image intents, Compose NavHost (home / editor)
├── data/
│   ├── model/                      # @Immutable & @Serializable domain models (Layer, CanvasConfig, ProjectData v2)
│   └── presets/                    # Bundled open-source FontCatalog (+ .ttf/.otf custom fonts) & 20 DefaultPresets
├── domain/
│   ├── editor/
│   │   ├── EditorHistory.kt        # ArrayDeque undo/redo stack with labels, continuous & <500ms nudge coalescing
│   │   └── LayerOperations.kt      # Pure z-order, align, duplicate, deleteUnlocked, and group/ungroup transforms
│   ├── CanvasRenderer.kt           # Shared preview & export renderer (ThreadLocal Paint pool, batched 3D extrusion)
│   ├── ImageRenderer.kt            # Photo crop, ColorMatrix adjustments/presets, shape masks, borders, bg blur
│   ├── TextGeometryCache.kt        # LruCache of glyph Paths, circular arc curved text, and exact RectF bounds
│   ├── ShapePathBuilder.kt         # 24 procedural vector shapes & word balloons
│   ├── SnappingEngine.kt           # Screen-space magnetic snapping (center, edges, grid divisions, Smart Guides)
│   ├── DraftStore.kt               # Atomic crash-recovery draft persistence (files/draft.json)
│   ├── ProjectStorage.kt           # Atomic project snapshots, index.json, 256px WebP thumbnails, .pixelcraft ZIP
│   ├── ImageStore.kt               # Dual-res master (4096px) + LruCache proxy (1536px) & 24h orphan image GC
│   ├── ImageExporter.kt            # RAM-aware PNG / JPEG / WebP export, sticker export, transparent trim
│   ├── GallerySaver.kt             # Scoped MediaStore Pictures/PixelCraft saver & FileProvider cache direct share
│   └── UserPreferencesStore.kt     # Jetpack DataStore for recent colors, favorite fonts, saved styles & export prefs
└── presentation/
    ├── HomeScreen.kt               # Draft recovery, blank/photo quick start, 20 templates, project thumbnail grid
    ├── PixelCraftScreen.kt         # Ordered BackHandler, responsive portrait/landscape workspace, keyboard shortcuts
    ├── PixelCraftViewModel.kt      # Unidirectional StateFlow<PixelCraftUiState> orchestrator & debounced autosave
    └── components/                 # Direct-manipulation Canvas, TopBar, BottomNav, LayersSheet, ToolPanels, Dialogs
```

## Key Technical Decisions

### 1. Single Shared Rendering Pipeline
Both interactive preview (`PixelCraftCanvas`) and final file export (`ImageExporter`) delegate to `CanvasRenderer` and `ImageRenderer`.
- **Zero-allocation hot path**: `CanvasRenderer` reuses `Paint` instances via `ThreadLocal<RenderPaints>` and caches vector glyph contours + exact `RectF` bounds inside `TextGeometryCache` (`LruCache`).
- **Batched 3D extrusion**: Extrusion steps union into a single `Path` before drawing so shadow/alpha layers composite cleanly and fast.
- **Static-layer drag cache**: While dragging a single ungrouped layer on canvas, static background and lower layers render once into an offscreen bitmap and blit at 60–120 fps.

### 2. Crash-Safe Persistence & Portable Archives
- Every write to `draft.json`, `index.json`, `<projectId>.json`, and custom font metadata uses `androidx.core.util.AtomicFile` (`DraftStore.writeAtomically`), guaranteeing power loss or process death never leaves truncated JSON.
- Imported images are stored under `files/images/<uuid>.<ext>` and referenced strictly by **relative filename** (`schemaVersion = 2`). Legacy v1 absolute paths are migrated transparently on load.
- Projects can be exported and imported as self-contained `.pixelcraft` ZIP archives (`project.json`, `thumbnail.webp`, and `images/*`) with Zip-Slip path traversal validation.

### 3. Memory Safety
- `ImageStore` keeps an `LruCache` of downsampled 1536px preview proxies (capped to `maxMemory / 8`) and responds to `Application.onTrimMemory`, while `ImageExporter` reads full 4096px masters on demand.
- `ImageExporter` computes a safe pixel ceiling from `Runtime.maxMemory()` (`<= 25%` heap) and automatically retries at half scale if `OutOfMemoryError` occurs on memory-constrained devices.
