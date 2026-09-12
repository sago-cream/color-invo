package dev.hsichen.colorinvo.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.hsichen.colorinvo.R
import dev.hsichen.colorinvo.data.CarrierSettings
import dev.hsichen.colorinvo.widget.WidgetBitmapRenderer

@Composable
fun CarrierPreview(settings: CarrierSettings, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val description = stringResource(R.string.barcode_preview, settings.carrierCode.ifEmpty { "/ABC1234" })
    Canvas(modifier.semantics { contentDescription = description }) {
        drawIntoCanvas { WidgetBitmapRenderer.draw(context, it.nativeCanvas, settings, size.width, size.height, preview = true) }
    }
}
