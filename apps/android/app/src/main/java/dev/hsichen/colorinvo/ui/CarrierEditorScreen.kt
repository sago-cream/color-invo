package dev.hsichen.colorinvo.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
    var step by rememberSaveable { mutableIntStateOf(0) }
    var showsCustomColors by rememberSaveable { mutableStateOf(false) }
    val currentStep = EditorStep.entries[step]
    val canContinue = when (currentStep) {
        EditorStep.CARRIER -> state.carrierIsValid
        EditorStep.COLORS -> state.settings.canSync && !state.isAnalyzingWallpaper
        else -> true
    }
    BackHandler(enabled = step > 0) { focus.clearFocus(); step-- }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.imePadding(),
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Row(
                    Modifier.navigationBarsPadding().padding(horizontal = Spacing.pageInset, vertical = Spacing.controlGap),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.controlGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (step > 0) TextButton(
                        onClick = { focus.clearFocus(); step-- },
                        modifier = Modifier.heightIn(min = Spacing.controlHeight).testTag("setup-back"),
                    ) { Text(stringResource(R.string.setup_back)) }
                    Button(
                        onClick = { focus.clearFocus(); step = if (currentStep == EditorStep.INSTALL) EditorStep.STYLE.ordinal else step + 1 },
                        enabled = canContinue,
                        shape = RoundedCornerShape(Spacing.controlRadius),
                        modifier = Modifier.weight(1f).heightIn(min = Spacing.controlHeight).testTag("setup-next"),
                    ) { Text(stringResource(currentStep.next)) }
                }
            }
        },
    ) { insets ->
        key(step) {
            Column(
                modifier = Modifier.fillMaxSize().padding(insets)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.pageInset, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.controlGap)) {
                    Text(stringResource(R.string.setup_progress, step + 1, EditorStep.entries.size), color = Muted, style = MaterialTheme.typography.labelLarge, modifier = Modifier.testTag("setup-progress"))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.smallGap), modifier = Modifier.fillMaxWidth()) {
                        EditorStep.entries.forEachIndexed { index, _ ->
                            Box(Modifier.weight(1f).height(4.dp).clip(CircleShape).background(if (index <= step) Primary else Hairline))
                        }
                    }
                    Text(stringResource(currentStep.title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("setup-title"))
                    Text(stringResource(currentStep.instruction), color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
                if (currentStep == EditorStep.COLORS || currentStep == EditorStep.INSTALL) WidgetPreview(state, compact = currentStep == EditorStep.COLORS)
                if (currentStep == EditorStep.STYLE) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.controlGap)) {
                        StyleOptions(state, largeText) { focus.clearFocus(); model.setDecoration(it) }
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
                }
                if (currentStep == EditorStep.CARRIER) {
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
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, autoCorrectEnabled = false, keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { if (state.carrierIsValid) { focus.clearFocus(); step = EditorStep.STYLE.ordinal } }),
                            shape = RoundedCornerShape(Spacing.controlRadius),
                            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label }.testTag("carrier-code"),
                        )
                    }
                }
                if (currentStep == EditorStep.COLORS) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.controlGap)) {
                        val scanMessage = stringResource(state.settings.palette.guidanceResource)
                        SectionHeader(stringResource(R.string.palette_title), stringResource(if (state.settings.palette.meetsCommercialGuidance) R.string.scan_ready else R.string.low_contrast), state.settings.palette.meetsCommercialGuidance, largeText, description = scanMessage, warning = !state.settings.palette.meetsCommercialGuidance)
                        FilledTonalButton(
                            onClick = { focus.clearFocus(); photoPicker.launch(PickVisualMediaRequest(ImageOnly)) },
                            enabled = !state.isAnalyzingWallpaper,
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(Spacing.controlGap),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("import-wallpaper"),
                        ) {
                            state.wallpaperPreview?.let { preview ->
                                Image(preview.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(Spacing.controlRadius)))
                            } ?: Icon(Icons.Outlined.PhotoLibrary, null, Modifier.size(48.dp))
                            Spacer(Modifier.width(Spacing.controlGap))
                            Text(stringResource(if (state.wallpaperPreview == null) R.string.import_wallpaper else R.string.change_wallpaper), Modifier.weight(1f))
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                        if (state.isAnalyzingWallpaper) Row(horizontalArrangement = Arrangement.spacedBy(Spacing.smallGap), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text(stringResource(R.string.analyzing_wallpaper), style = MaterialTheme.typography.bodyMedium, color = Muted)
                        }
                        if (state.wallpaperError) Text(stringResource(R.string.wallpaper_error), color = Warning, style = MaterialTheme.typography.bodySmall)
                        if (state.wallpaperPalettes.isNotEmpty()) {
                            Text(stringResource(R.string.wallpaper_colors), style = MaterialTheme.typography.labelLarge, color = Muted)
                            PaletteOptions(state, largeText, model::selectPalette)
                        }
                        val expansionState = stringResource(if (showsCustomColors) R.string.control_expanded else R.string.control_collapsed)
                        TextButton(
                            onClick = { showsCustomColors = !showsCustomColors },
                            contentPadding = PaddingValues(vertical = Spacing.smallGap),
                            modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.controlHeight).testTag("custom-colors").semantics { stateDescription = expansionState },
                        ) {
                            Text(stringResource(R.string.custom_colors), Modifier.weight(1f))
                            Text(if (showsCustomColors) "−" else "+", style = MaterialTheme.typography.titleLarge)
                        }
                        if (showsCustomColors) {
                            ColorControl(stringResource(R.string.background_color), state.settings.palette.backgroundColor.hex, model::updateBackgroundColor, Modifier.fillMaxWidth(), "background-color")
                            ColorControl(stringResource(R.string.bar_color), state.settings.palette.barColor.hex, model::updateBarColor, Modifier.fillMaxWidth(), "bar-color")
                            if (state.canResetPalette) TextButton(onClick = model::resetPaletteToWallpaper, modifier = Modifier.fillMaxWidth().testTag("reset-wallpaper-palette")) {
                                Icon(Icons.Outlined.Refresh, null, Modifier.size(20.dp))
                                Spacer(Modifier.width(Spacing.smallGap))
                                Text(stringResource(R.string.restore_wallpaper_colors))
                            }
                        }
                        if (!state.settings.palette.meetsCommercialGuidance) {
                            Text(scanMessage, color = Warning, style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = model::useClassicPalette, modifier = Modifier.testTag("use-classic-palette")) {
                                Text(stringResource(R.string.use_classic_palette))
                            }
                        }
                    }
                }
                if (currentStep == EditorStep.INSTALL) {
                    val status = when {
                        state.saveError -> R.string.save_error
                        state.isSaving -> R.string.saving
                        state.widgetIsReady -> R.string.synced
                        else -> R.string.not_synced
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.smallGap)) {
                        SectionHeader(stringResource(R.string.widget_title), stringResource(status), state.widgetIsReady, largeText, "widget-status", warning = state.saveError)
                        TextButton(onClick = { step = EditorStep.CARRIER.ordinal }, modifier = Modifier.testTag("edit-carrier")) {
                            Text(stringResource(R.string.edit_carrier))
                        }
                    }
                    listOf(R.string.setup_install_one, R.string.setup_install_two, R.string.setup_install_three).forEachIndexed { index, instruction ->
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.controlGap), verticalAlignment = Alignment.Top) {
                            Text("${index + 1}", color = Primary, style = MaterialTheme.typography.labelLarge, modifier = Modifier.background(PrimarySoft, CircleShape).padding(horizontal = Spacing.controlGap, vertical = Spacing.smallGap))
                            Text(stringResource(instruction), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        }
                    }
                    Text(stringResource(R.string.setup_edit_hint), color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
                if (currentStep == EditorStep.CARRIER || currentStep == EditorStep.INSTALL) AppInformation()
            }
        }
    }
}

