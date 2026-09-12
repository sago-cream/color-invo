package dev.hsichen.colorinvo.data

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import dev.hsichen.colorinvo.domain.BarcodePalette
import dev.hsichen.colorinvo.domain.RgbaColor
import dev.hsichen.colorinvo.domain.WallpaperPaletteGenerator
import dev.hsichen.colorinvo.ui.CarrierEditorViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test

class CarrierParityTest {
    private val application get() = ApplicationProvider.getApplicationContext<Application>()

    @Test fun storePreservesValidWidgetAndWallpaperSelection() {
        val context = application.createDeviceProtectedStorageContext()
        val store = CarrierStore(context)
        val colors = listOf(RgbaColor(0x70D6FF), RgbaColor(0xFF9770), RgbaColor(0x396791))
        val base = WallpaperPaletteGenerator.palettes(colors)[1]
        val settings = CarrierSettings("/ABC1234", base, colors, Decoration.WAVE, false, base, colors[1])
        assertTrue(store.save(settings))
        assertFalse(store.save(settings.copy(carrierCode = "/ABC")))
        assertFalse(store.save(settings.copy(palette = base.copy(barColor = RgbaColor(0xFF0000)))))
        val restored = store.load()
        assertEquals(settings.carrierCode, restored.carrierCode)
        assertTrue(base.hasSameColors(restored.wallpaperBasePalette))
        assertTrue(base.hasSameColors(restored.palette))
        assertEquals(colors[1], restored.waveColor)
        assertEquals(Decoration.WAVE, restored.decoration)
        assertFalse(restored.showsBarcodeValue)
    }

    @Test fun invalidDraftDoesNotSyncAndValidCorrectionDoes() = runBlocking {
        withModel { model, repository ->
            model.updateCarrierSuffix("ABC")
            delay(500)
            assertTrue(repository.saved.isEmpty())
            assertFalse(model.state.value.widgetIsReady)
            assertFalse(model.state.value.isSaving)
            model.updateCarrierSuffix("XYZ1234")
            delay(500)
            assertEquals("/XYZ1234", repository.saved.last().carrierCode)
            assertTrue(model.state.value.widgetIsReady)
            model.updateBarColor("#FF0000")
            delay(500)
            assertEquals(1, repository.saved.size)
            assertFalse(model.state.value.widgetIsReady)
        }
    }

    @Test fun lateWallpaperCannotReplaceNewerImportOrManualEdits() = runBlocking {
        withModel { model, repository ->
            model.loadWallpaper(Uri.parse("content://test/old"))
            delay(50)
            model.loadWallpaper(Uri.parse("content://test/new"))
            model.updateBackgroundColor("#FFFFFF")
            val newer = WallpaperAnalysis(createBitmap(8, 8), listOf(RgbaColor(0x70D6FF)))
            repository.newImage.complete(newer)
            delay(100)
            repository.oldImage.complete(WallpaperAnalysis(createBitmap(8, 8), listOf(RgbaColor(0xFF9770))))
            delay(100)
            assertEquals(newer.colors, model.state.value.settings.dominantColors)
            assertSame(newer.preview, model.state.value.wallpaperPreview)
            assertEquals("#FFFFFF", model.state.value.settings.palette.backgroundColor.hex)
            assertTrue(model.state.value.canResetPalette)
            model.resetPaletteToWallpaper()
            assertFalse(model.state.value.canResetPalette)
            model.selectPalette(model.state.value.wallpaperPalettes[2])
            assertEquals(2, model.state.value.selectedPaletteIndex)
            assertEquals(newer.colors.first(), model.state.value.settings.waveColor)
        }
    }

    @Test fun writesAreSerializedAndRevertingDuringSaveIsNotLost() = runBlocking {
        withModel { model, repository ->
            repository.saveGate = CompletableDeferred()
            model.setDecoration(Decoration.WAVE)
            delay(450)
            model.setDecoration(Decoration.CAT)
            delay(450)
            repository.saveGate!!.complete(Unit)
            delay(500)
            assertEquals(listOf(Decoration.WAVE, Decoration.CAT), repository.saved.map { it.decoration })
            assertTrue(model.state.value.widgetIsReady)
        }
    }

    @Test fun unreadableWallpaperKeepsThePreviousSelection() = runBlocking {
        withModel { model, repository ->
            model.loadWallpaper(Uri.parse("content://test/new"))
            repository.newImage.complete(null)
            delay(100)
            assertTrue(model.state.value.wallpaperError)
            assertFalse(model.state.value.isAnalyzingWallpaper)
            assertEquals(BarcodePalette.Classic, model.state.value.settings.palette)
        }
    }

    @Test fun downsampledPreviewSurvivesRepositoryRecreation() = runBlocking {
        val repository = LocalCarrierRepository(application)
        val file = java.io.File(application.cacheDir, "wallpaper-test.png")
        val source = createBitmap(1800, 2400).apply { eraseColor(android.graphics.Color.BLUE) }
        file.outputStream().use { source.compress(Bitmap.CompressFormat.PNG, 100, it) }
        source.recycle()
        try {
            val analysis = repository.analyze(Uri.fromFile(file))!!
            assertEquals(900, maxOf(analysis.preview.width, analysis.preview.height))
            assertTrue(analysis.colors.isNotEmpty())
            repository.savePreview(analysis.preview)
            val loaded = LocalCarrierRepository(application).loadPreview()!!
            assertEquals(analysis.preview.width, loaded.width)
            assertEquals(analysis.preview.height, loaded.height)
        } finally {
            file.delete()
            java.io.File(application.filesDir, "wallpaper-preview.jpg").delete()
        }
    }

    private suspend fun withModel(block: suspend (CarrierEditorViewModel, FakeRepository) -> Unit) = withContext(Dispatchers.Main) {
        val repository = FakeRepository()
        val model = CarrierEditorViewModel(application, repository)
        val owner = ViewModelStore().apply { put("editor", model) }
        try { block(model, repository) } finally { owner.clear() }
    }

    private class FakeRepository : CarrierRepository {
        val saved = mutableListOf<CarrierSettings>()
        val oldImage = CompletableDeferred<WallpaperAnalysis?>()
        val newImage = CompletableDeferred<WallpaperAnalysis?>()
        var saveGate: CompletableDeferred<Unit>? = null
        override fun load() = CarrierSettings(carrierCode = "/ABC1234")
        override suspend fun loadPreview(): Bitmap? = null
        override suspend fun analyze(uri: Uri) = withContext(NonCancellable) {
            if (uri.lastPathSegment == "old") oldImage.await() else newImage.await()
        }
        override suspend fun savePreview(preview: Bitmap) = Unit
        override suspend fun save(settings: CarrierSettings): Boolean {
            saveGate?.await()
            saved += settings
            return true
        }
        override suspend fun updateWidgets() = Unit
    }
}
