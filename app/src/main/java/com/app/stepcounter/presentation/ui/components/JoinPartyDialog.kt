package com.app.stepcounter.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun JoinPartyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var inviteCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unisciti a un Party") },
        text = {
            Column {
                Text("Chiedi a un amico il codice di invito e inseriscilo qui:")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    label = { Text("Codice di invito") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(inviteCode) },
                enabled = inviteCode.isNotBlank()
            ) {
                Text("Unisciti")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Preview
@Composable
fun JoinPartyDialogPreview() {
    JoinPartyDialog(
        onDismiss = {},
        onConfirm = {}
    )
}