package dev.hsichen.colorinvo.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.zxing.common.BitArray
import com.google.zxing.oned.Code39Reader
import dev.hsichen.colorinvo.data.CarrierSettings
import dev.hsichen.colorinvo.data.Decoration
import dev.hsichen.colorinvo.domain.BarcodePalette
import dev.hsichen.colorinvo.domain.RgbaColor
import dev.hsichen.colorinvo.domain.WallpaperPaletteGenerator
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetBitmapRendererTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    @Test fun everyStyleDecodesAtDifferentWidgetSizesWithTextOnAndOff() {
        val palettes = listOf(BarcodePalette.Classic) + WallpaperPaletteGenerator.palettes(listOf(RgbaColor(0x70D6FF)))
        for ((width, height) in listOf(500 to 220, 658 to 310, 900 to 500)) {
            for (decoration in Decoration.entries) for (palette in palettes) for (showsValue in listOf(true, false)) {
                for (code in listOf("/ABC1234", "/A1+-.Z9")) {
                    val settings = CarrierSettings(code, palette, decoration = decoration, showsBarcodeValue = showsValue)
                    val bitmap = WidgetBitmapRenderer.render(context, settings, width, height)
                    val rowY = (height * if (decoration == Decoration.WAVE) 0.70 else 0.20).toInt()
                    assertEquals("$decoration at $width x $height", code, decode(bitmap, rowY))
                    // Scan band remains opaque and uses only the selected foreground/background colors.
                    for (x in 0 until width) assertTrue(bitmap.getPixel(x, rowY) in listOf(palette.barColor.argb, palette.backgroundColor.argb))
                    bitmap.recycle()
                }
            }
        }
    }

    @Test fun paintUsesTheSelectedSourceColor() {
        val color = RgbaColor(0x70D6FF)
        val bitmap = WidgetBitmapRenderer.render(context, CarrierSettings("/ABC1234", decoration = Decoration.WAVE, waveColor = color))
        assertEquals(color.argb, bitmap.getPixel(120, 5))
    }

    @Test fun emptyAndInvalidWidgetsNeverShowASampleBarcode() {
        for (code in listOf("", "/ABC", "/ABC_123", "💛")) {
            val bitmap = WidgetBitmapRenderer.render(context, CarrierSettings(code))
            assertTrue(runCatching { decode(bitmap, 62) }.isFailure)
        }
    }

    @Test fun captureWidgetStylesForVisualReview() {
        val palette = BarcodePalette("showcase", RgbaColor(0x063F52), RgbaColor(0xEAF8FF))
        Decoration.entries.forEach { decoration ->
            val bitmap = WidgetBitmapRenderer.render(context, CarrierSettings("/ABC1234", palette, decoration = decoration, waveColor = RgbaColor(0x70D6FF)))
            File(context.getExternalFilesDir(null), "widget-${decoration.name.lowercase()}.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }

    private fun decode(bitmap: Bitmap, y: Int): String {
        val row = BitArray(bitmap.width)
        for (x in 0 until bitmap.width) if (Color.red(bitmap.getPixel(x, y)) < 128) row.set(x)
        return Code39Reader().decodeRow(y, row, null).text
    }
}
