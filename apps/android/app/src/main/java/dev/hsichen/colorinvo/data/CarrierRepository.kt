package dev.hsichen.colorinvo.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.AtomicFile
import androidx.core.graphics.scale
import androidx.glance.appwidget.updateAll
import dev.hsichen.colorinvo.domain.RgbaColor
import dev.hsichen.colorinvo.domain.WallpaperPaletteGenerator
import dev.hsichen.colorinvo.widget.ColorInvoWidget
import java.io.File
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WallpaperAnalysis(val preview: Bitmap, val colors: List<RgbaColor>)

interface CarrierRepository {
    fun load(): CarrierSettings
    suspend fun loadPreview(): Bitmap?
    suspend fun analyze(uri: Uri): WallpaperAnalysis?
    suspend fun savePreview(preview: Bitmap)
    suspend fun save(settings: CarrierSettings): Boolean
    suspend fun updateWidgets()
}

class LocalCarrierRepository(private val context: Context) : CarrierRepository {
    private val previewMutex = Mutex()
    private val store = CarrierStore(context)
    private val previewFile = AtomicFile(File(context.filesDir, "wallpaper-preview.jpg"))

    override fun load() = store.load()

    override suspend fun loadPreview(): Bitmap? = withContext(Dispatchers.IO) {
        runCatching { previewFile.openRead().use { BitmapFactory.decodeStream(it) } }.getOrNull()
    }

    override suspend fun analyze(uri: Uri): WallpaperAnalysis? = withContext(Dispatchers.IO) {
        runCatching {
            val preview = decodePreview(uri) ?: return@runCatching null
            val colors = WallpaperPaletteGenerator.representativeColors(preview)
            if (colors.isEmpty()) {
                preview.recycle()
                null
            } else WallpaperAnalysis(preview, colors)
        }.getOrNull()
    }

    override suspend fun savePreview(preview: Bitmap) = withContext(Dispatchers.IO) {
        previewMutex.withLock {
        val stream = previewFile.startWrite()
        try {
            check(preview.compress(Bitmap.CompressFormat.JPEG, 76, stream))
            previewFile.finishWrite(stream)
        } catch (error: Exception) {
            previewFile.failWrite(stream)
            throw error
        }
        }
    }

    override suspend fun save(settings: CarrierSettings) = withContext(Dispatchers.IO) { store.save(settings) }
    override suspend fun updateWidgets() { ColorInvoWidget().updateAll(context) }

    private fun decodePreview(uri: Uri): Bitmap? {
        if (Build.VERSION.SDK_INT >= 28) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
                val scale = minOf(1.0, MAXIMUM_DIMENSION.toDouble() / maxOf(info.size.width, info.size.height))
                decoder.setTargetSize(maxOf(1, (info.size.width * scale).toInt()), maxOf(1, (info.size.height * scale).toInt()))
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAXIMUM_DIMENSION * 2) sample *= 2
        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
        }.getOrNull()
        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> { setRotate(90f); postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> { setRotate(-90f); postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90f)
            }
        }
        val oriented = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        if (oriented !== decoded) decoded.recycle()
        val scale = minOf(1.0, MAXIMUM_DIMENSION.toDouble() / maxOf(oriented.width, oriented.height))
        val preview = oriented.scale(maxOf(1, (oriented.width * scale).toInt()), maxOf(1, (oriented.height * scale).toInt()))
        if (preview !== oriented) oriented.recycle()
        return preview
    }

    private companion object { const val MAXIMUM_DIMENSION = 900 }
}
