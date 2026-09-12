package dev.hsichen.colorinvo.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import dev.hsichen.colorinvo.R
import dev.hsichen.colorinvo.data.CarrierSettings
import dev.hsichen.colorinvo.data.Decoration
import dev.hsichen.colorinvo.domain.CarrierCode
import dev.hsichen.colorinvo.domain.Code39
import kotlin.math.roundToInt

/** The editor and home-screen widget use this same artwork and pixel-aligned barcode geometry. */
object WidgetBitmapRenderer {
    fun render(context: Context, settings: CarrierSettings, width: Int = 658, height: Int = 310): Bitmap {
        val bitmap = createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1))
        val canvas = Canvas(bitmap)
        val radius = minOf(bitmap.width * 24f / 329, bitmap.height * 24f / 155)
        canvas.clipPath(Path().apply { addRoundRect(RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()), radius, radius, Path.Direction.CW) })
        draw(context, canvas, settings, bitmap.width.toFloat(), bitmap.height.toFloat())
        return bitmap
    }

    fun draw(context: Context, canvas: Canvas, settings: CarrierSettings, width: Float, height: Float, preview: Boolean = false) {
        val code = CarrierCode.normalize(settings.carrierCode).ifEmpty { if (preview) "/ABC1234" else "" }
        val background = Paint().apply { color = settings.palette.backgroundColor.argb }
        val foreground = Paint().apply { color = settings.palette.barColor.argb }
        canvas.drawRect(0f, 0f, width, height, background)
        if (!CarrierCode.isValid(code)) {
            drawEmptyState(context, canvas, foreground, width, height, preview)
            return
        }
        val (bars, totalWidth) = Code39.bars(code)
        val unit = width / totalWidth
        val rects = bars.map {
            val start = (it.startsAt * unit).roundToInt().toFloat()
            val end = ((it.startsAt + it.width) * unit).roundToInt().toFloat()
            RectF(start, 0f, maxOf(start + 1, end), height)
        }
        if (settings.decoration == Decoration.CAT) {
            val spaces = buildList {
                var left = 0f
                rects.forEach { rect -> add(RectF(left, 0f, rect.left, height)); left = rect.right }
                add(RectF(left, 0f, width, height))
            }
            CatBarcodeArtwork.draw(context, canvas, rects, spaces, code, width, height, foreground, background)
        } else {
            rects.forEach { canvas.drawRect(it, foreground) }
            if (settings.decoration == Decoration.WAVE) drawWave(canvas, settings, width, height)
        }
        if (settings.showsBarcodeValue) drawValue(canvas, code, foreground.color, background.color, width, height, settings.decoration == Decoration.WAVE)
    }

    private fun drawEmptyState(context: Context, canvas: Canvas, paint: Paint, width: Float, height: Float, preview: Boolean) {
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        paint.textSize = minOf(width / 22f, height / 9f)
        val lines = context.getString(if (preview) R.string.preview_empty else R.string.widget_empty).split('\n')
        lines.forEachIndexed { index, line ->
            canvas.drawText(line, width / 2, height / 2 + (index - (lines.size - 1) / 2f) * paint.textSize * 1.4f - (paint.ascent() + paint.descent()) / 2, paint)
        }
    }

    private fun drawValue(canvas: Canvas, code: String, foreground: Int, background: Int, width: Float, height: Float, top: Boolean) {
        val scale = minOf(width / 329f, height / 155f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = foreground
            typeface = Typeface.create("monospace", Typeface.BOLD)
            textSize = 12f * scale
            textScaleX = 0.9f
        }
        val capsuleHeight = 20f * scale
        val right = width - 8f * scale
        val y = if (top) 8f * scale else height - 8f * scale - capsuleHeight
        val rect = RectF(right - paint.measureText(code) - 16f * scale, y, right, y + capsuleHeight)
        canvas.drawRoundRect(rect, capsuleHeight / 2, capsuleHeight / 2, paint)
        paint.color = background
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(code, rect.centerX(), rect.centerY() - (paint.ascent() + paint.descent()) / 2, paint)
    }

    private fun drawWave(canvas: Canvas, settings: CarrierSettings, width: Float, height: Float) {
        val waveHeight = minOf(height * 0.49f, width * 76f / 329f)
        fun x(value: Float) = width * value / 900f
        fun y(value: Float) = waveHeight * (601f - value) / 341.429f
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, y(435.3f))
            cubicTo(x(8.5f), y(397.7f), x(42f), y(313.274f), x(107.8f), y(313.2f))
            cubicTo(x(193.926f), y(313.103f), x(189.338f), y(516.203f), x(275.2f), y(519.5f))
            cubicTo(x(365.968f), y(522.985f), x(378.1f), y(381f), x(450f), y(380f))
            cubicTo(x(523.814f), y(378.973f), x(569.117f), y(460.46f), x(629.8f), y(457.5f))
            cubicTo(x(702.511f), y(453.953f), x(704.125f), y(259.815f), x(807.394f), y(259.571f))
            cubicTo(x(887.814f), y(259.381f), x(900f), y(381f), x(900f), y(381f))
            lineTo(width, 0f)
            close()
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = settings.waveColor?.argb ?: settings.dominantColors.firstOrNull()?.argb ?: 0xFF0066FF.toInt()
        }
        canvas.drawPath(path, paint)
    }
}
