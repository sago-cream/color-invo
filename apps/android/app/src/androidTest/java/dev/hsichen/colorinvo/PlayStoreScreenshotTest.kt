package dev.hsichen.colorinvo

import android.app.LocaleManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.LocaleList
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import dev.hsichen.colorinvo.data.CarrierSettings
import dev.hsichen.colorinvo.data.CarrierStore
import dev.hsichen.colorinvo.domain.RgbaColor
import dev.hsichen.colorinvo.domain.WallpaperPaletteGenerator
import dev.hsichen.colorinvo.ui.CarrierEditorViewModel
import dev.hsichen.colorinvo.widget.WidgetBitmapRenderer
import java.io.File
import org.junit.Rule
import org.junit.Test

/** Synthetic carrier and wallpaper only. Run scripts/android-screenshots.sh to export both locales. */
class PlayStoreScreenshotTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val locale get() = InstrumentationRegistry.getArguments().getString("locale", "en-US")
    private val outputDirectory get() = File(context.getExternalFilesDir(null), "play-store/$locale").apply { mkdirs() }

    @Test fun captureListing() {
        if (Build.VERSION.SDK_INT >= 33) context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(locale)
        val colors = listOf(RgbaColor(0x59BAC9), RgbaColor(0xFFB876), RgbaColor(0x396791))
        val palette = WallpaperPaletteGenerator.palettes(colors)[2]
        val settings = CarrierSettings("/ABC1234", palette, colors, wallpaperBasePalette = palette, waveColor = colors[2])
        CarrierStore(context).save(settings)
        val wallpaper = createBitmap(450, 800)
        Canvas(wallpaper).apply {
            drawColor(Color.rgb(232, 248, 252))
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            colors.forEachIndexed { index, color ->
                paint.color = color.argb
                save()
                rotate(-20f, 225f, 400f)
                drawRect(-200f, index * 280f, 700f, index * 280f + 100f, paint)
                restore()
            }
        }
        File(context.filesDir, "wallpaper-preview.jpg").outputStream().use { wallpaper.compress(Bitmap.CompressFormat.JPEG, 76, it) }
        wallpaper.recycle()
        instrumentation.runOnMainSync { composeRule.activity.viewModelStore.clear() }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        val model = ViewModelProvider(composeRule.activity)[CarrierEditorViewModel::class.java]
        composeRule.waitUntil(5000) { model.state.value.wallpaperPreview != null }
        captureScreen("phoneScreenshots/1_wallpaper.png")
        composeRule.onNodeWithTag("decoration-wave").performClick()
        composeRule.waitUntil(5000) { model.state.value.widgetIsReady }
        captureScreen("phoneScreenshots/2_paint.png")
        composeRule.onNodeWithTag("background-color").performScrollTo().performClick()
        captureScreen("phoneScreenshots/3_colors.png")
        composeRule.onNodeWithTag("color-picker-done").performClick()
        // Also exercise the offline policy through its real, scrollable entry point.
        composeRule.onNodeWithTag("privacy").performScrollTo().performClick()
        composeRule.onNodeWithTag("app-information").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.done)).performClick()
        exportArtwork(settings)
    }

    private fun captureScreen(name: String) {
        composeRule.waitForIdle()
        instrumentation.uiAutomation.waitForIdle(300, 3000)
        val screenshot = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        writePng(screenshot, name)
        screenshot.recycle()
    }

    private fun exportArtwork(settings: CarrierSettings) {
        val brand = BitmapFactory.decodeResource(context.resources, R.drawable.brand_icon)
        val icon = createBitmap(512, 512)
        Canvas(icon).drawBitmap(brand, null, RectF(0f, 0f, 512f, 512f), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        writePng(icon, "icon.png", alpha = true)
        brand.recycle()
        icon.recycle()

        val graphic = createBitmap(1024, 500)
        val canvas = Canvas(graphic)
        canvas.drawColor(Color.rgb(234, 248, 255))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // Native app artwork and renderer keep the listing faithful to the shipping widget.
        paint.color = Color.rgb(205, 233, 238)
        canvas.drawCircle(940f, 32f, 208f, paint)
        paint.color = Color.rgb(255, 224, 190)
        canvas.drawCircle(48f, 520f, 176f, paint)
        val chinese = locale.startsWith("zh")
        paint.color = Color.rgb(0, 126, 168)
        paint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        paint.textSize = 24f
        canvas.drawText(if (chinese) "條色盤" else "ColorInvo", 72f, 132f, paint)
        paint.color = Color.rgb(17, 24, 39)
        paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        paint.textSize = if (chinese) 44f else 42f
        canvas.drawText(if (chinese) "載具隨手可得" else "Your carrier.", 72f, 212f, paint)
        canvas.drawText(if (chinese) "配色跟著你走" else "Your colors.", 72f, 272f, paint)
        paint.color = Color.rgb(82, 96, 106)
        paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        paint.textSize = 20f
        canvas.drawText(if (chinese) "桌布取色 · 主畫面小工具" else "Wallpaper colors. Home screen widget.", 72f, 328f, paint)
        val widget = WidgetBitmapRenderer.render(context, settings, 900, 424)
        paint.color = Color.rgb(192, 220, 230)
        canvas.drawRoundRect(508f, 152f, 964f, 372f, 32f, 32f, paint)
        canvas.drawBitmap(widget, null, RectF(504f, 140f, 960f, 355f), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        writePng(graphic, "featureGraphic.png")
        widget.recycle()
        graphic.recycle()
    }

    private fun writePng(bitmap: Bitmap, name: String, alpha: Boolean = false) {
        bitmap.setHasAlpha(alpha)
        File(outputDirectory, name).apply { parentFile?.mkdirs() }.outputStream().use {
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }
}
