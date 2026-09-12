package dev.hsichen.colorinvo.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import dev.hsichen.colorinvo.domain.BarcodePalette
import dev.hsichen.colorinvo.domain.CarrierCode
import dev.hsichen.colorinvo.domain.RgbaColor
import dev.hsichen.colorinvo.domain.WallpaperPaletteGenerator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

enum class Decoration { CAT, WAVE, NONE }

data class CarrierSettings(
    val carrierCode: String = "",
    val palette: BarcodePalette = BarcodePalette.Classic,
    val dominantColors: List<RgbaColor> = emptyList(),
    val decoration: Decoration = Decoration.CAT,
    val showsBarcodeValue: Boolean = true,
    val wallpaperBasePalette: BarcodePalette? = null,
    val waveColor: RgbaColor? = null,
) {
    val canSync get() = CarrierCode.isValid(carrierCode) && palette.meetsCommercialGuidance
}

class CarrierStore(context: Context) {
    private val preferences = context.getSharedPreferences("carrier-settings", Context.MODE_PRIVATE)

    fun observe() = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(load()) }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(load())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.conflate().distinctUntilChanged()

    fun load(): CarrierSettings {
        val palette = readPalette("") ?: BarcodePalette.Classic
        val colors = preferences.getString("dominantColors", "").orEmpty()
            .split(',').mapNotNull(RgbaColor::parse).take(3)
        val options = WallpaperPaletteGenerator.palettes(colors)
        val base = readPalette("wallpaperBase")
            ?: options.firstOrNull { it.hasSameColors(palette) }
            ?: options.firstOrNull()
        val selectedIndex = options.indexOfFirst { it.hasSameColors(base) }
        return CarrierSettings(
            carrierCode = preferences.getString("carrierCode", "").orEmpty(),
            palette = palette,
            dominantColors = colors,
            decoration = preferences.getString("decoration", null)
                ?.let { runCatching { Decoration.valueOf(it) }.getOrNull() } ?: Decoration.CAT,
            showsBarcodeValue = preferences.getBoolean("showsBarcodeValue", true),
            wallpaperBasePalette = base,
            waveColor = RgbaColor.parse(preferences.getString("waveColor", "").orEmpty())
                ?: colors.getOrNull(selectedIndex) ?: colors.firstOrNull(),
        )
    }

    // Run off the main thread. Invalid drafts must never replace a working widget.
    @SuppressLint("UseKtx") // KTX edit discards commit() failure; sync status needs the result.
    fun save(settings: CarrierSettings): Boolean {
        if (!settings.canSync) return false
        return preferences.edit().apply {
            putString("carrierCode", CarrierCode.normalize(settings.carrierCode))
            putString("paletteName", settings.palette.name)
            putString("barColor", settings.palette.barColor.hex)
            putString("backgroundColor", settings.palette.backgroundColor.hex)
            putString("dominantColors", settings.dominantColors.take(3).joinToString(",") { it.hex })
            putString("decoration", settings.decoration.name)
            putBoolean("showsBarcodeValue", settings.showsBarcodeValue)
            putString("wallpaperBasepaletteName", settings.wallpaperBasePalette?.name)
            putString("wallpaperBasebarColor", settings.wallpaperBasePalette?.barColor?.hex)
            putString("wallpaperBasebackgroundColor", settings.wallpaperBasePalette?.backgroundColor?.hex)
            putString("waveColor", settings.waveColor?.hex)
        }.commit()
    }

    private fun readPalette(prefix: String): BarcodePalette? {
        val bar = RgbaColor.parse(preferences.getString("${prefix}barColor", "").orEmpty()) ?: return null
        val background = RgbaColor.parse(preferences.getString("${prefix}backgroundColor", "").orEmpty()) ?: return null
        return BarcodePalette(preferences.getString("${prefix}paletteName", "自訂").orEmpty(), bar, background)
    }
}
