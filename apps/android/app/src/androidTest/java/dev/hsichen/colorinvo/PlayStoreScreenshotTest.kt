package dev.hsichen.colorinvo

import android.app.LocaleManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.LocaleList
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.graphics.createBitmap
import androidx.test.platform.app.InstrumentationRegistry
import dev.hsichen.colorinvo.data.CarrierSettings
import dev.hsichen.colorinvo.data.CarrierStore
import dev.hsichen.colorinvo.domain.RgbaColor
import dev.hsichen.colorinvo.domain.WallpaperPaletteGenerator
import java.io.File
import org.junit.Rule
import org.junit.Test

class PlayStoreScreenshotTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun captureMainScreen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val locale = InstrumentationRegistry.getArguments().getString("locale", "en-US")
        if (Build.VERSION.SDK_INT >= 33) context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(locale)
        val colors = listOf(RgbaColor(0x59BAC9), RgbaColor(0xFFB876), RgbaColor(0x396791))
        val palette = WallpaperPaletteGenerator.palettes(colors)[2]
        CarrierStore(context).save(CarrierSettings("/ABC1234", palette, colors, wallpaperBasePalette = palette, waveColor = colors[2]))
        val wallpaper = createBitmap(450, 800)
        Canvas(wallpaper).apply {
            drawColor(android.graphics.Color.rgb(232, 248, 252))
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
        instrumentation.runOnMainSync { composeRule.activity.viewModelStore.clear() }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        val model = androidx.lifecycle.ViewModelProvider(composeRule.activity)[dev.hsichen.colorinvo.ui.CarrierEditorViewModel::class.java]
        composeRule.waitUntil(5000) { model.state.value.wallpaperPreview != null }
        composeRule.waitForIdle()
        instrumentation.uiAutomation.waitForIdle(300, 3000)
        File(context.getExternalFilesDir(null), "play-store-$locale.png").outputStream().use {
            instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
