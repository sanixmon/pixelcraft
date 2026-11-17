# PixelCraft vs. product spec — gap analysis

Reference: the PixelForge product & technical spec (PixelLab-inspired editor) the owner
shared on 2026-11-17, used as the product direction for PixelCraft. Status reflects the
code at v0.5.0; ✅ marks gaps closed since (unreleased).

## Already covered

- **Canvas:** presets and custom size, transparent / solid / gradient / photo background,
  zoom and pan, grid, safe-zone guides, snapping to edges, centre and other layers,
  checkerboard preview.
- **Text:** font, size, bold/italic/underline, letter and line spacing, alignment,
  opacity, rotation, curved text, solid/linear/radial fill, stroke with alignment, shadow,
  glow, 3D extrusion, reflection, background box, font import, favourites, saved styles.
- **Images:** crop editor, rotate, flip, opacity, brightness/contrast/saturation/warmth,
  8 filter presets, shape masks, rounded corners, stroke, shadow, blend modes.
- **Shapes:** 24 shapes incl. rectangle, rounded rectangle, circle, triangle,
  pentagon/hexagon/octagon, star, arrows, speech bubbles; fill, gradient, stroke
  (alignment, dashes, joins, caps), corner radius, opacity, shadow, blend.
- **Layers:** rename, hide, lock, duplicate, delete, drag-to-reorder, multi-select,
  group/ungroup with whole-group transforms, merge to picture.
- **Gestures:** tap select, double-tap edit text, drag, pinch, twist, long-press menu,
  screen-space hit testing.
- **History:** undo/redo, gesture coalescing, 50-step limit, hardware-keyboard shortcuts.
- **Projects:** save, rename, duplicate, delete, autosave, crash recovery, atomic writes,
  portable `.pixelcraft` ZIP archives.
- **Export:** PNG/JPG/WebP, quality scale presets, transparent export, share, MediaStore.

## Gaps

Sizes: S = a day or less, M = a few days, L = a week or more.

### Editing precision and layers
| Gap | Spec | Size |
|---|---|---|
| ✅ Numeric X / Y / W / H / rotation fields for the selection | §4 Precision controls | S |
| ✅ Distribute horizontally / vertically for multi-select | §4 Alignment | S |
| ✅ Copy / paste layers (also across projects) | §3 | S |
| ✅ Layer thumbnails in the layers sheet | §3 Layer panel | S |
| Rotation handle on the selection box | §4 Selection handles | M |
| Rulers | §2.1 | M |
| Groups as a real nested layer type (today: shared `groupId` tag) | §3 GroupLayer | L |

### Typography
| Gap | Spec | Size |
|---|---|---|
| ✅ Recent fonts | §2.2 | S |
| ✅ Built-in text style presets (one-tap looks) | §2.2 Text presets | M |
| ✅ Horizontal / vertical text scaling | §2.2 | M |
| ✅ Text box width with wrapping | §2.2 | M |
| ✅ Inner shadow, emboss | §2.2 effects table | M |
| Texture (image) fill for text | §2.2 Fill | M |
| Text along a custom path | §2.2 advanced | L |

### Images and shapes
| Gap | Spec | Size |
|---|---|---|
| ✅ Hue, blur, tint adjustments (grayscale/sepia exist as presets) | §2.3 | S |
| ✅ Import several photos at once | §2.3 | S |
| ✅ Ellipse and line shapes | §2.4 | S |
| Perspective transform | §2.3 advanced | L |
| Background-removal integration point | §2.3 advanced | M |
| Custom path editing | §2.4 (later milestone) | L |
| Sticker & asset library with categories, recents, favourites | §2.5 | L (needs licensed assets) |

### Export
| Gap | Spec | Size |
|---|---|---|
| ✅ Custom output resolution / scale multiplier | §7 | S |
| ✅ Background colour when exporting a transparent canvas as JPG | §7 | S |
| Export preview | §7 | M |

