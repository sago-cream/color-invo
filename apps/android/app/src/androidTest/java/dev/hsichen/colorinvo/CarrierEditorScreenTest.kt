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

    @Test fun carrierEntryValidatesAndOpensVisualStyleChoices() {
        composeRule.onNodeWithTag("setup-next").assertIsNotEnabled()
        composeRule.onNodeWithTag("carrier-code").performTextReplacement("abc1234")
        composeRule.onNodeWithTag("carrier-code").assertTextContains("ABC1234")
        composeRule.onNodeWithTag("carrier-code").performImeAction()
        composeRule.waitUntil(5000) { model.state.value.widgetIsReady }
        composeRule.onNodeWithTag("carrier-code").assertDoesNotExist()
        for (style in listOf("cat", "wave", "none")) composeRule.onNodeWithTag("decoration-$style").assertIsDisplayed()
        composeRule.onNodeWithTag("show-carrier-value").assertIsOn()
        org.junit.Assert.assertEquals("/ABC1234", model.state.value.settings.carrierCode)
    }

    @Test fun invalidCharactersShowAnEmptyPreviewWithoutReplacingSavedCarrier() {
        composeRule.onNodeWithTag("carrier-code").performTextReplacement("ABC1234")
        composeRule.waitUntil(5000) { model.state.value.widgetIsReady }
        composeRule.onNodeWithTag("carrier-code").performTextReplacement("ABC_123")
        composeRule.onNodeWithTag("carrier-code").performImeAction()
        composeRule.onNodeWithTag("setup-next").assertIsNotEnabled()
        org.junit.Assert.assertEquals("/ABC1234", CarrierStore(composeRule.activity).load().carrierCode)
    }

    @Test fun decorationAndWholeSwitchRowCanBeChanged() {
        enterCarrierAndOpenStyle()

        composeRule.onNodeWithTag("decoration-wave").performClick().assertIsSelected()
        composeRule.onNodeWithTag("show-carrier-value").performClick().assertIsOff()
        composeRule.onNodeWithTag("decoration-wave").assertIsSelected()
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
        composeRule.onNodeWithTag("setup-next").performClick()
        composeRule.runOnIdle { model.loadWallpaper(Uri.fromFile(file)) }
        composeRule.waitUntil(5000) { !model.state.value.isAnalyzingWallpaper && model.state.value.wallpaperPalettes.size == 3 }
        composeRule.onNodeWithTag("wallpaper-palette-1").performScrollTo().performClick().assertIsSelected()
        composeRule.onNodeWithTag("background-color").assertDoesNotExist()
        composeRule.onNodeWithTag("custom-colors").performScrollTo().performClick()
        composeRule.onNodeWithTag("background-color").performScrollTo().performClick()
        composeRule.onNodeWithTag("background-color-hex").performTextReplacement("#FFFFFF")
        composeRule.onNodeWithTag("color-picker-done").performClick()
        composeRule.onNodeWithTag("wallpaper-palette-1").assertIsNotSelected()
        composeRule.onNodeWithTag("reset-wallpaper-palette").performScrollTo().assertIsEnabled().performClick().assertDoesNotExist()
        composeRule.onNodeWithTag("wallpaper-palette-1").performScrollTo().assertIsSelected()
        composeRule.waitUntil(5000) { model.state.value.widgetIsReady }
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync { composeRule.activity.viewModelStore.clear() }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitUntil(5000) { model.state.value.wallpaperPreview != null }
        composeRule.onNodeWithTag("wallpaper-palette-1").performScrollTo().assertIsSelected()
        composeRule.onNodeWithTag("reset-wallpaper-palette").assertDoesNotExist()
        file.delete()
    }
    @Test fun setupKeepsChoicesAcrossPagesAndBlocksUnsafeColors() {
        composeRule.onNodeWithTag("setup-next").assertIsNotEnabled()
        composeRule.onNodeWithTag("carrier-code").performTextReplacement("ABC")
        composeRule.onNodeWithTag("setup-next").assertIsNotEnabled()
        composeRule.onNodeWithTag("carrier-code").performTextReplacement("ABC1234")
        composeRule.onNodeWithTag("carrier-code").performImeAction()
        composeRule.onNodeWithTag("carrier-code").assertDoesNotExist()
        composeRule.onNodeWithTag("decoration-wave").performClick()
        composeRule.onNodeWithTag("show-carrier-value").performClick().assertIsOff()
        composeRule.onNodeWithTag("setup-next").performClick()
        composeRule.onNodeWithTag("decoration-wave").assertDoesNotExist()
        composeRule.runOnIdle { model.updateBarColor("#FFFFFF") }
        composeRule.onNodeWithTag("setup-next").assertIsNotEnabled()
        composeRule.onNodeWithTag("use-classic-palette").performScrollTo().performClick()
        composeRule.onNodeWithTag("setup-next").assertIsEnabled().performClick()
        composeRule.onNodeWithTag("setup-title").assertTextEquals(composeRule.activity.getString(R.string.setup_install_title))
        composeRule.onNodeWithTag("background-color").assertDoesNotExist()
        composeRule.onNodeWithTag("setup-back").performClick()
        composeRule.onNodeWithTag("setup-back").performClick()
        composeRule.onNodeWithTag("decoration-wave").assertIsSelected()
        composeRule.onNodeWithTag("show-carrier-value").assertIsOff()
        composeRule.onNodeWithTag("setup-back").performClick()
        composeRule.onNodeWithTag("carrier-code").assertTextContains("ABC1234")
    }

    private fun enterCarrierAndOpenStyle() {
        composeRule.onNodeWithTag("carrier-code").performTextReplacement("ABC1234")
        composeRule.onNodeWithTag("carrier-code").performImeAction()
    }

}
