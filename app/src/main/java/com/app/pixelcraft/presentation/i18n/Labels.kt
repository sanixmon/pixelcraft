package com.app.pixelcraft.presentation.i18n

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.app.pixelcraft.R
import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.data.model.GradientType
import com.app.pixelcraft.data.model.LayerBlendMode
import com.app.pixelcraft.data.model.PhotoFilterPreset
import com.app.pixelcraft.data.model.PresetCategory
import com.app.pixelcraft.data.model.ShapeCategory
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.data.model.StrokeCapStyle
import com.app.pixelcraft.data.model.StrokeJoinStyle
import com.app.pixelcraft.data.model.StrokePattern
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.model.TextAlignment
import com.app.pixelcraft.domain.ExportQuality

@Composable
fun LayerBlendMode.localizedLabel(): String = stringResource(
    when (this) {
        LayerBlendMode.NORMAL -> R.string.enum_blend_normal
        LayerBlendMode.MULTIPLY -> R.string.enum_blend_multiply
        LayerBlendMode.SCREEN -> R.string.enum_blend_screen
        LayerBlendMode.OVERLAY -> R.string.enum_blend_overlay
        LayerBlendMode.DARKEN -> R.string.enum_blend_darken
        LayerBlendMode.LIGHTEN -> R.string.enum_blend_lighten
        LayerBlendMode.PLUS -> R.string.enum_blend_plus
    }
)

@Composable
fun PhotoFilterPreset.localizedLabel(): String = stringResource(
    when (this) {
        PhotoFilterPreset.NONE -> R.string.enum_filter_none
        PhotoFilterPreset.BW -> R.string.enum_filter_bw
        PhotoFilterPreset.VINTAGE -> R.string.enum_filter_vintage
        PhotoFilterPreset.COOL -> R.string.enum_filter_cool
        PhotoFilterPreset.WARM -> R.string.enum_filter_warm
        PhotoFilterPreset.VIVID -> R.string.enum_filter_vivid
        PhotoFilterPreset.NOIR -> R.string.enum_filter_noir
        PhotoFilterPreset.FADE -> R.string.enum_filter_fade
    }
)

@Composable
fun ShapeCategory.localizedLabel(): String = stringResource(
    when (this) {
        ShapeCategory.BASIC -> R.string.enum_shapecat_basic
        ShapeCategory.BALLOONS -> R.string.enum_shapecat_balloons
        ShapeCategory.ARROWS -> R.string.enum_shapecat_arrows
        ShapeCategory.BADGES -> R.string.enum_shapecat_badges
    }
)

@Composable
fun ShapeType.localizedLabel(): String = stringResource(
    when (this) {
        ShapeType.RECTANGLE -> R.string.enum_shape_rectangle
        ShapeType.ROUNDED_RECTANGLE -> R.string.enum_shape_rounded_rectangle
        ShapeType.CIRCLE -> R.string.enum_shape_circle
        ShapeType.TRIANGLE -> R.string.enum_shape_triangle
        ShapeType.DIAMOND -> R.string.enum_shape_diamond
        ShapeType.PENTAGON -> R.string.enum_shape_pentagon
        ShapeType.HEXAGON -> R.string.enum_shape_hexagon
        ShapeType.OCTAGON -> R.string.enum_shape_octagon
        ShapeType.SPEECH_ROUND -> R.string.enum_shape_speech_round
        ShapeType.SPEECH_BUBBLE -> R.string.enum_shape_speech_bubble
        ShapeType.THOUGHT_CLOUD -> R.string.enum_shape_thought_cloud
        ShapeType.SHOUT_BURST -> R.string.enum_shape_shout_burst
        ShapeType.ARROW -> R.string.enum_shape_arrow
        ShapeType.ARROW_DOUBLE -> R.string.enum_shape_arrow_double
        ShapeType.CHEVRON -> R.string.enum_shape_chevron
        ShapeType.LOCATION_PIN -> R.string.enum_shape_location_pin
        ShapeType.STAR -> R.string.enum_shape_star
        ShapeType.STARBURST -> R.string.enum_shape_starburst
        ShapeType.HEART -> R.string.enum_shape_heart
        ShapeType.SHIELD -> R.string.enum_shape_shield
        ShapeType.RIBBON_BANNER -> R.string.enum_shape_ribbon_banner
        ShapeType.CROWN -> R.string.enum_shape_crown
        ShapeType.LIGHTNING -> R.string.enum_shape_lightning
        ShapeType.CROSS -> R.string.enum_shape_cross
    }
)

