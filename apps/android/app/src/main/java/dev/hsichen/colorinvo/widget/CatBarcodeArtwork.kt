package dev.hsichen.colorinvo.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import dev.hsichen.colorinvo.R
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/** Port of the iOS cat layout and deterministic tears; the top 39% stays a complete barcode. */
internal object CatBarcodeArtwork {
    private var silhouette: Bitmap? = null
    private var details: Bitmap? = null

    fun draw(context: Context, canvas: Canvas, bars: List<RectF>, spaces: List<RectF>, code: String, width: Float, height: Float, foreground: Paint, background: Paint) {
        val layout = Layout(width, height)
        val seed = Seed(code)
        val safeY = height * 0.39f
        val pixel = 1f
        bars.forEachIndexed { index, rect ->
            canvas.drawRect(rect.left, 0f, rect.right, safeY + pixel, foreground)
            val tear = tear(rect, index, seed, layout, safeY)
            val left = mutableListOf<PointF>()
            val right = mutableListOf<PointF>()
            for (step in 0..7) {
                val t = step / 7f
                val offset = offset(rect, index, t, seed, layout)
                val pulse = seed.signed(700 + index) * rect.width() * 0.08f * t
                val x = rect.left + offset - pulse
                left += PointF(x, safeY + maxOf(0f, tear.left - safeY) * t)
                right += PointF(maxOf(x + maxOf(pixel, rect.width() * 0.62f), rect.right + offset + pulse), safeY + maxOf(0f, tear.right - safeY) * t)
            }
            canvas.drawPath(polygon(left, right), foreground)
        }
        bars.forEachIndexed { index, rect ->
            val tear = tear(rect, index, seed, layout, safeY)
            if (tear.drawStrip) {
                val left = mutableListOf<PointF>()
                val right = mutableListOf<PointF>()
                val stripWidth = maxOf(pixel, rect.width() * tear.stripWidth)
                for (step in 0..4) {
                    val t = step / 4f
                    val y = tear.stripStart + maxOf(0f, tear.stripEnd - tear.stripStart) * t
                    val drift = seed.signed(1520 + index + step) * rect.width() * 0.12f * t
                    val center = rect.centerX() + offset(rect, index, 1f, seed, layout) + rect.width() * tear.stripBias * t + drift
                    left += PointF(center - stripWidth / 2, y)
                    right += PointF(center + stripWidth / 2, y)
                }
                canvas.drawPath(polygon(left, right), foreground)
            }
        }
        spaces.forEachIndexed { index, rect ->
            val frame = layout.frame
            if (rect.width() >= pixel && rect.width() <= width * 0.05f &&
                rect.centerX() >= frame.left - frame.width() * 0.08f && rect.centerX() <= frame.right + frame.width() * 0.08f) {
                val left = mutableListOf<PointF>()
                val right = mutableListOf<PointF>()
                val stop = layout.contactY(rect.centerX(), safeY)
                for (step in 0..7) {
                    val t = step / 7f
                    val y = safeY + maxOf(0f, stop - safeY) * t
                    val center = rect.centerX() + offset(rect, index, t, seed, layout)
                    left += PointF(center - rect.width() / 2, y)
                    right += PointF(center + rect.width() / 2, y)
                }
                canvas.drawPath(polygon(left, right), background)
            }
        }
        drawCat(context, canvas, layout.frame, foreground.color, background.color)
    }

