package dev.hsichen.colorinvo.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.hsichen.colorinvo.R
import dev.hsichen.colorinvo.domain.RgbaColor
import kotlin.math.roundToInt

@Composable
internal fun ColorControl(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier, tag: String) {
    var open by remember { mutableStateOf(false) }
    Row(
        modifier.heightIn(min = EditorMetrics.controlHeight)
            .clip(RoundedCornerShape(EditorMetrics.controlRadius))
            .border(1.dp, Hairline, RoundedCornerShape(EditorMetrics.controlRadius))
            .clickable(role = Role.Button) { open = true }
            .semantics(mergeDescendants = true) { contentDescription = "$label, $value" }
            .padding(EditorMetrics.controlGap).testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.width(EditorMetrics.smallGap))
        Box(Modifier.size(EditorMetrics.swatchSize).clip(CircleShape).background(Color(RgbaColor.parse(value)!!.argb)).border(1.dp, Color.Black.copy(alpha = 0.10f), CircleShape))
    }
    if (open) ColorPickerDialog(label, value, onValueChange, { open = false }, tag)
}

@Composable
private fun ColorPickerDialog(label: String, value: String, onValueChange: (String) -> Unit, onDismiss: () -> Unit, tag: String) {
    var draft by remember(value) { mutableStateOf(value) }
    val selected = RgbaColor.parse(draft) ?: RgbaColor.parse(value)!!
    fun select(color: RgbaColor) { draft = color.hex; onValueChange(color.hex) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(EditorMetrics.smallGap)) {
                Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(EditorMetrics.controlRadius)).background(Color(selected.argb)).border(1.dp, Color.Black.copy(alpha = 0.10f), RoundedCornerShape(EditorMetrics.controlRadius)))
                val labels = listOf(stringResource(R.string.color_red), stringResource(R.string.color_green), stringResource(R.string.color_blue))
                listOf(selected.red, selected.green, selected.blue).forEachIndexed { index, channel ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(EditorMetrics.smallGap)) {
                        Text(labels[index], Modifier.widthIn(min = 48.dp), style = MaterialTheme.typography.labelLarge)
                        Slider(
                            value = channel.toFloat(),
                            onValueChange = { next -> select(when (index) {
                                0 -> selected.copy(red = next.toDouble())
                                1 -> selected.copy(green = next.toDouble())
                                else -> selected.copy(blue = next.toDouble())
                            }) },
                            modifier = Modifier.weight(1f).semantics { contentDescription = labels[index] }.testTag("$tag-channel-$index"),
                        )
                        Text((channel * 255).roundToInt().toString(), Modifier.width(32.dp), style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace))
                    }
                }
                OutlinedTextField(
                    value = draft, onValueChange = { draft = it; RgbaColor.parse(it)?.let { color -> onValueChange(color.hex) } },
                    label = { Text(stringResource(R.string.hex_color)) },
                    supportingText = { if (RgbaColor.parse(draft) == null) Text(stringResource(R.string.invalid_hex)) },
                    isError = RgbaColor.parse(draft) == null,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, autoCorrectEnabled = false, keyboardType = KeyboardType.Ascii),
                    modifier = Modifier.fillMaxWidth().testTag("$tag-hex"),
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss, modifier = Modifier.testTag("color-picker-done")) { Text(stringResource(R.string.done)) } },
    )
}