@Composable
fun StrokePosition.localizedLabel(): String = stringResource(
    when (this) {
        StrokePosition.OUTSIDE -> R.string.enum_strokepos_outside
        StrokePosition.CENTER -> R.string.enum_strokepos_center
        StrokePosition.INSIDE -> R.string.enum_strokepos_inside
    }
)

@Composable
fun StrokeJoinStyle.localizedLabel(): String = stringResource(
    when (this) {
        StrokeJoinStyle.ROUND -> R.string.enum_join_round
        StrokeJoinStyle.MITER -> R.string.enum_join_miter
        StrokeJoinStyle.BEVEL -> R.string.enum_join_bevel
    }
)

@Composable
fun StrokeCapStyle.localizedLabel(): String = stringResource(
    when (this) {
        StrokeCapStyle.ROUND -> R.string.enum_cap_round
        StrokeCapStyle.BUTT -> R.string.enum_cap_butt
        StrokeCapStyle.SQUARE -> R.string.enum_cap_square
    }
)

@Composable
fun StrokePattern.localizedLabel(): String = stringResource(
    when (this) {
        StrokePattern.SOLID -> R.string.enum_pattern_solid
        StrokePattern.DASHED -> R.string.enum_pattern_dashed
        StrokePattern.DOTTED -> R.string.enum_pattern_dotted
        StrokePattern.DASH_DOT -> R.string.enum_pattern_dash_dot
    }
)

@Composable
fun PresetCategory.localizedLabel(): String = stringResource(
    when (this) {
        PresetCategory.QUOTE -> R.string.enum_presetcat_quote
        PresetCategory.YOUTUBE -> R.string.enum_presetcat_youtube
        PresetCategory.PROMO -> R.string.enum_presetcat_promo
        PresetCategory.GREETING -> R.string.enum_presetcat_greeting
        PresetCategory.MEME -> R.string.enum_presetcat_meme
        PresetCategory.STORY -> R.string.enum_presetcat_story
    }
)

@Composable
fun GradientType.localizedLabel(): String = stringResource(
    when (this) {
        GradientType.LINEAR -> R.string.enum_gradient_linear
        GradientType.RADIAL -> R.string.enum_gradient_radial
        GradientType.SWEEP -> R.string.enum_gradient_sweep
    }
)

@Composable
fun AspectRatioPreset.localizedLabel(): String = stringResource(
    when (this) {
        AspectRatioPreset.SQUARE -> R.string.enum_ratio_square
        AspectRatioPreset.INSTAGRAM_PORTRAIT_4_5 -> R.string.enum_ratio_instagram_portrait_4_5
        AspectRatioPreset.STORY_REELS -> R.string.enum_ratio_story_reels
        AspectRatioPreset.WHATSAPP_STATUS -> R.string.enum_ratio_whatsapp_status
        AspectRatioPreset.YOUTUBE_THUMBNAIL -> R.string.enum_ratio_youtube_thumbnail
        AspectRatioPreset.YOUTUBE_BANNER -> R.string.enum_ratio_youtube_banner
        AspectRatioPreset.MARKETPLACE_PRODUCT -> R.string.enum_ratio_marketplace_product
        AspectRatioPreset.PROFILE_PICTURE -> R.string.enum_ratio_profile_picture
        AspectRatioPreset.FACEBOOK_COVER -> R.string.enum_ratio_facebook_cover
        AspectRatioPreset.TWITTER_HEADER -> R.string.enum_ratio_twitter_header
        AspectRatioPreset.POSTER_4_3 -> R.string.enum_ratio_poster_4_3
        AspectRatioPreset.PORTRAIT_3_4 -> R.string.enum_ratio_portrait_3_4
        AspectRatioPreset.A4_PRINT -> R.string.enum_ratio_a4_print
        AspectRatioPreset.CUSTOM -> R.string.enum_ratio_custom
    }
)

