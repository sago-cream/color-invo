package dev.hsichen.colorinvo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperPaletteGeneratorTest {
    @Test fun fillsMissingSourceColorsWithThreeDistinctOptions() {
        val palettes = WallpaperPaletteGenerator.palettes(listOf(RgbaColor(0x70D6FF)))
        assertEquals(3, palettes.size)
        assertEquals(3, palettes.map { it.backgroundColor.hex }.distinct().size)
        assertTrue(palettes.all(BarcodePalette::meetsCommercialGuidance))
    }

    @Test fun quantizedPalettesRemainScannerReadyAcrossTheColorCube() {
        for (red in 0..255 step 17) for (green in 0..255 step 17) for (blue in 0..255 step 17) {
            val palettes = WallpaperPaletteGenerator.palettes(listOf(RgbaColor(red / 255.0, green / 255.0, blue / 255.0)))
            for (palette in palettes) {
                val stored = palette.copy(barColor = RgbaColor.parse(palette.barColor.hex)!!, backgroundColor = RgbaColor.parse(palette.backgroundColor.hex)!!)
                assertTrue("${stored.barColor.hex} on ${stored.backgroundColor.hex}", stored.meetsCommercialGuidance)
            }
        }
    }

    // Values evaluated from apps/ios/Sources/Shared/BarcodePalette.swift.
    @Test fun matchesIosPaletteRecipes() {
        val palettes = WallpaperPaletteGenerator.palettes(listOf(RgbaColor(0x70D6FF), RgbaColor(0xFF9770), RgbaColor(0x396791)))
        val expected = listOf(
            listOf(0.036, 0.093, 0.15, 0.8654117647058823, 0.9614117647058824, 1.0),
            listOf(0.0432, 0.18, 0.08598109090909092, 1.0, 0.9347450980392157, 0.9102745098039215),
            listOf(0.08679745454545444, 0.13, 0.0312, 0.9223529411764706, 0.9403921568627451, 0.9568627450980393),
        )
        palettes.forEachIndexed { index, palette ->
            val actual = listOf(palette.barColor.red, palette.barColor.green, palette.barColor.blue, palette.backgroundColor.red, palette.backgroundColor.green, palette.backgroundColor.blue)
            actual.forEachIndexed { channel, value -> assertEquals(expected[index][channel], value, 1e-12) }
        }
    }

    @Test fun createsThreeScannerReadyPalettes() {
        val palettes = WallpaperPaletteGenerator.palettes(
            listOf(RgbaColor(0x70D6FF), RgbaColor(0xFF9770), RgbaColor(0x396791)),
        )
        assertEquals(3, palettes.size)
        assertTrue(palettes.all(BarcodePalette::meetsCommercialGuidance))
    }
}