    private fun drawCat(context: Context, canvas: Canvas, frame: RectF, foreground: Int, background: Int) {
        val cat = silhouette ?: BitmapFactory.decodeResource(context.resources, R.drawable.cat_barcode).also { silhouette = it }
        val detail = details ?: BitmapFactory.decodeResource(context.resources, R.drawable.cat_barcode_details).also { details = it }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.colorFilter = PorterDuffColorFilter(background, PorterDuff.Mode.SRC_IN)
        val outline = maxOf(1f, frame.height() * 0.012f)
        listOf(-outline to 0f, outline to 0f, 0f to -outline, 0f to outline).forEach { (x, y) ->
            val shifted = RectF(frame).apply { offset(x, y) }
            canvas.drawBitmap(cat, null, shifted, paint)
        }
        paint.colorFilter = PorterDuffColorFilter(foreground, PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(cat, null, frame, paint)
        paint.colorFilter = PorterDuffColorFilter(background, PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(detail, null, frame, paint)
    }

    private fun polygon(left: List<PointF>, right: List<PointF>) = Path().apply {
        moveTo(left.first().x, left.first().y)
        left.drop(1).forEach { lineTo(it.x, it.y) }
        right.asReversed().forEach { lineTo(it.x, it.y) }
        close()
    }

    private fun offset(rect: RectF, index: Int, t: Float, seed: Seed, layout: Layout): Float {
        val distance = (rect.centerX() - layout.frame.centerX()) / maxOf(1f, layout.frame.width() * 0.72f)
        val pressure = maxOf(0f, 1 - minOf(1f, abs(distance)))
        val direction = if (distance < 0) -1f else 1f
        val bend = t * t * (3 - 2 * t)
        val shove = direction * pressure * layout.width * 0.011f * (sin(PI * t).toFloat() * 0.45f + bend * 0.55f)
        val pawShove = layout.pawDeflection(rect.centerX(), if (seed.signed(760 + index) < 0) -1f else 1f) * layout.width * 0.010f * bend
        val phase = seed.unit(720 + index) * PI * 2
        val wave = sin((t * 2.4 + 0.2) * PI + phase).toFloat() * layout.width * (0.003f + seed.unit(740 + index) * 0.004f) * t
        return shove + pawShove + wave
    }

    private fun tear(rect: RectF, index: Int, seed: Seed, layout: Layout, safeY: Float): Tear {
        val pressure = layout.pressure(rect.centerX())
        val paw = layout.pawPressure(rect.centerX())
        val contact = layout.contactY(rect.centerX(), safeY)
        val breaks = pressure > 0.16f && (seed.unit(1000 + index) > 0.42f || paw > 0.55f)
        val lift = if (breaks) layout.height * (0.05f + pressure * 0.13f + paw * 0.075f) * (0.35f + seed.unit(1040 + index) * 0.65f) else 0f
        val stop = maxOf(safeY + layout.height * 0.05f, contact - lift)
        val ragged = layout.height * (if (breaks) 0.012f + seed.unit(1080 + index) * 0.035f else 0.004f) * maxOf(pressure, paw)
        val left = maxOf(safeY + 1, minOf(contact - 1, stop + seed.signed(1120 + index) * ragged))
        val right = maxOf(safeY + 1, minOf(contact - 1, stop + seed.signed(1160 + index) * ragged))
        val start = minOf(left, right) - 1
        val length = minOf(contact - start - 1, layout.height * (0.026f + seed.unit(1240 + index) * 0.070f) * maxOf(pressure, paw))
        return Tear(left, right, start, start + maxOf(0f, length), 0.34f + seed.unit(1320 + index) * 0.34f,
            seed.signed(1360 + index) * 0.20f, breaks && rect.width() > 1.5f && length > 3 && seed.unit(1280 + index) > 0.38f)
    }

    private data class Tear(val left: Float, val right: Float, val stripStart: Float, val stripEnd: Float, val stripWidth: Float, val stripBias: Float, val drawStrip: Boolean)

    private class Layout(val width: Float, val height: Float) {
        val frame: RectF
        private val visible: RectF
        init {
            val aspect = 1106f / 654f
            val catHeight = minOf(height * 0.58f, width * 0.46f / aspect)
            val catWidth = catHeight * aspect
            val bottom = height - maxOf(1f, height * 0.015f)
            frame = RectF(width * 0.52f - catWidth / 2, bottom - catHeight, width * 0.52f + catWidth / 2, bottom)
            visible = RectF(frame.left + catWidth * 0.122f, frame.top + catHeight * 0.203f, frame.left + catWidth * 0.853f, frame.top + catHeight * 0.830f)
        }
        private fun unit(x: Float) = (x - visible.left) / maxOf(1f, visible.width())
        private fun contains(x: Float) = x >= visible.left && x <= visible.right
        fun pressure(x: Float): Float = maxOf(0f, 1 - abs(x - visible.centerX()) / maxOf(1f, visible.width() * 0.68f))
        fun pawPressure(x: Float): Float = if (!contains(x)) 0f else maxOf(tent(unit(x), 0.46f, 0.085f), tent(unit(x), 0.66f, 0.095f))
        fun pawDeflection(x: Float, fallback: Float): Float {
            if (!contains(x)) return 0f
            val u = unit(x)
            val left = tent(u, 0.46f, 0.085f)
            val right = tent(u, 0.66f, 0.095f)
            val force = left * (if (u < 0.46f) -1 else 1) + right * (if (u < 0.66f) -1 else 1)
            return if (abs(force) < 0.04f) fallback * maxOf(left, right) * 0.30f else force.coerceIn(-1f, 1f)
        }
        fun contactY(x: Float, safeY: Float): Float {
            val collision = if (!contains(x)) visible.bottom else {
                val u = unit(x)
                val lift = maxOf(tent(u, 0.12f, 0.12f) * 0.24f, tent(u, 0.28f, 0.055f) * 0.60f,
                    tent(u, 0.43f, 0.09f) * 0.26f, tent(u, 0.52f, 0.32f) * 0.24f,
                    tent(u, 0.67f, 0.09f) * 0.66f, tent(u, 0.86f, 0.08f) * 0.28f)
                minOf(visible.bottom, visible.top + visible.height() * (0.76f - lift).coerceIn(0.04f, 0.76f))
            }
            return maxOf(safeY + 1, collision - height * (0.010f + pawPressure(x) * 0.060f))
        }
        private fun tent(value: Float, center: Float, radius: Float) = maxOf(0f, 1 - abs(value - center) / radius)
    }

    private class Seed(text: String) {
        private val value = "cat-barcode-v1|$text".toByteArray(Charsets.UTF_8).fold(1_469_598_103_934_665_603uL) { hash, byte ->
            (hash xor byte.toUByte().toULong()) * 1_099_511_628_211uL
        }
        fun unit(salt: Int): Float {
            var number = value + salt.toULong() * 0x9E3779B97F4A7C15uL
            number = (number xor (number shr 30)) * 0xBF58476D1CE4E5B9uL
            number = (number xor (number shr 27)) * 0x94D049BB133111EBuL
            number = number xor (number shr 31)
            return ((number shr 11).toDouble() / 9_007_199_254_740_992.0).toFloat()
        }
        fun signed(salt: Int) = unit(salt) * 2 - 1
    }
}
