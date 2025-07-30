package com.app.stepcounter.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.stepcounter.domain.model.PartyData
import com.app.stepcounter.presentation.ui.components.StepPartyCard
import com.app.stepcounter.presentation.viewmodel.PartyUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepPartyListScreen(
    parties: List<PartyData>,
    uiState: PartyUiState,
    onCreatePartyClick: () -> Unit,
    onPartyClick: (PartyData) -> Unit,
    onDeleteParty: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "I tuoi Party",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista party
        if (parties.isEmpty()) {
            // Stato vuoto
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nessun party ancora!",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Crea il tuo primo party per iniziare",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(parties) { party ->
                    StepPartyCard(
                        party = party,
                        onPartyClick = { onPartyClick(party) },
                        onDeleteClick = { onDeleteParty(party.id) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Pulsante Crea Party
        StepPartyCard(
            party = null,
            onCreatePartyClick = { showCreateDialog = true }
        )
    }

    // Dialog per creare party
    if (showCreateDialog) {
        CreatePartyDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                onCreatePartyClick()
                // Potresti passare il nome qui se necessario
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreatePartyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var partyName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crea nuovo Party") },
        text = {
            Column {
                Text("Inserisci il nome del party:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text("Nome party") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(partyName) },
                enabled = partyName.isNotBlank()
            ) {
                Text("Crea")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun StepPartyListScreenPreview_Empty() {
    StepPartyListScreen(
        parties = emptyList(),
        uiState = PartyUiState(isLoading = false, errorMessage = null),
        onCreatePartyClick = {},
        onPartyClick = {},
        onDeleteParty = {}
    )
}

@Preview(showBackground = true)
@Composable
fun StepPartyListScreenPreview_WithParties() {
    val sampleParties = listOf(
        PartyData(id = "1", name = "Party 1", participants = listOf("Alice", "Bob")),
        PartyData(id = "2", name = "Party 2", participants = listOf("Carlo")),
        PartyData(id = "3", name = "Party 3", participants = emptyList())
    )

    StepPartyListScreen(
        parties = sampleParties,
        uiState = PartyUiState(isLoading = false, errorMessage = null),
        onCreatePartyClick = {},
        onPartyClick = {},
        onDeleteParty = {}
    )
}

