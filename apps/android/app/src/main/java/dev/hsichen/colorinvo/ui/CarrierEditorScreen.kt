package dev.hsichen.colorinvo.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.hsichen.colorinvo.R
import dev.hsichen.colorinvo.data.Decoration
import dev.hsichen.colorinvo.domain.BarcodePalette
import dev.hsichen.colorinvo.ui.EditorMetrics as Spacing

@Composable
fun CarrierEditorScreen(model: CarrierEditorViewModel = viewModel()) {
    val state by model.state
    val focus = LocalFocusManager.current
    val photoPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri -> uri?.let(model::loadWallpaper) }
    val largeText = LocalDensity.current.fontScale >= 1.3f
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.pageInset, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.controlGap)) {
                val status = when {
                    state.saveError -> R.string.save_error
                    state.isSaving -> R.string.saving
                    state.widgetIsReady -> R.string.synced
                    else -> R.string.not_synced
                }
                SectionHeader(stringResource(R.string.widget_title), stringResource(status), state.widgetIsReady, largeText, "widget-status", warning = state.saveError)
                Row(
                    Modifier.fillMaxWidth().selectableGroup().clip(RoundedCornerShape(Spacing.controlRadius))
                        .background(PrimarySoft).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Decoration.entries.forEach { decoration ->
                        val selected = state.settings.decoration == decoration
                        Box(
                            modifier = Modifier.weight(1f).heightIn(min = Spacing.controlHeight)
                                .clip(RoundedCornerShape(4.dp)).background(if (selected) Surface else Color.Transparent)
                                .selectable(selected, role = Role.RadioButton, onClick = { focus.clearFocus(); model.setDecoration(decoration) })
                                .testTag("decoration-${decoration.name.lowercase()}"),
                            contentAlignment = Alignment.Center,
                        ) { Text(stringResource(decoration.label), style = MaterialTheme.typography.labelLarge, color = if (selected) Primary else Muted, modifier = Modifier.padding(Spacing.smallGap)) }
                    }
                }
                Box(
                    Modifier.fillMaxWidth().height(Spacing.previewHeight)
                        .clip(RoundedCornerShape(Spacing.controlRadius)).background(PrimarySoft)
                        .border(1.dp, Color.Black.copy(alpha = 0.10f), RoundedCornerShape(Spacing.controlRadius))
                        .testTag("wallpaper-preview"),
                    contentAlignment = Alignment.Center,
                ) {
                    state.wallpaperPreview?.let { preview ->
                        Image(preview.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
                        Box(Modifier.matchParentSize().background(Color.White.copy(alpha = 0.52f)))
                    }
                    CarrierPreview(
                        state.settings,
                        Modifier.padding(Spacing.previewInset).fillMaxWidth().aspectRatio(329f / 155)
                            .shadow(8.dp, RoundedCornerShape(Spacing.widgetRadius))
                            .clip(RoundedCornerShape(Spacing.widgetRadius)).testTag("carrier-preview"),
                    )
                }
                Row(
                    Modifier.fillMaxWidth().border(1.dp, Hairline, RoundedCornerShape(Spacing.controlRadius))
                        .clip(RoundedCornerShape(Spacing.controlRadius))
                        .toggleable(state.settings.showsBarcodeValue, role = Role.Switch, onValueChange = model::setShowsBarcodeValue)
                        .padding(horizontal = Spacing.controlGap, vertical = 4.dp).testTag("show-carrier-value"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.show_carrier_value), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(Spacing.smallGap))
                    Switch(state.settings.showsBarcodeValue, onCheckedChange = null)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.controlGap)) {
                val status = when {
                    state.carrierSuffix.isEmpty() -> ""
                    state.carrierIsValid -> stringResource(R.string.valid_format)
                    else -> stringResource(R.string.invalid_format)
                }
                SectionHeader(stringResource(R.string.carrier_title), status, state.carrierIsValid, largeText, warning = state.carrierSuffix.isNotEmpty() && !state.carrierIsValid)
                val label = stringResource(R.string.carrier_title)
                OutlinedTextField(
                    value = state.carrierSuffix, onValueChange = model::updateCarrierSuffix,
                    prefix = { Text("/", fontFamily = FontFamily.Monospace) },
                    placeholder = { Text("ABC1234", fontFamily = FontFamily.Monospace) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    singleLine = true,
                    isError = state.carrierSuffix.isNotEmpty() && !state.carrierIsValid,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, autoCorrectEnabled = false, keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                    shape = RoundedCornerShape(Spacing.controlRadius),
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = label }.testTag("carrier-code"),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.controlGap)) {
                val scanMessage = stringResource(state.settings.palette.guidanceResource)
                SectionHeader(stringResource(R.string.palette_title), stringResource(if (state.settings.palette.meetsCommercialGuidance) R.string.scan_ready else R.string.low_contrast), state.settings.palette.meetsCommercialGuidance, largeText, description = scanMessage, warning = !state.settings.palette.meetsCommercialGuidance)
                FilledTonalButton(
                    onClick = { focus.clearFocus(); photoPicker.launch(PickVisualMediaRequest(ImageOnly)) },
                    enabled = !state.isAnalyzingWallpaper,
                    shape = RoundedCornerShape(Spacing.controlRadius),
                    modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.controlHeight).testTag("import-wallpaper"),
                ) {
                    Icon(Icons.Outlined.PhotoLibrary, null)
                    Spacer(Modifier.width(Spacing.smallGap))
                    Text(stringResource(if (state.isAnalyzingWallpaper) R.string.analyzing_wallpaper else R.string.import_wallpaper))
                }
                if (state.wallpaperError) Text(stringResource(R.string.wallpaper_error), color = Warning, style = MaterialTheme.typography.bodySmall)
                if (largeText) {
                    Text(stringResource(R.string.wallpaper_colors), style = MaterialTheme.typography.labelLarge, color = Muted)
                    PaletteOptions(state, model::selectPalette)
                } else Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.wallpaper_colors), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = Muted)
                    PaletteOptions(state, model::selectPalette)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.fine_tuning), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = Muted)
                    TextButton(onClick = model::resetPaletteToWallpaper, enabled = state.canResetPalette, modifier = Modifier.testTag("reset-wallpaper-palette")) {
                        Icon(Icons.Outlined.Refresh, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.reset))
                    }
                }
                if (largeText) {
                    ColorControl(stringResource(R.string.background_color), state.settings.palette.backgroundColor.hex, model::updateBackgroundColor, Modifier.fillMaxWidth(), "background-color")
                    ColorControl(stringResource(R.string.bar_color), state.settings.palette.barColor.hex, model::updateBarColor, Modifier.fillMaxWidth(), "bar-color")
                } else Row(horizontalArrangement = Arrangement.spacedBy(Spacing.smallGap)) {
                    ColorControl(stringResource(R.string.background_color), state.settings.palette.backgroundColor.hex, model::updateBackgroundColor, Modifier.weight(1f), "background-color")
                    ColorControl(stringResource(R.string.bar_color), state.settings.palette.barColor.hex, model::updateBarColor, Modifier.weight(1f), "bar-color")
                }
                if (!state.settings.palette.meetsCommercialGuidance) Text(scanMessage, color = Warning, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PaletteOptions(state: CarrierEditorState, onSelect: (BarcodePalette) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.smallGap), modifier = Modifier.selectableGroup()) {
        repeat(3) { index ->
            val palette = state.wallpaperPalettes.getOrNull(index)
            val selected = index == state.selectedPaletteIndex
            val enabled = palette != null && !state.isAnalyzingWallpaper
            val label = stringResource(if (palette == null) R.string.palette_unavailable else R.string.palette_option, index + 1)
            Box(
                Modifier.size(Spacing.controlHeight).clip(CircleShape)
                    .background(if (selected) PrimarySoft else Color.Transparent)
                    .selectable(selected, enabled = enabled, role = Role.RadioButton, onClick = { palette?.let(onSelect) })
                    .semantics { contentDescription = label }.testTag("wallpaper-palette-$index"),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(Spacing.swatchSize).clip(CircleShape).border(if (selected) 4.dp else 1.dp, if (selected) Primary else Hairline, CircleShape)) {
                    drawRect(palette?.let { Color(it.backgroundColor.argb) } ?: Surface)
                    drawRect(palette?.let { Color(it.barColor.argb) } ?: PrimarySoft, Offset.Zero, Size(size.width / 2, size.height))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, status: String, positive: Boolean, stacked: Boolean, tag: String = "", description: String = status, warning: Boolean = false) {
    val badge: @Composable () -> Unit = {
        if (status.isNotEmpty()) Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description }.testTag(tag),
        ) {
            Icon(if (positive) Icons.Outlined.CheckCircle else if (warning) Icons.Outlined.WarningAmber else Icons.Outlined.Refresh, null, Modifier.size(16.dp), tint = if (positive) Success else if (warning) Warning else Muted)
            Text(status, color = if (positive) Success else if (warning) Warning else Muted, style = MaterialTheme.typography.labelLarge)
        }
    }
    if (stacked) Column(verticalArrangement = Arrangement.spacedBy(Spacing.smallGap)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        badge()
    } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.smallGap)) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        badge()
    }
}

private val Decoration.label get() = when (this) {
    Decoration.CAT -> R.string.decoration_cat
    Decoration.WAVE -> R.string.decoration_wave
    Decoration.NONE -> R.string.decoration_none
}

private val BarcodePalette.guidanceResource get() = when {
    meetsCommercialGuidance -> R.string.scan_ready
    barColor.isReddish -> R.string.red_bars_warning
    backgroundColor.scannerReflectance < BarcodePalette.MINIMUM_BACKGROUND_REFLECTANCE -> R.string.bright_background_warning
    barColor.scannerReflectance > backgroundColor.scannerReflectance / 2 -> R.string.dark_bars_warning
    else -> R.string.symbol_contrast_warning
}
