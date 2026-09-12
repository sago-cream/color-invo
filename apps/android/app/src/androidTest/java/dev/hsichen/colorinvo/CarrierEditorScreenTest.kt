package dev.hsichen.colorinvo

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModelProvider
import dev.hsichen.colorinvo.data.CarrierStore
import dev.hsichen.colorinvo.ui.CarrierEditorViewModel
import java.io.File
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CarrierEditorScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    private val model get() = ViewModelProvider(composeRule.activity)[CarrierEditorViewModel::class.java]

    @Before fun resetEditor() {
        composeRule.activity.getSharedPreferences("carrier-settings", 0).edit(commit = true) { clear() }
        File(composeRule.activity.filesDir, "wallpaper-preview.jpg").delete()
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync { composeRule.activity.viewModelStore.clear() }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
    }

    @Test fun carrierEntryUpdatesPreviewAndValidation() {
        composeRule.onNodeWithTag("widget-status").assertTextContains(composeRule.activity.getString(R.string.not_synced))
        composeRule.onNodeWithTag("carrier-code").performTextReplacement("abc1234")
        composeRule.onNodeWithTag("carrier-code").performImeAction()
        composeRule.onNodeWithTag("carrier-code").assertTextContains("ABC1234")
        composeRule.waitUntil(5000) { model.state.value.widgetIsReady }
        composeRule.onNodeWithTag("widget-status").assertTextContains(composeRule.activity.getString(R.string.synced))
        composeRule.onNodeWithTag("show-carrier-value").assertIsOn()
        composeRule.onNodeWithTag("carrier-preview").assertContentDescriptionContains("/ABC1234", substring = true)
    }

    @Test fun invalidCharactersShowAnEmptyPreviewWithoutReplacingSavedCarrier() {
        composeRule.onNodeWithTag("carrier-code").performTextReplacement("ABC1234")
        composeRule.onNodeWithTag("carrier-code").performImeAction()
        composeRule.waitUntil(5000) { model.state.value.widgetIsReady }
        composeRule.onNodeWithTag("carrier-code").performTextReplacement("ABC_123")
        composeRule.onNodeWithTag("carrier-code").performImeAction()
        composeRule.onNodeWithTag("widget-status").assertTextContains(composeRule.activity.getString(R.string.not_synced))
        org.junit.Assert.assertEquals("/ABC1234", CarrierStore(composeRule.activity).load().carrierCode)
    }

    @Test fun decorationAndWholeSwitchRowCanBeChanged() {
        composeRule.onNodeWithTag("decoration-wave").performClick().assertIsSelected()
        composeRule.onNodeWithTag("show-carrier-value").performClick().assertIsOff()
    }

    @Test fun importedWallpaperSupportsSelectionColorEditingResetAndRelaunch() {
        val context = composeRule.activity
        val file = File(context.cacheDir, "editor-wallpaper.png")
        createBitmap(40, 40).apply { eraseColor(android.graphics.Color.CYAN) }.let { bitmap ->
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        composeRule.onNodeWithTag("carrier-code").performTextReplacement("ABC1234")
        composeRule.onNodeWithTag("carrier-code").performImeAction()
        composeRule.runOnIdle { model.loadWallpaper(Uri.fromFile(file)) }
        composeRule.waitUntil(5000) { !model.state.value.isAnalyzingWallpaper && model.state.value.wallpaperPalettes.size == 3 }
        composeRule.onNodeWithTag("wallpaper-palette-1").performScrollTo().performClick().assertIsSelected()
        composeRule.onNodeWithTag("background-color").performScrollTo().performClick()
        composeRule.onNodeWithTag("background-color-hex").performTextReplacement("#FFFFFF")
        composeRule.onNodeWithTag("color-picker-done").performClick()
        composeRule.onNodeWithTag("wallpaper-palette-1").assertIsSelected()
        composeRule.onNodeWithTag("reset-wallpaper-palette").assertIsEnabled().performClick().assertIsNotEnabled()
        composeRule.waitUntil(5000) { model.state.value.widgetIsReady }
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync { composeRule.activity.viewModelStore.clear() }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitUntil(5000) { model.state.value.wallpaperPreview != null }
        composeRule.onNodeWithTag("wallpaper-palette-1").performScrollTo().assertIsSelected()
        composeRule.onNodeWithTag("reset-wallpaper-palette").assertIsNotEnabled()
        file.delete()
    }
}