private enum class EditorStep(val title: Int, val instruction: Int, val next: Int) {
    CARRIER(R.string.setup_carrier_title, R.string.setup_carrier_hint, R.string.setup_next_style),
    STYLE(R.string.setup_style_title, R.string.setup_style_hint, R.string.setup_next_colors),
    COLORS(R.string.setup_colors_title, R.string.setup_colors_hint, R.string.setup_next_install),
    INSTALL(R.string.setup_install_title, R.string.setup_install_hint, R.string.setup_restart),
}

@Composable
private fun WidgetPreview(state: CarrierEditorState, compact: Boolean = false) {
    Box(
        Modifier.fillMaxWidth().height(if (compact) 160.dp else Spacing.previewHeight)
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
}

@Composable
private fun StyleOptions(state: CarrierEditorState, largeText: Boolean, onSelect: (Decoration) -> Unit) {
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(Spacing.controlGap)) {
        Decoration.entries.forEach { decoration ->
            val selected = state.settings.decoration == decoration
            val thumbnail: @Composable (Modifier) -> Unit = { modifier ->
                CarrierPreview(
                    state.settings.copy(decoration = decoration),
                    modifier.clip(RoundedCornerShape(Spacing.controlRadius))
                        .border(1.dp, Color.Black.copy(alpha = 0.10f), RoundedCornerShape(Spacing.controlRadius))
                        .clearAndSetSemantics { },
                )
            }
            val label: @Composable () -> Unit = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.controlGap), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(decoration.label), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    RadioButton(selected, onClick = null, modifier = Modifier.size(24.dp).clearAndSetSemantics { })
                }
            }
            val card = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(if (selected) PrimarySoft else Surface)
                .border(if (selected) 2.dp else 1.dp, if (selected) Primary else Hairline, RoundedCornerShape(20.dp))
                .selectable(selected, role = Role.RadioButton, onClick = { onSelect(decoration) })
                .padding(Spacing.controlGap).testTag("decoration-${decoration.name.lowercase()}")
            if (largeText) Column(card, verticalArrangement = Arrangement.spacedBy(Spacing.controlGap)) {
                thumbnail(Modifier.fillMaxWidth().aspectRatio(329f / 155))
                label()
            } else Row(card, horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                thumbnail(Modifier.width(144.dp).height(68.dp))
                Box(Modifier.weight(1f)) { label() }
            }
        }
    }
}

