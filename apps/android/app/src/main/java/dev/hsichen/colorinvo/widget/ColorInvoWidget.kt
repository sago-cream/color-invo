package dev.hsichen.colorinvo.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import dev.hsichen.colorinvo.MainActivity
import dev.hsichen.colorinvo.R
import dev.hsichen.colorinvo.data.CarrierStore

class ColorInvoWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = CarrierStore(context)
        val initialSettings = store.load()
        provideContent {
            // updateAll does not restart provideGlance during an active session.
            val settings by remember(store) { store.observe() }.collectAsState(initialSettings)
            val size = LocalSize.current
            val bitmap = remember(settings, size) {
                WidgetBitmapRenderer.render(context, settings, (size.width.value * 2).toInt(), (size.height.value * 2).toInt())
            }
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = if (dev.hsichen.colorinvo.domain.CarrierCode.isValid(settings.carrierCode))
                    context.getString(R.string.barcode_preview, settings.carrierCode)
                else context.getString(R.string.widget_empty),
                contentScale = ContentScale.FillBounds,
                modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity(MainActivity::class.java)),
            )
        }
    }
}

class ColorInvoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ColorInvoWidget()
}
