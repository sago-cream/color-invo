package dev.hsichen.colorinvo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.hsichen.colorinvo.R

/** Available offline, including on devices without a browser or mail app. */
@Composable
fun AppInformation() {
    var information by rememberSaveable { mutableStateOf<Int?>(null) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        TextButton(onClick = { information = R.string.privacy_title }, modifier = Modifier.testTag("privacy")) {
            Text(stringResource(R.string.privacy_title))
        }
        TextButton(onClick = { information = R.string.help_title }, modifier = Modifier.testTag("help")) {
            Text(stringResource(R.string.help_title))
        }
    }
    information?.let { title ->
        AlertDialog(
            onDismissRequest = { information = null },
            title = { Text(stringResource(title)) },
            text = {
                Text(
                    stringResource(if (title == R.string.privacy_title) R.string.privacy_body else R.string.help_body),
                    Modifier.verticalScroll(rememberScrollState()).testTag("app-information"),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { information = null }) { Text(stringResource(R.string.done)) }
            },
        )
    }
}
