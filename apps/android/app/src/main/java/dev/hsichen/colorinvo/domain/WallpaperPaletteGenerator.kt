package dev.hsichen.colorinvo.domain

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.get
import androidx.core.graphics.scale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt

/** Color extraction and scanner-safe palette recipes shared with the iOS implementation. */
object WallpaperPaletteGenerator {
    fun representativeColors(bitmap: Bitmap): List<RgbaColor> {
        val sample = bitmap.scale(40, 40)
        val buckets = mutableMapOf<Int, ColorBucket>()
        try {
            for (y in 0 until sample.height) for (x in 0 until sample.width) {
                val pixel = sample[x, y]
                val alpha = Color.alpha(pixel) / 255.0
                if (alpha <= 0.5) continue
                val color = RgbaColor(Color.red(pixel) / 255.0 * alpha, Color.green(pixel) / 255.0 * alpha, Color.blue(pixel) / 255.0 * alpha)
                val key = (((color.red * 255).toInt() / 32) shl 8) or
                    (((color.green * 255).toInt() / 32) shl 4) or ((color.blue * 255).toInt() / 32)
                val weight = 1 + color.hsb.saturation * 0.35
                val bucket = buckets.getOrPut(key) { ColorBucket() }
                bucket.weight += weight
                bucket.red += color.red * weight
                bucket.green += color.green * weight
                bucket.blue += color.blue * weight
            }
        } finally {
            if (sample !== bitmap) sample.recycle()
        }
        val candidates = buckets.values.map {
            val color = RgbaColor(it.red / it.weight, it.green / it.weight, it.blue / it.weight)
            color to it.weight * (0.72 + color.hsb.saturation * 0.56)
        }.sortedByDescending { it.second }
        val selected = mutableListOf<RgbaColor>()
        for (distance in listOf(0.28, 0.18, 0.10)) {
            for ((color, _) in candidates) {
                if (selected.size < 3 && selected.all { perceptualDistance(it, color) >= distance }) selected += color
            }
        }
        return selected
    }

    fun palettes(colors: List<RgbaColor>): List<BarcodePalette> {
        val first = colors.firstOrNull() ?: return emptyList()
        val names = listOf("桌布主色", "桌布副色", "桌布點綴")
        val offsets = listOf(0.50, 0.34, 0.66)
        val fallbackOffsets = listOf(0.00, 0.08, 0.92)
        val mixes = listOf(0.76, 0.84, 0.90)
        val brightness = listOf(0.15, 0.18, 0.13)
        return names.indices.map { index ->
            val source = colors.getOrNull(index) ?: first.hsb.let {
                fromHsb(it.hue + fallbackOffsets[index], (it.saturation + 0.12).coerceIn(0.32, 0.76), maxOf(0.44, it.brightness))
            }
            val requiredMix = if (source.red >= 0.74) mixes[index]
                else maxOf(mixes[index], (0.74 - source.red) / maxOf(0.01, 1 - source.red))
            val background = mix(source, RgbaColor(0xFFFFFF), minOf(0.96, requiredMix))
            val hsb = source.hsb
            val hue = (hsb.hue + offsets[index]).let { it - floor(it) }.let {
                if (it * 360 <= 40 || it * 360 >= 340) 210.0 / 360 else it
            }
            var bar = fromHsb(hue, (hsb.saturation + 0.24).coerceIn(0.48, 0.76), brightness[index])
            if (bar.isReddish) bar = fromHsb(210.0 / 360, 0.68, 0.16)
            val maximumRed = maxOf(0.0, minOf(background.red / 2, background.red - BarcodePalette.SYMBOL_CONTRAST_STANDARD - 0.02))
            if (bar.red > maximumRed) bar = mix(RgbaColor(0x000000), bar, maximumRed / bar.red)
            val palette = BarcodePalette(names[index], bar, background)
            if (palette.meetsCommercialGuidance) palette else palette.copy(
                barColor = RgbaColor(0x000000),
                backgroundColor = if (background.red >= 0.70) background else RgbaColor(0xFFFFFF),
            )
        }
    }

    private data class Hsb(val hue: Double, val saturation: Double, val brightness: Double)
    private val RgbaColor.hsb: Hsb
        get() {
            val maximum = maxOf(red, green, blue)
            val chroma = maximum - minOf(red, green, blue)
            val hue = when {
                chroma == 0.0 -> 0.0
                maximum == red -> ((green - blue) / chroma) % 6 / 6
                maximum == green -> ((blue - red) / chroma + 2) / 6
                else -> ((red - green) / chroma + 4) / 6
            }
            return Hsb(if (hue < 0) hue + 1 else hue, if (maximum == 0.0) 0.0 else chroma / maximum, maximum)
        }

    private fun fromHsb(hue: Double, saturation: Double, brightness: Double): RgbaColor {
        val segment = (hue - floor(hue)) * 6
        val chroma = brightness * saturation
        val second = chroma * (1 - abs(segment % 2 - 1))
        val match = brightness - chroma
        val channels = when (segment.toInt()) {
            0 -> RgbaColor(chroma, second, 0.0)
            1 -> RgbaColor(second, chroma, 0.0)
            2 -> RgbaColor(0.0, chroma, second)
            3 -> RgbaColor(0.0, second, chroma)
            4 -> RgbaColor(second, 0.0, chroma)
            else -> RgbaColor(chroma, 0.0, second)
        }
        return RgbaColor(channels.red + match, channels.green + match, channels.blue + match)
    }

    private fun mix(first: RgbaColor, second: RgbaColor, amount: Double) = RgbaColor(
        first.red + (second.red - first.red) * amount,
        first.green + (second.green - first.green) * amount,
        first.blue + (second.blue - first.blue) * amount,
    )

    private fun perceptualDistance(first: RgbaColor, second: RgbaColor): Double {
        val hueDelta = abs(first.hsb.hue - second.hsb.hue).let { minOf(it, 1 - it) } * 0.40
        return sqrt(
            ((first.red - second.red) * 0.30).let { it * it } +
                ((first.green - second.green) * 0.59).let { it * it } +
                ((first.blue - second.blue) * 0.11).let { it * it } + hueDelta * hueDelta,
        )
    }

    private class ColorBucket(var weight: Double = 0.0, var red: Double = 0.0, var green: Double = 0.0, var blue: Double = 0.0)
}
