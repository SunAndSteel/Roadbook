package com.florent.carnetconduite.ui.shared.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme

@Composable
fun EditKmDialog(
    title: String,
    initialKm: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var km by remember { mutableStateOf(initialKm.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = km,
                onValueChange = { km = it },
                label = { Text("Kilomètres") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                km.toIntOrNull()?.let { onConfirm(it) }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@DevicePreview
@Composable
private fun EditKmDialogPreview() {
    RoadbookTheme {
        EditKmDialog(
            title = "Modifier km arrivée",
            initialKm = 12620,
            onDismiss = {},
            onConfirm = {}
        )
    }
}