### Architecture (spec §9–12)
The spec suggests Hilt, Room, a multi-module Gradle layout and a renderer with region
invalidation. PixelCraft already keeps rendering (`CanvasRenderer`, `ImageRenderer`),
geometry (`GroupOperations`, `TransformHelper`) and history (`EditorHistory`) out of the
composables. A wholesale restructure would cost weeks with no user-visible gain, so it
should only happen piecemeal, when a feature needs it.

## Suggested order
1. **Precision pack (S):** numeric transform fields, distribute, copy/paste layers, layer
   thumbnails.
2. **Image & shape pack (S):** hue/blur/tint, multi-photo import, ellipse and line.
3. **Export pack (S):** custom resolution, JPG background colour.
4. **Typography pack (M):** recent fonts, text presets, text scaling, wrapping, inner
   shadow/emboss.
5. **Bigger bets (L):** rotation handle + rulers, sticker library, text on path,
   perspective.

---

# UI/UX interaction spec — comparison

Reference: the "PixelForge — UI/UX Interaction & Implementation Specification" (CapCut-style
contextual panel, portrait-first) shared on 2026-11-17.

## Matches today
- Portrait-first editor; canvas on top, tools at the bottom; landscape puts tools in a side panel.
- Contextual tools per selection (text / shape / photo / group), canvas tools when nothing is selected.
- Layers in a separate bottom sheet with visibility, lock, drag-to-reorder, duplicate, delete,
  group and ungroup; reorder updates the canvas immediately.
- Screen-space hit testing and snap threshold (`12.dp / scale`), snap guides while moving,
  locked layers can't be transformed, tap empty canvas to deselect.
- Sliders with numeric entry and reset to default (`PrecisionSlider`); colour picker with hex.
- Ordered back handling (eyedropper → open tool → layers sheet → deselect → unsaved-changes prompt).
- Text edits commit as one undo step from the edit dialog.
- Fixed-height tool area so opening tools never moves the canvas.

## Gaps
| Gap | Spec | Size |
|---|---|---|
| **Category toolbar stays visible while a property panel is open.** Today the inspector replaces the tool row, so switching Style → Effects means closing and reopening. | §3, §14 final note | M |
| **Group tools into a few categories** instead of one long row: Text `Edit · Font · Style · Effects · Arrange`; Image `Replace · Crop · Adjust · Filter · Effects · Arrange`; Shape `Geometry · Fill · Stroke · Effects · Arrange`; Multi-select `Arrange · Align · Distribute · Group`. | §3.2–3.5 | M |
| No-selection toolbar should be `Text · Image · Shape · Sticker · Background · Layers` (today also Date and Effects, no Sticker). | §3.1 | S (Sticker needs the asset library) |
| Multi-selection (not grouped) gets its own panel with only shared properties. Today it shows the primary layer's panel. | §3.5 | M |
| Panel states Compact / Expanded / Full with a drag handle. **Conflicts with the owner's request for a fixed canvas**; if added, resizing must keep zoom and pan, and the default should stay fixed. | §4 | M |
| Rotation handle separate from resize handles; corner resize handles drawn at a constant screen size. | §5 | M |
| Replace image action. | §3.3 | S |
| Layer sheet: search, long-press to enter multi-select, thumbnails. | §6 | S–M |
| Inline text editor above the keyboard with Apply / Cancel (today: a dialog). | §7 Text editing | M |
| Back order should also dismiss the keyboard and close pickers first. | §7 | S |
| Purposeful animation: `AnimatedContent` for category changes (120–180 ms), panel expansion (200–250 ms). Today there is none. | §11 | S |
| `EditorAction` / reducer structure and a separate interaction controller. Useful when the toolbar is rebuilt; not worth a standalone rewrite. | §8 | L |

## Suggested order (UX)
1. **Editor shell refresh:** persistent category toolbar + property area under it, the
   category groupings above, multi-select panel, `AnimatedContent` transitions. Keep the
   property area a fixed height by default so the canvas stays put.
2. Rotation handle and constant-size handles.
3. Layer sheet search, long-press multi-select, thumbnails.
4. Inline text editor with Apply / Cancel.
