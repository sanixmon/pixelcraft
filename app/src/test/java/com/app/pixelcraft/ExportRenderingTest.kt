package com.app.pixelcraft

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.domain.ExportFormat
import com.app.pixelcraft.domain.ExportOptions
import com.app.pixelcraft.domain.ExportQuality
import com.app.pixelcraft.domain.ImageExporter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayOutputStream
import kotlin.math.abs

/**
 * Renders real pixels through the shared export pipeline (CanvasRenderer + ImageExporter)
 * using Robolectric's native graphics, so regressions in what users actually get out of
 * "Save to Gallery" are caught on the JVM.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExportRenderingTest {

    private val exporter = ImageExporter(ApplicationProvider.getApplicationContext())

    private val blueBackground = CanvasConfig(
        width = 400,
        height = 400,
        backgroundType = BackgroundType.SOLID_COLOR,
        backgroundColor = 0xFF0000FF
    )

    private fun redBox(
        id: String = "box",
        x: Float = 0f,
        y: Float = 0f,
        w: Float = 100f,
        h: Float = 60f
    ) = Layer(
        id = id,
        name = "Box",
        type = LayerType.SHAPE,
        shapeType = ShapeType.RECTANGLE,
        shapeFillColor = 0xFFFF0000,
        shapeStrokeWidth = 0f,
        positionX = x,
        positionY = y,
        width = w,
        height = h
    )

    private fun export(
        config: CanvasConfig,
        layers: List<Layer>,
        options: ExportOptions = ExportOptions(),
        selected: Set<String> = emptySet()
    ): Bitmap = runBlocking { exporter.exportCanvasToBitmap(config, layers, options, selected) }

    private fun assertColorNear(expected: Int, actual: Int, tolerance: Int = 8) {
        val ok = abs(Color.red(expected) - Color.red(actual)) <= tolerance &&
            abs(Color.green(expected) - Color.green(actual)) <= tolerance &&
            abs(Color.blue(expected) - Color.blue(actual)) <= tolerance &&
            abs(Color.alpha(expected) - Color.alpha(actual)) <= tolerance
        assertTrue(
            "expected #%08X but was #%08X".format(expected, actual),
            ok
        )
    }

    @Test
    fun standardQualityExportMatchesCanvasSizeAndBackground() {
        val bmp = export(blueBackground, emptyList())
        assertEquals(400, bmp.width)
        assertEquals(400, bmp.height)
        assertColorNear(Color.BLUE, bmp.getPixel(5, 5))
        assertColorNear(Color.BLUE, bmp.getPixel(395, 395))
    }

    @Test
    fun qualityMultiplierScalesOutputResolution() {
        val bmp = export(blueBackground, emptyList(), ExportOptions(qualityPreset = ExportQuality.VERY_HIGH))
        assertEquals(800, bmp.width)
        assertEquals(800, bmp.height)
    }

    @Test
    fun shapeLayerIsDrawnAtItsCanvasCenteredPosition() {
        // positionX/Y are relative to the canvas center.
        val bmp = export(blueBackground, listOf(redBox(x = 100f, y = -50f)))
        assertColorNear(Color.RED, bmp.getPixel(300, 150))
        assertColorNear(Color.BLUE, bmp.getPixel(200, 200))
        assertColorNear(Color.BLUE, bmp.getPixel(5, 5))
    }

    @Test
    fun hiddenAndFullyTransparentLayersAreNotExported() {
        val layers = listOf(
            redBox(id = "hidden").copy(isVisible = false),
            redBox(id = "clear", x = 100f).copy(opacity = 0f)
        )
        val bmp = export(blueBackground, layers)
        assertColorNear(Color.BLUE, bmp.getPixel(200, 200))
        assertColorNear(Color.BLUE, bmp.getPixel(300, 200))
    }

    @Test
    fun higherZIndexLayerIsPaintedOnTop() {
        val bottom = redBox(id = "bottom", w = 200f, h = 200f).copy(zIndex = 0)
        val top = redBox(id = "top", w = 50f, h = 50f).copy(shapeFillColor = 0xFF00FF00, zIndex = 1)
        // List order deliberately reversed: zIndex, not list position, must decide stacking.
        val bmp = export(blueBackground, listOf(top, bottom))
        assertColorNear(Color.GREEN, bmp.getPixel(200, 200))
        assertColorNear(Color.RED, bmp.getPixel(200 + 80, 200))
    }

    @Test
    fun jpegExportOfTransparentCanvasFillsWhite() {
        val transparent = blueBackground.copy(backgroundType = BackgroundType.TRANSPARENT)
        val bmp = export(transparent, emptyList(), ExportOptions(format = ExportFormat.JPEG))
        assertColorNear(Color.WHITE, bmp.getPixel(10, 10))
    }

    @Test
    fun pngExportOfTransparentCanvasKeepsAlpha() {
        val transparent = blueBackground.copy(backgroundType = BackgroundType.TRANSPARENT)
        val bmp = export(transparent, listOf(redBox()), ExportOptions(format = ExportFormat.PNG))
        assertEquals(0, Color.alpha(bmp.getPixel(10, 10)))
        assertColorNear(Color.RED, bmp.getPixel(200, 200))
    }

    @Test
    fun stickerExportKeepsOnlySelectedLayerAndTrimsMargins() {
        val keep = redBox(id = "keep", w = 100f, h = 60f)
        val other = redBox(id = "other", x = 150f, y = 150f).copy(shapeFillColor = 0xFF00FF00)
        val bmp = export(
            blueBackground,
            listOf(keep, other),
            ExportOptions(format = ExportFormat.PNG, selectedLayerOnly = true, trimTransparent = true),
            selected = setOf("keep")
        )
        // Trimmed to the 100×60 box (allowing a pixel or two of anti-aliasing).
        assertTrue("width was ${bmp.width}", bmp.width in 98..104)
        assertTrue("height was ${bmp.height}", bmp.height in 58..64)
        assertColorNear(Color.RED, bmp.getPixel(bmp.width / 2, bmp.height / 2))
    }

    @Test
    fun exportedBitmapSurvivesPngEncodeDecodeRoundTrip() {
        val bmp = export(blueBackground, listOf(redBox()))
        val bytes = ByteArrayOutputStream().use { out ->
            assertTrue(bmp.compress(ExportFormat.PNG.resolveCompressFormat(), 100, out))
            out.toByteArray()
        }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        assertEquals(bmp.width, decoded.width)
        assertEquals(bmp.height, decoded.height)
        assertColorNear(Color.RED, decoded.getPixel(200, 200))
        assertColorNear(Color.BLUE, decoded.getPixel(5, 5))
    }
}
