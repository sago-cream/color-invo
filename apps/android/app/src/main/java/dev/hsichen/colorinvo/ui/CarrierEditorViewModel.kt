package dev.hsichen.colorinvo.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.hsichen.colorinvo.data.CarrierRepository
import dev.hsichen.colorinvo.data.CarrierSettings
import dev.hsichen.colorinvo.data.Decoration
import dev.hsichen.colorinvo.data.LocalCarrierRepository
import dev.hsichen.colorinvo.domain.BarcodePalette
import dev.hsichen.colorinvo.domain.CarrierCode
import dev.hsichen.colorinvo.domain.RgbaColor
import dev.hsichen.colorinvo.domain.WallpaperPaletteGenerator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CarrierEditorState(
    val settings: CarrierSettings,
    val savedSettings: CarrierSettings = settings,
    val wallpaperPalettes: List<BarcodePalette> = emptyList(),
    val wallpaperPreview: Bitmap? = null,
    val isAnalyzingWallpaper: Boolean = false,
    val wallpaperError: Boolean = false,
    val saveError: Boolean = false,
) {
    val carrierSuffix get() = settings.carrierCode.removePrefix("/")
    val carrierIsValid get() = CarrierCode.isValid(settings.carrierCode)
    val isSaving get() = settings.canSync && settings != savedSettings && !saveError
    val widgetIsReady get() = settings.canSync && settings == savedSettings && !saveError
    val selectedPaletteIndex get() = wallpaperPalettes.indexOfFirst { it.hasSameColors(settings.wallpaperBasePalette) }
    val canResetPalette get() = settings.wallpaperBasePalette?.let { !it.hasSameColors(settings.palette) } == true
}

class CarrierEditorViewModel(
    application: Application,
    private val repository: CarrierRepository,
) : AndroidViewModel(application) {
    constructor(application: Application) : this(application, LocalCarrierRepository(application))

    private val saveRequests = Channel<CarrierSettings?>(Channel.CONFLATED)
    private var wallpaperJob: Job? = null
    private var wallpaperRequest = 0
    private var paletteRevision = 0
    val state = mutableStateOf(repository.load().let { CarrierEditorState(it, wallpaperPalettes = WallpaperPaletteGenerator.palettes(it.dominantColors)) })

    @OptIn(FlowPreview::class)
    private fun startSaving() = viewModelScope.launch {
        // A single writer finishes an in-flight save before handling the latest draft.
        saveRequests.receiveAsFlow().debounce(350).collect { settings ->
            if (settings == null || settings == state.value.savedSettings) return@collect
            withContext(NonCancellable) {
                try {
                    if (repository.save(settings)) {
                        repository.updateWidgets()
                        state.value = state.value.copy(savedSettings = settings, saveError = false)
                    } else state.value = state.value.copy(saveError = true)
                } catch (_: Exception) {
                    state.value = state.value.copy(saveError = true)
                }
            }
        }
    }

    init {
        startSaving()
        viewModelScope.launch {
            val preview = repository.loadPreview()
            if (wallpaperRequest == 0) state.value = state.value.copy(wallpaperPreview = preview)
        }
    }

    fun updateCarrierSuffix(value: String) = updateSettings { copy(carrierCode = CarrierCode.fromSuffix(value)) }

    fun updateBarColor(value: String) {
        RgbaColor.parse(value)?.let { color ->
            paletteRevision++
            updateSettings { copy(palette = palette.copy(name = "自訂", barColor = color)) }
        }
    }

    fun updateBackgroundColor(value: String) {
        RgbaColor.parse(value)?.let { color ->
            paletteRevision++
            updateSettings { copy(palette = palette.copy(name = "自訂", backgroundColor = color)) }
        }
    }

    fun selectPalette(palette: BarcodePalette) {
        paletteRevision++
        updateSettings {
            val index = state.value.wallpaperPalettes.indexOf(palette)
            copy(palette = palette, wallpaperBasePalette = palette, waveColor = dominantColors.getOrNull(index) ?: dominantColors.firstOrNull())
        }
    }

    fun resetPaletteToWallpaper() {
        val base = state.value.settings.wallpaperBasePalette ?: return
        paletteRevision++
        updateSettings { copy(palette = base) }
    }

    fun setDecoration(decoration: Decoration) = updateSettings { copy(decoration = decoration) }
    fun setShowsBarcodeValue(value: Boolean) = updateSettings { copy(showsBarcodeValue = value) }

    fun loadWallpaper(uri: Uri) {
        wallpaperJob?.cancel()
        val request = ++wallpaperRequest
        val revision = paletteRevision
        state.value = state.value.copy(isAnalyzingWallpaper = true, wallpaperError = false)
        wallpaperJob = viewModelScope.launch {
            try {
                val result = repository.analyze(uri)
                if (request != wallpaperRequest) return@launch
                if (result == null) {
                    state.value = state.value.copy(isAnalyzingWallpaper = false, wallpaperError = true)
                    return@launch
                }
                repository.savePreview(result.preview)
                if (request != wallpaperRequest) return@launch
                val palettes = WallpaperPaletteGenerator.palettes(result.colors)
                state.value = state.value.copy(
                    settings = state.value.settings.copy(
                        dominantColors = result.colors,
                        palette = if (revision == paletteRevision) palettes.first() else state.value.settings.palette,
                        wallpaperBasePalette = palettes.first(),
                        waveColor = result.colors.first(),
                    ),
                    wallpaperPreview = result.preview,
                    wallpaperPalettes = palettes,
                    isAnalyzingWallpaper = false,
                )
                scheduleSave()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (request == wallpaperRequest) state.value = state.value.copy(isAnalyzingWallpaper = false, wallpaperError = true)
            }
        }
    }

    private fun updateSettings(transform: CarrierSettings.() -> CarrierSettings) {
        val settings = state.value.settings.transform()
        if (settings == state.value.settings) return
        state.value = state.value.copy(settings = settings)
        scheduleSave()
    }

    private fun scheduleSave() {
        state.value = state.value.copy(saveError = false)
        saveRequests.trySend(state.value.settings.takeIf { it.canSync })
    }
}