@Composable
fun ExportQuality.localizedLabel(): String = stringResource(
    when (this) {
        ExportQuality.LOW -> R.string.enum_quality_low
        ExportQuality.MEDIUM -> R.string.enum_quality_medium
        ExportQuality.HIGH -> R.string.enum_quality_high
        ExportQuality.VERY_HIGH -> R.string.enum_quality_very_high
        ExportQuality.ULTRA -> R.string.enum_quality_ultra
    }
)

/** Alignment chip labels for [com.app.pixelcraft.data.model.TextAlignment]. */
@StringRes
fun TextAlignment.labelRes(): Int = when (this) {
    TextAlignment.LEFT -> R.string.ui_align_text_left
    TextAlignment.CENTER -> R.string.ui_align_text_center
    TextAlignment.RIGHT -> R.string.ui_align_text_right
}

/**
 * Undo-history labels are stored as stable English identifiers (they are part of the editor's
 * state and assertions in tests). They are translated only at display time.
 */
private val HistoryLabels: Map<String, Int> = mapOf(
    "Add Photo" to R.string.hist_add_photo,
    "Add Shape" to R.string.hist_add_shape,
    "Add Text" to R.string.hist_add_text,
    "Align Layer" to R.string.hist_align_layer,
    "Apply Custom Font" to R.string.hist_apply_custom_font,
    "Apply Style" to R.string.hist_apply_style,
    "Apply Template" to R.string.hist_apply_template,
    "Background Color" to R.string.hist_background_color,
    "Background Gradient" to R.string.hist_background_gradient,
    "Background Photo" to R.string.hist_background_photo,
    "Background Photo Adjustments" to R.string.hist_background_photo_adjustments,
    "Bring to Front" to R.string.hist_bring_to_front,
    "Canvas Filters" to R.string.hist_canvas_filters,
    "Change Text Case" to R.string.hist_change_text_case,
    "Delete Layer" to R.string.hist_delete_layer,
    "Duplicate Layer" to R.string.hist_duplicate_layer,
    "Edit" to R.string.hist_edit,
    "Edit Layer" to R.string.hist_edit_layer,
    "Edit Text" to R.string.hist_edit_text,
    "Eyedropper Glow" to R.string.hist_eyedropper_glow,
    "Eyedropper Gradient" to R.string.hist_eyedropper_gradient,
    "Eyedropper Photo Border" to R.string.hist_eyedropper_photo_border,
    "Eyedropper Shadow" to R.string.hist_eyedropper_shadow,
    "Eyedropper Shape Fill" to R.string.hist_eyedropper_shape_fill,
    "Eyedropper Shape Gradient" to R.string.hist_eyedropper_shape_gradient,
    "Eyedropper Shape Stroke" to R.string.hist_eyedropper_shape_stroke,
    "Eyedropper Stroke" to R.string.hist_eyedropper_stroke,
    "Eyedropper Text Color" to R.string.hist_eyedropper_text_color,
    "Eyedropper Text Pill" to R.string.hist_eyedropper_text_pill,
    "Group Layers" to R.string.hist_group_layers,
    "Move Layer" to R.string.hist_move_layer,
    "Move Layer Down" to R.string.hist_move_layer_down,
    "Move Layer Up" to R.string.hist_move_layer_up,
    "Nudge Layer" to R.string.hist_nudge_layer,
    "Open Project" to R.string.hist_open_project,
    "Paste Style" to R.string.hist_paste_style,
    "Rename Layer" to R.string.hist_rename_layer,
    "Resize Canvas" to R.string.hist_resize_canvas,
    "Send to Back" to R.string.hist_send_to_back,
    "Toggle Lock" to R.string.hist_toggle_lock,
    "Toggle Visibility" to R.string.hist_toggle_visibility,
    "Transform Layer" to R.string.hist_transform_layer,
    "Transparent Background" to R.string.hist_transparent_background,
    "Ungroup Layers" to R.string.hist_ungroup_layers,
)

@Composable
fun historyLabel(label: String): String = HistoryLabels[label]?.let { stringResource(it) } ?: label
