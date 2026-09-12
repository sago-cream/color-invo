package dev.hsichen.colorinvo

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import dev.hsichen.colorinvo.data.CarrierSettings
import dev.hsichen.colorinvo.data.CarrierStore
import dev.hsichen.colorinvo.widget.ColorInvoWidgetReceiver
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Test

/** Exercises the provider → Glance → WorkManager → actual host RemoteViews path. */
@SdkSuppress(minSdkVersion = 29)
class WidgetLifecycleTest {
    @Test fun savedCarrierRendersInARealWidgetHost() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val rendered = AtomicBoolean(false)
        val store = CarrierStore(context)
        val previous = store.load()
        val host = object : AppWidgetHost(context, 0x4349) {
            override fun onCreateView(context: Context, appWidgetId: Int, appWidget: AppWidgetProviderInfo): AppWidgetHostView =
                object : AppWidgetHostView(context) {
                    override fun updateAppWidget(remoteViews: RemoteViews?) {
                        super.updateAppWidget(remoteViews)
                        if (hasRenderedImage(this)) rendered.set(true)
                    }
                }
        }
        var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        try {
            store.save(CarrierSettings(carrierCode = "/ABC1234"))
            instrumentation.uiAutomation.adoptShellPermissionIdentity("android.permission.BIND_APPWIDGET")
            val manager = AppWidgetManager.getInstance(context)
            widgetId = host.allocateAppWidgetId()
            val options = Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 329)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 329)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 155)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 155)
            }
            val provider = ComponentName(context, ColorInvoWidgetReceiver::class.java)
            assertTrue("Test host must bind the provider", manager.bindAppWidgetIdIfAllowed(widgetId, provider, options))
            instrumentation.runOnMainSync {
                host.startListening()
                host.createView(context, widgetId, manager.getAppWidgetInfo(widgetId))
            }
            context.sendBroadcast(Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                component = provider
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
            })
            val deadline = SystemClock.elapsedRealtime() + 15000
            while (!rendered.get() && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(100)
            assertTrue("The saved carrier must reach the widget host as rendered artwork", rendered.get())
        } finally {
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) host.deleteAppWidgetId(widgetId)
            host.stopListening()
            instrumentation.uiAutomation.dropShellPermissionIdentity()
            store.save(previous)
        }
    }

    private fun hasRenderedImage(view: View): Boolean =
        (view is ImageView && view.drawable != null && view.contentDescription?.contains("/ABC1234") == true) ||
            (view is ViewGroup && (0 until view.childCount).any { hasRenderedImage(view.getChildAt(it)) })
}