@Composable
private fun PaletteOptions(state: CarrierEditorState, largeText: Boolean, onSelect: (BarcodePalette) -> Unit) {
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(Spacing.smallGap)) {
        state.wallpaperPalettes.withIndex().toList().chunked(if (largeText) 1 else 3).forEach { palettes ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.smallGap)) {
                palettes.forEach { (index, palette) ->
                    val selected = palette.hasSameColors(state.settings.palette)
                    val label = stringResource(R.string.palette_option, index + 1)
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(if (selected) PrimarySoft else Surface)
                            .border(if (selected) 2.dp else 1.dp, if (selected) Primary else Hairline, RoundedCornerShape(16.dp))
                            .selectable(selected, enabled = !state.isAnalyzingWallpaper, role = Role.RadioButton, onClick = { onSelect(palette) })
                            .semantics { contentDescription = label }.padding(Spacing.smallGap).testTag("wallpaper-palette-$index"),
                        verticalArrangement = Arrangement.spacedBy(Spacing.smallGap),
                    ) {
                        CarrierPreview(
                            state.settings.copy(palette = palette, decoration = Decoration.NONE, showsBarcodeValue = false),
                            Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(Spacing.controlRadius)).clearAndSetSemantics { },
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.palette_short, index + 1), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                            RadioButton(selected, onClick = null, modifier = Modifier.size(20.dp).clearAndSetSemantics { })
                        }
                    }
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
